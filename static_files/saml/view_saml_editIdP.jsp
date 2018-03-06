<%@ include file="/html/plugins/plugin-com.dotcms.dotsaml/saml/view_saml_configuration_js_inc.jsp" %>

<div class="portlet-main">
	<!-- START Toolbar -->
	<div class="portlet-toolbar">
   </div>

   <!-- END Toolbar -->

    <div>
        <label for="idpConfigName"><%=LanguageUtil.get(pageContext, "idp-config-name-label")%></label>
        <input type="text" name="idpConfigName" id="configName" onkeydown="" dojoType="dijit.form.TextBox" value="" />
    </div>

    <div>
        <input type="checkbox" dojoType="dijit.form.CheckBox" name="idp-status" id="idp-status" onClick=""/>
        <label for="idp-status"><%=LanguageUtil.get(pageContext, "idp-status-label")%></label>
    </div>

    <div>
        <label for="spIssuerURL"><%=LanguageUtil.get(pageContext, "sp-issuer-url-label")%></label>
        <input type="text" name="spIssuerURL" id="spIssuerURL" onkeydown="" dojoType="dijit.form.TextBox" value="" />
    </div>

    <div>
        <label for="spEndpointHostname"><%=LanguageUtil.get(pageContext, "sp-endpoint-hostname-label")%></label>
        <input type="text" name="spEndpointHostname" id="spEndpointHostname" onkeydown="" dojoType="dijit.form.TextBox" value="" />
    </div>

    <div>
        <form action="" enctype="multipart/form-data" id="privateKeyUploadForm" name="privateKeyUploadForm" method="post">
            <div>
                <label for="privateKey"><%=LanguageUtil.get(pageContext, "private-key-label")%></label>
                <input type="file" id="privateKey" name="privateKey">
            </div>
            <div style="text-align: center">
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                            <%= LanguageUtil.get(pageContext, "private-key-upload") %>
                </button>
            </div>
        </form>
    </div>

    <div>
        <form action="" enctype="multipart/form-data" id="publicCertificateUploadForm" name="publicCertificateUploadForm" method="post">
            <div>
                <label for="publicCertificate"><%=LanguageUtil.get(pageContext, "public-certificate-label")%></label>
                <input type="file" id="publicCertificate" name="publicCertificate">
            </div>
            <div style="text-align: center">
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "public-certificate-upload") %>
                </button>
            </div>
        </form>
    </div>

    <div>
        <form action="" enctype="multipart/form-data" id="idpMetadataUploadForm" name="idpMetadataUploadForm" method="post">
            <div>
                <label for="idpMetadata"><%=LanguageUtil.get(pageContext, "idp-metadata-label")%></label>
                <input type="file" id="idpMetadata" name="idpMetadata">
            </div>
            <div style="text-align: center">
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "idp-metadata-upload") %>
                </button>
            </div>
        </form>
    </div>

    <div>
        <label for="optionalProperties"><%=LanguageUtil.get(pageContext, "optional-properties-label")%></label>
        <input type="text" dojoType="dijit.form.TextBox" name="optionalProperties" size="20" value="" />
    </div>

    <div>
		<div>
			<label id="addSite" for=""><%= LanguageUtil.get(pageContext, "add-site") %></label>
			<div dojoType="dotcms.dijit.form.HostFolderFilteringSelect"></div>
                <button dojoType="dijit.form.Button" onclick="" type="button">
			        <%= LanguageUtil.get(pageContext, "add-site-to-config") %>
			    </button>
		</div>
    </div>

    <!-- Placeholder table with sites already assigned to the current IdP configuration -->

    <table class="listingTable">
        <thead id="siteTableHeader">
            <tr>
                <th><%=LanguageUtil.get(pageContext, "site")%></th>
                <th><%=LanguageUtil.get(pageContext, "actions")%></th>
            </tr>
        </thead>
        <tbody id="idpTableBody">
            <tr>
                <td>site1.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "remove")%>
                    </button>
                </td>
            </tr>
            <tr>
                <td>site2.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "remove")%>
                    </button>
                </td>
            </tr>
            <tr>
                <td>site3.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "remove")%>
                    </button>
                </td>
            </tr>
        </tbody>
    </table>

    <div style="text-align: center">
        <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
            <%= LanguageUtil.get(pageContext, "save") %>
        </button>
        <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
            <%= LanguageUtil.get(pageContext, "cancel") %>
        </button>
    </div>



</div>