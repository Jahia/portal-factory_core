package org.jahia.modules.portal.sitesettings;

import org.jahia.modules.portal.service.PortalService;
import org.jahia.modules.portal.sitesettings.form.PortalForm;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevan
 * Date: 23/12/13
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class ManagePortalsHandler implements Serializable {
    private static final long serialVersionUID = 978219001163542883L;

    private static final Logger logger = LoggerFactory.getLogger(ManagePortalsHandler.class);

    @Autowired
    private transient PortalService portalService;

    public List<JCRNodeWrapper> getSitePortalModels(RequestContext ctx) {
        return portalService.getSitePortalModels(getRenderContext(ctx).getSite(), null, false, getCurrentUserSession(ctx, "live"));
    }

    public boolean createPortalModel(RequestContext ctx, PortalForm form){
        try {
            portalService.createPortalModel(form, getRenderContext(ctx).getSite(), getCurrentUserSession(ctx, "live"));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return true;
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
