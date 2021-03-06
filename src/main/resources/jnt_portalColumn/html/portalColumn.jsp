<%@ page import="org.jahia.modules.portal.PortalConstants" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="portal" uri="http://www.jahia.org/tags/portalLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="nodetype" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>

<c:set var="portalGadgetMixin" value="<%= PortalConstants.JMIX_PORTAL_GADGET %>"/>
<c:set var="portalMixin" value="<%= PortalConstants.JMIX_PORTAL %>"/>
<c:set var="portalWidgetMixin" value="<%= PortalConstants.JMIX_PORTAL_WIDGET %>"/>
<c:set var="portalWidgetReferenceNT" value="<%= PortalConstants.JNT_PORTAL_WIDGET_REFERENCE %>"/>
<c:set var="portalTabNode" value="${renderContext.mainResource.node}"/>

<c:set var="widgetIdentifier" value="${not empty renderContext.request.parameterMap['w'] ? renderContext.request.parameterMap['w'][0] : null}"/>
<c:set var="widgetSolo" value="${not empty renderContext.request.parameterMap['w_solo'] ? renderContext.request.parameterMap['w_solo'][0] : null}"/>
<c:set var="widgetState" value="${not empty renderContext.request.parameterMap['w_state'] ? renderContext.request.parameterMap['w_state'][0] : ''}"/>
<c:set var="widgetView" value="${not empty renderContext.request.parameterMap['w_view'] ? renderContext.request.parameterMap['w_view'][0] : ''}"/>
<template:addCacheDependency flushOnPathMatchingRegexp="\\\\Q${renderContext.mainResource.node.path}\\\\E/[^/]*"/>

<c:choose>
    <c:when test="${widgetSolo != null && widgetIdentifier != null}">
        <jcr:node var="widgetNode" uuid="${widgetIdentifier}"/>
        <c:if test="${not empty widgetNode}">
            <c:set var="isGadget" value="${jcr:isNodeType(widgetNode, portalGadgetMixin) ||
        (jcr:isNodeType(widgetNode, 'jnt:portalWidgetReference') && not empty widgetNode.properties['j:node'] && jcr:isNodeType(widgetNode.properties['j:node'].node, 'jmix:portalGadget'))}"/>
            <div id="w_${widgetNode.identifier}" class="portal_widget"
                 data-widget-gadget="${isGadget}"
                 data-widget-reference="${jcr:isNodeType(widgetNode, portalWidgetReferenceNT)}"
                 data-widget-path="${widgetNode.path}"
                 data-widget-state="${widgetState}"
                 data-widget-view="${widgetView}"
                 data-col-id="${currentNode.identifier}"
                    >
                <c:if test="${isGadget}">
                    <template:module path="${widgetNode.path}" view="${widgetView}"/>
                </c:if>
            </div>
        </c:if>
    </c:when>
    <c:otherwise>
        <c:forEach var="widgetNode" items="${jcr:getChildrenOfType(currentNode, portalWidgetMixin)}">
            <c:if test="${not empty widgetNode}">
                <c:set var="isGadget" value="${jcr:isNodeType(widgetNode, portalGadgetMixin) ||
        (jcr:isNodeType(widgetNode, 'jnt:portalWidgetReference') && (not empty widgetNode.properties['j:node'].node) && jcr:isNodeType(widgetNode.properties['j:node'].node, 'jmix:portalGadget'))}"/>
                <div id="w_${widgetNode.identifier}" class="portal_widget"
                     data-widget-gadget="${isGadget}"
                     data-widget-reference="${jcr:isNodeType(widgetNode, portalWidgetReferenceNT)}"
                     data-widget-path="${widgetNode.path}"
                        <c:if test="${widgetIdentifier != null && widgetNode.identifier == widgetIdentifier}">
                            data-widget-state="${widgetState}"
                            data-widget-view="${widgetView}"
                        </c:if>
                     data-col-id="${currentNode.identifier}"
                        >
                    <c:if test="${isGadget}">
                        <template:module path="${widgetNode.path}"
                                         view="${widgetIdentifier != null && widgetNode.identifier == widgetIdentifier && not empty widgetView ? widgetView : 'portal.view'}"/>
                    </c:if>
                </div>
            </c:if>
        </c:forEach>
    </c:otherwise>
</c:choose>