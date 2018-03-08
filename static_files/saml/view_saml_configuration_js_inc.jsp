<%@page import="com.liferay.portal.language.LanguageUtil"%>
<%response.setContentType("text/JavaScript");%>
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
            url: "/api/v1/saml/idps",
            handleAs: "json",
            sync: true,
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
                        "                <button dojoType='dijit.form.Button' onclick='idpAdmin.deleteIdp();' class='dijitButtonFlat'>" +
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

    require(['dojo/_base/declare'], function(declare){
      declare("dotcms.dijit.saml.IdPAdmin", null, {
        constructor: function(){
        },
        addEditIdp : function() {
           dijit.byId('addEditIdPDialog').show();
        },
        deleteIdp : function() {
            window.alert("Delete Here");
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