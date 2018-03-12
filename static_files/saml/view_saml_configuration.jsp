<%@page import="com.liferay.portal.language.LanguageUtil"%>

<script type="text/javascript" src="/html/plugins/plugin-com.dotcms.dotsaml/saml/view_saml_configuration_js_inc.jsp" ></script>

<script type="text/javascript">
    //dojo.require("dotcms.dijit.form.HostFolderFilteringSelect");

    require(["dojo/ready"], function(ready){
        ready(function(){
            renderIdpConfigs();
        });
    });

    function renderIdpConfigs() {
        //Node List
        var idpList;

        xhrArgs = {
            url: "/api/v1/dotsaml/idps",
            handleAs: "json",
            load: function (data) {
                idpList = data.entity;
                var idpsTableHTML = "";

                dojo.forEach(idpList, function (item, index) {

                    idpsTableHTML +=
                        "        <tr>" +
                        "            <td>" +
                        "                <i class='statusIcon green'></i>" +
                        "            </td>" +
                        "            <td>" + item.idpName + "</td>" +
                        "            <td>" +
                        "                <button dojoType='dijit.form.Button' onclick='idpAdmin.addEditIdp();' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "edit")%>" +
                        "                </button>" +
                        "                <button dojoType='dijit.form.Button' onclick='idpAdmin.deleteIdp(\"" + item.id +"\");' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "delete")%>" +
                        "                </button>" +
                        "                <button dojoType='dijit.form.Button' onclick='idpAdmin.setDefaultIdp();' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "set-as-default")%>" +
                        "                </button>" +
                        "            </td>" +
                        "            <td>" +
                        "                <button dojoType='dijit.form.Button' onclick='idpAdmin.downloadSPMedatadata();' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "download-sp-metadata")%>" +
                        "                </button>" +
                        "            </td>" +
                        "        </tr>";
                });

                idpsTableHTML += "<tr>" +
                    "                <td>" +
                    "                </td>" +
                    "                <td><%=LanguageUtil.get(pageContext, "disabled-sites")%></td>" +
                    "                <td>" +
                    "                    <button dojoType='dijit.form.Button' onclick='idpAdmin.disableSAMLPerSite();' class='dijitButtonFlat'>" +
                    "                        <%=LanguageUtil.get(pageContext, "disable-site")%>" +
                    "                    </button>" +
                    "                </td>" +
                    "                <td>" +
                    "                </td>" +
                    "            </tr>"

                dojo.empty(dojo.byId("idpTableBody"));
                dojo.place(idpsTableHTML, dojo.byId("idpTableBody"));
                dojo.parser.parse(dojo.byId("idpTableBody"))
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };

        deferred = dojo.xhrGet(xhrArgs);
    }

    function saveIdp() {
        var addEditIdPForm = dojo.byId("addEditIdPForm");

        var xhrArgs = {
            url: "/api/v1/dotsaml/idp",
            headers: { "Content-Type": false },
            postData: new FormData(addEditIdPForm),
            handleAs: "json",
            load: function (data) {
                dijit.byId('addEditIdPDialog').hide();
                renderIdpConfigs();
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrPost(xhrArgs);
    }

    function deleteIdpConfig(id) {
        xhrArgs = {
            url: "/api/v1/dotsaml/idp/" + id,
            handleAs: "json",
            load: function () {
                renderIdpConfigs();
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrDelete(xhrArgs);
    }

    require(['dojo/_base/declare'], function(declare){
        declare("dotcms.dijit.saml.IdPAdmin", null, {
            constructor: function(){
            },
            addEditIdp : function() {
                dijit.byId('addEditIdPDialog').show();
            },
            deleteIdp : function(id) {
                deleteIdpConfig(id);
            },
            setDefaultIdp : function() {
                window.alert("Set Default Here");
            },
            disableSAMLPerSite : function() {
                window.alert("Disable SAML Authentication here");
            },
            downloadSPMedatadata : function() {
                window.alert("This functionality will be available in the next sprint");
            }
        });
    });

    var idpAdmin = new dotcms.dijit.saml.IdPAdmin({});
</script>

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
        <form id="addEditIdPForm" name="addEditIdPForm" dojoType="dijit.form.Form">
            <div class="form-inline">
                <dl>
                    <dt><label for="id"><%=LanguageUtil.get(pageContext, "idp-id")%></label></dt>
                    <dd><input type="text" name="id" id="id" value="" dojoType="dijit.form.TextBox" readonly/></dd>
                </dl>
                <dl>
                    <dt><label for="idpName"><%=LanguageUtil.get(pageContext, "idp-config-name-label")%></label></dt>
                    <dd><input type="text" name="idpName" id="idpName" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><label><%=LanguageUtil.get(pageContext, "idp-status-label")%></label></dt>
                    <dd>
                        <input type="radio" name="enabled" value="true" id="enabledTrue" dojoType="dijit.form.RadioButton"/> <label for="enabledTrue">Yes</label>
                        <input type="radio" name="enabled" value="false" id="enabledFalse" dojoType="dijit.form.RadioButton"/> <label for="enabledFalse">No</label>
                    </dd>

                </dl>

                <dl>
                    <dt><label for="sPIssuerURL"><%=LanguageUtil.get(pageContext, "sp-issuer-url-label")%></label></dt>
                    <dd><input type="text" name="sPIssuerURL" id="sPIssuerURL" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><label for="sPEndponintHostname"><%=LanguageUtil.get(pageContext, "sp-endpoint-hostname-label")%></label></dt>
                    <dd><input type="text" name="sPEndponintHostname" id="sPEndponintHostname" required="true" onkeydown="" dojoType="dijit.form.TextBox" value="" /></dd>
                </dl>

                <dl>
                    <dt><label for="privateKey"><%=LanguageUtil.get(pageContext, "private-key-label")%></label></dt>
                    <dd><input type="file" id="privateKey" name="privateKey" required="true"></dd>
                </dl>

                <dl>
                    <dt><label for="publicCert"><%=LanguageUtil.get(pageContext, "public-certificate-label")%></label></dt>
                    <dd><input type="file" id="publicCert" name="publicCert" required></dd>
                </dl>

                <dl>
                    <dt><label for="idPMetadataFile"><%=LanguageUtil.get(pageContext, "idp-metadata-label")%></label></dt>
                    <dd><input type="file" id="idPMetadataFile" name="idPMetadataFile" required="true"></dd>
                </dl>

                <dl>
                    <dt><label for="optionalProperties"><%=LanguageUtil.get(pageContext, "optional-properties-label")%></label></dt>
                    <dd><input type="text" dojoType="dijit.form.TextBox" name="optionalProperties" id="optionalProperties" size="20" value="" /></dd>
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
                    <tbody id="idpAssignedTableBody">
                    </tbody>
                </table>
            </div>
            <div style="text-align: center">
                <button  dojoType="dijit.form.Button" onclick="saveIdp()" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "save") %>
                </button>
                <button  dojoType="dijit.form.Button" onClick="" iconClass="uploadIcon">
                    <%= LanguageUtil.get(pageContext, "cancel") %>
                </button>
            </div>
        </form>
    </div>
</div>