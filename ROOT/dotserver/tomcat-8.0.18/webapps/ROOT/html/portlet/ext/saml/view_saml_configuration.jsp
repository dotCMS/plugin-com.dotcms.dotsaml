<%@page import="com.dotmarketing.beans.Host"%>
<%@ page import="com.dotmarketing.business.APILocator" %>
<%@page import="com.liferay.portal.language.LanguageUtil"%>
<%@page import="java.util.List"%>

<script type="text/javascript" src="/html/portlet/ext/saml/view_saml_configuration_js_inc.jsp" ></script>

<div class="portlet-main">
	<!-- START Toolbar -->
	<div class="portlet-toolbar">
		<%--<div class="portlet-toolbar__actions-primary">
			<div class="inline-form">
				<input type="text" name="filter" id="filter" onkeydown="" dojoType="dijit.form.TextBox" value="" />
				<button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
			</div>
		</div>--%>
		<div class="portlet-toolbar__info">
		</div>
		<div class="portlet-toolbar__actions-secondary">
			<button dojoType="dijit.form.Button" onClick="idpAdmin.addIdp();" iconClass="plusIcon">
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
			<button dojoType="dijit.form.Button" id="buttonPreviousResults" onclick="idpAdmin.gotoPreviousPage()" iconClass="previousIcon">
				<%=LanguageUtil.get(pageContext, "Previous")%>
			</button>
		</div>
		<div class="yui-u" style="text-align: center;" id="resultsSummary">
			<%=LanguageUtil.get(pageContext, "Viewing-Results-From")%>
		</div>
		<div class="yui-u" style="text-align: right;" id="buttonNextResultsWrapper">
			<button dojoType="dijit.form.Button" id="buttonNextResults" onclick="idpAdmin.gotoNextPage()" iconClass="nextIcon">
				<%=LanguageUtil.get(pageContext, "Next")%>
			</button>
		</div>
	</div>

</div>

<div id="addEditIdPDialog" dojoType="dijit.Dialog" disableCloseButton="true" title="<%=LanguageUtil.get(pageContext, "add-edit-dialog-title")%>" style="display: none; width:600px">
	<div>
		<form id="addEditIdPForm" name="addEditIdPForm" dojoType="dijit.form.Form">
			<div class="form-inline">
				<dl>
					<dt><label for="id"><%=LanguageUtil.get(pageContext, "idp-id")%></label></dt>
					<dd><input type="text" name="id" id="id" value="" dojoType="dijit.form.TextBox" readonly="true" class="input-text-naked"></dd>
				</dl>
				<dl>
					<dt><label for="idpName" class="required"><%=LanguageUtil.get(pageContext, "idp-config-name-label")%></label></dt>
					<dd><input type="text" name="idpName" id="idpName" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
				</dl>

				<dl>
					<dt><label><%=LanguageUtil.get(pageContext, "idp-status-label")%></label></dt>
					<dd>
						<input type="radio" name="enabled" value="true" id="enabledTrue" /> <label for="enabledTrue">Yes</label>
						<input type="radio" name="enabled" value="false" id="enabledFalse" /> <label for="enabledFalse">No</label>
					</dd>

				</dl>

				<dl>
					<dt><label for="sPIssuerURL" class="required"><%=LanguageUtil.get(pageContext, "sp-issuer-url-label")%></label></dt>
					<dd><input type="text" name="sPIssuerURL" id="sPIssuerURL" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
				</dl>

				<dl>
					<dt><label for="sPEndpointHostname" class="required"><%=LanguageUtil.get(pageContext, "sp-endpoint-hostname-label")%></label></dt>
					<dd><input type="text" name="sPEndpointHostname" id="sPEndpointHostname" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
				</dl>

				<dl>
					<dt><label for="privateKey" class="required"><%=LanguageUtil.get(pageContext, "private-key-label")%></label></dt>
					<dd><div id="privateKeySavedFile" id="privateKeySavedFile"></div><input type="file" id="privateKey" name="privateKey" required="true"></dd>
				</dl>

				<dl>
					<dt><label for="publicCert" class="required"><%=LanguageUtil.get(pageContext, "public-certificate-label")%></label></dt>
					<dd><div id="publicCertSavedFile" id="publicCertSavedFile"></div><input type="file" id="publicCert" name="publicCert" required="true"></dd>
				</dl>

				<dl>
					<dt><label for="idPMetadataFile" class="required"><%=LanguageUtil.get(pageContext, "idp-metadata-label")%></label></dt>
					<dd><div id="idPMetadataSavedFile" id="idPMetadataSavedFile"></div><input type="file" id="idPMetadataFile" name="idPMetadataFile" required="true"></dd>
				</dl>


				<dl>
					<dt><label for="signatureValidationType" class="required"><%=LanguageUtil.get(pageContext, "idp-validation-label")%></label></dt>
					<dd><select id="signatureValidationType" dojoType="dijit.form.Select" required="true">
						<option value="responseandassertion" selected="selected">Response and Assertion</option>
						<option value="response">Response Only</option>
						<option value="assertion">Assertion Only</option>
					</select>
					</dd>
				</dl>

				<dl>
					<dt><label for="optionalProperties"><%=LanguageUtil.get(pageContext, "optional-properties-label")%></label></dt>
					<dd><textarea name="optionalProperties" id="optionalProperties" cols="50" rows="10"></textarea></dd>
				</dl>


				<dl>
					<dt><label for="addSite"><%= LanguageUtil.get(pageContext, "add-site") %></label></dt>
					<dd>
						<div>
							<%
								List<Host> allHosts = APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(),true);
							%>

							<select id="addSite" dojoType="dijit.form.FilteringSelect" autocomplete="true" onChange="" style="width: 200px">
								<%for (Host h: allHosts){
									if (!h.getIdentifier().equals(Host.SYSTEM_HOST) && h.isLive()) {
										%><option value="<%= h.getIdentifier() %> "><%= h.getHostname() %></option><%
									}
								}
							%>
							</select>

							<button dojoType="dijit.form.Button" onclick="addSite(mySitesMap, 'mySitesMap', 'addSite', 'siteListingTable');" type="button">
								<%= LanguageUtil.get(pageContext, "add-site-to-config") %>
							</button>
						</div>
					</dd>
				</dl>


				<!-- Placeholder table with sites already assigned to the current IdP configuration .. Not sure if
				a table is what we need here, though-->

				<table id="siteListingTable" class="listingTable">
					<thead id="siteTableHeader">
					<tr>
						<th><%=LanguageUtil.get(pageContext, "site")%></th>
						<th><%=LanguageUtil.get(pageContext, "id")%></th>
						<th><%=LanguageUtil.get(pageContext, "Actions")%></th>
					</tr>
					</thead>
					<tbody id="idpAssignedTableBody">
					</tbody>
				</table>
			</div>
			<div style="text-align: center">
				<button  dojoType="dijit.form.Button" onclick="saveIdp()" iconClass="uploadIcon">
					<%= LanguageUtil.get(pageContext, "save") %>
				</button>
			</div>
		</form>
	</div>
</div>

<div id="disableSamlSiteDialog" dojoType="dijit.Dialog" disableCloseButton="true" title="<%=LanguageUtil.get(pageContext, "disabled-sites-dialog-title")%>" style="display: none; width:600px">
	<div>
		<form id="disableSamlSiteForm" name="disableSamlSiteForm" dojoType="dijit.form.Form">
			<div class="form-inline">
				<dl>
					<dt><label for="addSite"><%= LanguageUtil.get(pageContext, "add-site") %></label></dt>
					<dd>
						<div>
							<%
								allHosts = APILocator.getHostAPI().findAll(APILocator.getUserAPI().getSystemUser(),true);
							%>

							<select id="addDisabledSite" dojoType="dijit.form.FilteringSelect" autocomplete="true" onChange="" style="width: 200px">
								<%for (Host h: allHosts){
									if (!h.getIdentifier().equals(Host.SYSTEM_HOST) && h.isLive()) {
										%><option value="<%= h.getIdentifier() %> "><%= h.getHostname() %></option><%
									}
								}
							%>
							</select>

							<button dojoType="dijit.form.Button" onclick="addSite(myDisabledSitesMap, 'myDisabledSitesMap', 'addDisabledSite', 'disabledSiteListingTable');" type="button">
								<%= LanguageUtil.get(pageContext, "add-site-to-config") %>
							</button>
						</div>
					</dd>
				</dl>

				<table id="disabledSiteListingTable" class="listingTable">
					<thead id="disabledSiteTableHeader">
					<tr>
						<th><%=LanguageUtil.get(pageContext, "site")%></th>
						<th><%=LanguageUtil.get(pageContext, "id")%></th>
						<th><%=LanguageUtil.get(pageContext, "Actions")%></th>
					</tr>
					</thead>
					<tbody id="disabledSiteTableBody">
					</tbody>
				</table>
			</div>
			<div style="text-align: center">
				<button  dojoType="dijit.form.Button" onclick="saveDisabledSites()" iconClass="uploadIcon">
					<%= LanguageUtil.get(pageContext, "save") %>
				</button>
			</div>
		</form>
	</div>
</div>