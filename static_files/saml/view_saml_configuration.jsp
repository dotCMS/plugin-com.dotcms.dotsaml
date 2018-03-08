<%@page import="com.liferay.portal.language.LanguageUtil"%>

<script type="text/javascript" src="/html/plugins/plugin-com.dotcms.dotsaml/saml/view_saml_configuration_js_inc.jsp" ></script>

<div class="portlet-main">
	<!-- START Toolbar -->
	<div class="portlet-toolbar">
		<div class="portlet-toolbar__actions-primary">
			<div class="inline-form">
				<input type="text" name="filter" id="filter" onkeydown="" dojoType="dijit.form.TextBox" value="" />
		        <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
		            <%=LanguageUtil.get(pageContext, "Reset")%>
		        </button>
			</div>
		</div>
		<div class="portlet-toolbar__info">
		</div>
    	<div class="portlet-toolbar__actions-secondary">
		    <button dojoType="dijit.form.Button" onClick="idpAdmin.addEditIdp();" iconClass="plusIcon">
		            <%=LanguageUtil.get(pageContext, "add-idp")%>
		     </button>
    	</div>
   </div>

   <!-- END Toolbar -->

	<table class="listingTable idp-list">
	    <thead id="idpPTableHeader">
	        <tr>
	            <th><%=LanguageUtil.get(pageContext, "status")%></th>
	            <th><%=LanguageUtil.get(pageContext, "idp-name")%></th>
	            <th><%=LanguageUtil.get(pageContext, "actions")%></th>
	            <th><%=LanguageUtil.get(pageContext, "sp-metadata-file")%></th>
	        </tr>
	    </thead>
	    <tbody id="idpTableBody">
	    </tbody>
	</table>


	<div class="yui-gb buttonRow">
        <div class="yui-u first" style="text-align: left;" id="buttonPreviousResultsWrapper">
    	    <button dojoType="dijit.form.Button" id="buttonPreviousResults" onclick="" iconClass="previousIcon">
    		    <%=LanguageUtil.get(pageContext, "Previous")%>
    		</button>
    	</div>
    </div>

    <div class="yui-u" style="text-align: center;" id="resultsSummary">
        <%=LanguageUtil.get(pageContext, "Viewing-Results-From")%>
    </div>

    <div class="yui-u" style="text-align: right;" id="buttonNextResultsWrapper">
        <button dojoType="dijit.form.Button" id="buttonNextResults" onclick="" iconClass="nextIcon">
    	    <%=LanguageUtil.get(pageContext, "Next")%>
    	</button>
    </div>

</div>

<div id="addEditIdPDialog" dojoType="dijit.Dialog" disableCloseButton="true" title="<%=LanguageUtil.get(pageContext, "add-edit-dialog-title")%>" style="display: none; width:600px">
    <div>
        <form id="addEditIdPForm" name="addEditIdPForm" enctype="multipart/form-data" method="post" dojoType="dijit.form.Form">
            <div class="form-inline">
                <dl>
                    <dt><label for="idpConfigName"><%=LanguageUtil.get(pageContext, "idp-config-name-label")%></label></dt>
                    <dd><input type="text" name="idpConfigName" id="configName" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><input type="checkbox" dojoType="dijit.form.CheckBox" name="idp-status" id="idp-status" onClick=""/></dt>
                    <dd><label for="idp-status"><%=LanguageUtil.get(pageContext, "idp-status-label")%></label></dd>
                </dl>

                <dl>
                    <dt><label for="spIssuerURL"><%=LanguageUtil.get(pageContext, "sp-issuer-url-label")%></label></dt>
                    <dd><input type="text" name="spIssuerURL" id="spIssuerURL" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><label for="spEndpointHostname"><%=LanguageUtil.get(pageContext, "sp-endpoint-hostname-label")%></label></dt>
                    <dd><input type="text" name="spEndpointHostname" id="spEndpointHostname" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><label for="privateKey"><%=LanguageUtil.get(pageContext, "private-key-label")%></label></dt>
                    <dd><input type="file" id="privateKey" name="privateKey" required="true"></dd>
                </dl>

                <dl>
                    <dt><label for="publicCertificate"><%=LanguageUtil.get(pageContext, "public-certificate-label")%></label></dt>
                    <dd><input type="file" id="publicCertificate" name="publicCertificate" required="true"></dd>
                </dl>

                <dl>
                    <dt><label for="idpMetadata"><%=LanguageUtil.get(pageContext, "idp-metadata-label")%></label></dt>
                    <dd><input type="file" id="idpMetadata" name="idpMetadata" required="true"></dd>
                </dl>

                <dl>
                    <dt><label for="optionalProperties"><%=LanguageUtil.get(pageContext, "optional-properties-label")%></label></dt>
                    <dd><input type="text" dojoType="dijit.form.TextBox" name="optionalProperties" size="20" value="" /></dd>
                </dl>


                <dl>
                    <dt><label id="addSite" for=""><%= LanguageUtil.get(pageContext, "add-site") %></label></dt>
                    <dd>
                        <button dojoType="dijit.form.Button" onclick="" type="button">
                            <%= LanguageUtil.get(pageContext, "add-site-to-config") %>
                        </button>
                    </dd>
                </dl>


                <!-- Placeholder table with sites already assigned to the current IdP configuration .. Not sure if
                a table is what we need here, though-->

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
            </div>
            <div style="text-align: center">
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "save") %>
                </button>
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "cancel") %>
                </button>
            </div>
        </form>
    </div>
</div>