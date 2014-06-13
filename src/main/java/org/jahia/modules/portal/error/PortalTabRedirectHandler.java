/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.portal.error;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.modules.portal.PortalConstants;
import org.jahia.modules.portal.service.PortalService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kevan
 * Date: 30/12/13
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class PortalTabRedirectHandler implements ErrorHandler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PortalTabRedirectHandler.class);

    private URLResolverFactory urlResolverFactory;
    private PortalService portalService;

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    public void setPortalService(PortalService portalService) {
        this.portalService = portalService;
    }

    @Override
    public boolean handle(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!(e instanceof PathNotFoundException)) {
            return false;
        }
        try {
            URLResolver urlResolver = urlResolverFactory.createURLResolver(request.getPathInfo(), request.getServerName(), request);
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(urlResolver.getWorkspace(), urlResolver.getLocale());
            JCRNodeWrapper portalNode = session.getNode(urlResolver.getPath());
            if(portalNode != null && portalNode.isNodeType(PortalConstants.JMIX_PORTAL)){
                // redirect to first tab
                List<JCRNodeWrapper> portalTabs = portalService.getPortalTabs(portalNode, session);
                if(portalTabs.size() > 0){
                    String link = request.getContextPath() + request.getServletPath() + "/" + StringUtils.substringBefore(
                            request.getPathInfo().substring(1),
                            "/") + "/" + urlResolver.getWorkspace() + "/" + urlResolver.getLocale() + portalTabs.get(0).getPath() + ".html";
                    response.sendRedirect(link);
                    return true;
                }
            }
        }catch (Exception e1){
            logger.error(e1.getMessage(), e1);
        }

        return false;
    }
}
