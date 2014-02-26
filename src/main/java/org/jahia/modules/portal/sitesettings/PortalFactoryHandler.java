package org.jahia.modules.portal.sitesettings;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.portal.PortalConstants;
import org.jahia.modules.portal.service.PortalService;
import org.jahia.modules.portal.sitesettings.form.PortalForm;
import org.jahia.modules.portal.sitesettings.form.PortalModelForm;
import org.jahia.modules.portal.sitesettings.table.UserPortalsPager;
import org.jahia.modules.portal.sitesettings.table.UserPortalsTable;
import org.jahia.modules.portal.sitesettings.table.UserPortalsTableRow;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QueryResultWrapperImpl;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kevan
 * Date: 23/12/13
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class PortalFactoryHandler implements Serializable {
    private static final long serialVersionUID = 978219001163542883L;

    private static final Logger logger = LoggerFactory.getLogger(PortalFactoryHandler.class);
    private static final String BUNDLE = "resources.portal-core";

    @Autowired
    private transient PortalService portalService;

    public List<JCRNodeWrapper> getSitePortalModels(RequestContext ctx) {
        return portalService.getSitePortalModels(getRenderContext(ctx).getSite(), null, false, getCurrentUserSession(ctx, "live"));
    }

    public boolean createPortalModel(RequestContext ctx, PortalModelForm form){
        try {
            portalService.createPortalModel(form, getRenderContext(ctx).getSite(), getCurrentUserSession(ctx, "live"));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }
    
    public boolean updatePortalModel(RequestContext ctx, PortalForm form, String portalModelIdentifier){
        try {
            form.setPortalModelIdentifier(portalModelIdentifier);
            portalService.updatePortalModel(form, getCurrentUserSession(ctx, "live"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    public void initPortalForm(RequestContext ctx){
        JCRSiteNode site = getRenderContext(ctx).getSite();

        ctx.getRequestScope().put("templatesPath", site.getTemplatePackage().getRootFolderPath() + "/" + site.getTemplatePackage().getVersion() + "/templates");
        try {
            ctx.getRequestScope().put("allowedWidgetsSkin", RenderService.getInstance().getViewsSet(
                    NodeTypeRegistry.getInstance().getNodeType(PortalConstants.JMIX_PORTAL_WIDGET),
                    getRenderContext(ctx).getSite(), "html"));
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public PortalForm initEditPortalForm(RequestContext ctx, String identifier) {
        JCRSiteNode site = getRenderContext(ctx).getSite();

        ctx.getRequestScope().put("templatesPath", site.getTemplatePackage().getRootFolderPath() + "/" + site.getTemplatePackage().getVersion() + "/templates");

        PortalForm form = new PortalForm();
        if(StringUtils.isNotEmpty(identifier)){
            try {
                JCRNodeWrapper portalNode = getCurrentUserSession(ctx, "live").getNodeByUUID(identifier);
                ctx.getRequestScope().put("portalNode", portalNode);

                form.setName(portalNode.getDisplayableName());
                
                List<String> allowedWidgetTypes = new ArrayList<String>();
                JCRPropertyWrapper allowedWidgetTypesProp = portalNode.getProperty(PortalConstants.J_ALLOWED_WIDGET_TYPES);
                for(JCRValueWrapper allowedWidgetType : allowedWidgetTypesProp.getValues()){
                    allowedWidgetTypes.add(allowedWidgetType.getString());
                }
                form.setAllowedWidgetTypes(allowedWidgetTypes.toArray(new String[allowedWidgetTypes.size()]));
                form.setTemplateFull(portalNode.getPropertyAsString(PortalConstants.J_FULL_TEMPLATE));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return form;
    }

    public UserPortalsTable initUserPortalsManager(RequestContext ctx) {
        UserPortalsTable userPortalsTable = new UserPortalsTable();
        UserPortalsPager pager = new UserPortalsPager();
        try {
            pager.setMaxResults(getUserPortalsQuery(ctx, null, null).execute().getNodes().getSize());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            pager.setMaxResults(0);
        }
        userPortalsTable.setPager(pager);
        userPortalsTable.setRows(new LinkedHashMap<String, UserPortalsTableRow>());
        return userPortalsTable;
    }

    private Query getUserPortalsQuery(RequestContext ctx, String sortby, String sortOrder){
        Query query = null;
        try {
            JCRSessionWrapper sessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession("live");
            QueryManager queryManager = sessionWrapper.getWorkspace().getQueryManager();
            query = queryManager.createQuery(("select * from [" + PortalConstants.JNT_PORTAL_USER + "] " +
                    "as p where p.['" + PortalConstants.J_SITEKEY + "'] = '" +
                    getRenderContext(ctx).getSite().getSiteKey()) + "'" +
                    (StringUtils.isNotEmpty(sortby) ? " order by '" + sortby + "' " + sortOrder : ""), Query.JCR_SQL2);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return query;
    }

    public void doUserPortalsQuery(RequestContext ctx, UserPortalsTable userPortalsTable){
        try {
            Query query = getUserPortalsQuery(ctx, userPortalsTable.getPager().getSortBy(), userPortalsTable.getPager().isSortAsc() ? "ASC" : "DESC");
            query.setLimit(userPortalsTable.getPager().getItemsPerPage());
            query.setOffset(userPortalsTable.getPager().getItemsPerPage() * (userPortalsTable.getPager().getPage() - 1));

            NodeIterator nodeIterator = query.execute().getNodes();
            userPortalsTable.setRows(new LinkedHashMap<String, UserPortalsTableRow>());
            while (nodeIterator.hasNext()){
                JCRNodeWrapper portalNode = (JCRNodeWrapper) nodeIterator.next();
                UserPortalsTableRow row = new UserPortalsTableRow();
                row.setUserNodeIdentifier(JCRContentUtils.getParentOfType(portalNode, "jnt:user").getIdentifier());
                row.setModelName(((JCRNodeWrapper) portalNode.getProperty("j:model").getNode()).getDisplayableName());
                row.setLastUsed(portalNode.getPropertyAsString("j:lastViewed"));
                row.setCreated(portalNode.getProperty("jcr:created").getDate().getTime());
                userPortalsTable.getRows().put(portalNode.getPath(), row);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean enablePortalModel(RequestContext ctx, String selectedPortalModelIdentifier){
        portalService.switchPortalModelActivation(getCurrentUserSession(ctx, "live"), selectedPortalModelIdentifier, true);
        return true;
    }

    public boolean disablePortalModel(RequestContext ctx, String selectedPortalModelIdentifier){
        portalService.switchPortalModelActivation(getCurrentUserSession(ctx, "live"), selectedPortalModelIdentifier, false);
        return true;
    }

    public String getTemplatesPath(RequestContext ctx){
        JCRSiteNode currentSite = getRenderContext(ctx).getSite();
        return currentSite.getTemplatePackage().getRootFolderPath() + "/" + currentSite.getTemplatePackage().getVersion() + "/templates";
    }

    public void deleteUserPortal(RequestContext ctx, MessageContext messageContext, String selectedPortal, UserPortalsTable userPortalsTable){
        JCRSessionWrapper sessionWrapper = getCurrentUserSession(ctx, "live");
        try {
            JCRNodeWrapper portalNode = sessionWrapper.getNode(selectedPortal);
            String name = portalNode.getDisplayableName();
            portalNode.remove();
            sessionWrapper.save();
            setActionMessage(messageContext, true, "manageUserPortals", ".deleted", name);

            userPortalsTable.getPager().setMaxResults(userPortalsTable.getPager().getMaxResults() - 1);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            setActionMessage(messageContext, false, "manageUserPortals", ".deleted", null);
        }
    }

    private void setActionMessage(MessageContext msgCtx, boolean success, String panel, String action, Object name){
        Locale locale = LocaleContextHolder.getLocale();

        String successFlag = success ? ".successfully" : ".failed";

        String message = Messages.get(BUNDLE, panel + successFlag + action, locale);
        if(name != null){
            message = Messages.format(message, name);
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        if(success){
            messageBuilder.info();
        } else {
            messageBuilder.error();
        }
        messageBuilder.defaultText(message);
        msgCtx.addMessage(messageBuilder.build());
    }

    private JCRSessionWrapper getCurrentUserSession(RequestContext ctx) {
        try {
            RenderContext renderContext = getRenderContext(ctx);
            return JCRSessionFactory.getInstance().getCurrentUserSession(renderContext.getMainResource().getWorkspace(), renderContext.getMainResourceLocale());
        } catch (RepositoryException e) {
            logger.error("Error retrieving current user session", e);
        }
        return null;
    }

    private JCRSessionWrapper getCurrentUserSession(RequestContext ctx, String workspace) {
        try {
            return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, getRenderContext(ctx).getMainResourceLocale());
        } catch (RepositoryException e) {
            logger.error("Error retrieving current user session", e);
        }
        return null;
    }

    private JCRNodeWrapper getNodeByUUID(String identifier, JCRSessionWrapper session) {
        try {
            return session.getNodeByUUID(identifier);
        } catch (RepositoryException e) {
            logger.error("Error retrieving node with UUID " + identifier, e);
        }
        return null;
    }

    private RenderContext getRenderContext(RequestContext ctx) {
        return (RenderContext) ctx.getExternalContext().getRequestMap().get("renderContext");
    }

    public PortalService getPortalService() {
        return portalService;
    }

    public void setPortalService(PortalService portalService) {
        this.portalService = portalService;
    }
}
