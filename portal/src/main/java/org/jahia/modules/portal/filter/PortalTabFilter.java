package org.jahia.modules.portal.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.net.URLEncoder;

/**
 * Created with IntelliJ IDEA.
 * User: kevan
 * Date: 30/12/13
 * Time: 14:15
 * To change this template use File | Settings | File Templates.
 */
public class PortalTabFilter extends AbstractFilter{
    private static final String JS_API_FILE = "jahia-portal.js";
    private static final String MODULE_NAME = "jahia-portal";

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String path = URLEncoder.encode("/modules/" + MODULE_NAME + "/javascript/" + JS_API_FILE, "UTF-8");
        previousOut += ("<jahia:resource type='javascript' path='" + path + "' insert='false' resource='" + JS_API_FILE + "'/>");
        return previousOut;
    }
}
