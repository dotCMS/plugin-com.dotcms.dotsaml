<%@ page import="com.dotmarketing.business.CacheLocator" %>
<%@ page import="com.dotmarketing.util.Logger" %>
<%@ page import="com.dotcms.plugin.saml.v3.cache.SamlCache" %>

<%@ include file="/html/common/init.jsp"%>
<%request.setAttribute("requiredPortletAccess", "maintenance"); %>
<%@ include file="/html/common/uservalidation.jsp"%>

<%
    try{
        Logger.info(SamlCache.class, "Clearing SAML cache");
        CacheLocator.getSamlCache().clearCache();
        Logger.info(SamlCache.class, "SAML cache cleared");
    } catch (Exception e){
        Logger.error(SamlCache.class, "Error clearing SAML cache", e);
    }
%>