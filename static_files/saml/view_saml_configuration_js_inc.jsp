<%@page import="com.liferay.portal.language.LanguageUtil"%>
<%@page import="com.dotmarketing.util.UtilMethods"%>
<%response.setContentType("text/JavaScript");%>

    require(["dojo/ready"], function(ready){
        ready(function(){
            idpAdmin.renderIdpConfigs();
        });
    });

    function saveIdp() {

        if (document.getElementById ("addEditIdPForm").checkValidity()) {

            var addEditIdPForm = dojo.byId("addEditIdPForm");
            var formData = new FormData(addEditIdPForm);

            formData.append("signatureValidationType", dijit.byId("signatureValidationType").value);

            var jsonData = {};

            for (var key of mySitesMap.keys()) {
                var siteId = key;
                var siteName = mySitesMap.get(key);

                jsonData[siteId] = siteName;
            }

            formData.append("sites", JSON.stringify(jsonData));

            var xhrArgs = {
                url: "/api/v1/dotsaml/idp",
                headers: { "Content-Type": false },
                postData: formData,
                handleAs: "json",
                load: function (data) {
                    dijit.byId('addEditIdPDialog').hide();
                    idpAdmin.renderIdpConfigs();
                },
                error: function (error) {
                    alert("An unexpected error occurred: " + error);
                }
            };
            deferred = dojo.xhrPost(xhrArgs);
         } else {
            document.getElementById ("addEditIdPForm").reportValidity();
         }

    }

    function saveDisabledSites() {
        var formData = new FormData();
        var jsonData = {};

        for (var key of myDisabledSitesMap.keys()) {
            var siteId = key;
            var siteName = myDisabledSitesMap.get(key);

            jsonData[siteId] = siteName;
        }

        formData.append("disabledsites", JSON.stringify(jsonData));

        var xhrArgs = {
            url: "/api/v1/dotsaml/disabledsites",
            headers: { "Content-Type": false },
            postData: formData,
            handleAs: "json",
            load: function (data) {
                dijit.byId('disableSamlSiteDialog').hide();
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrPost(xhrArgs);
    }

    function setDefaultIdpConfig(id) {
        xhrArgs = {
            url: "/api/v1/dotsaml/default/" + id,
            handleAs: "json",
            load: function () {
                idpAdmin.renderIdpConfigs();
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrPost(xhrArgs);
    }

    function getDefaultIdpConfig(){
        var defaultIdp = "";

        xhrArgs = {
            url: "/api/v1/dotsaml/default",
            sync: true,
            handleAs: "json",
            load: function (data) {
                idp = data.entity;
                if(idp && idp.defaultSamlConfig){
                    defaultIdp = idp.defaultSamlConfig;
                }
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        }
        deferred = dojo.xhrGet(xhrArgs);

        return defaultIdp;
    }

    function deleteIdpConfig(id) {
        xhrArgs = {
            url: "/api/v1/dotsaml/idp/" + id,
            handleAs: "json",
            load: function () {
                idpAdmin.renderIdpConfigs();
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrDelete(xhrArgs);
    }

    function getDisabledSites() {
        xhrArgs = {
            url: "/api/v1/dotsaml/disabledsites/",
            handleAs: "json",
            load: function (data ) {
                var entity = data.entity;

                Object.keys(entity.disabledSamlSites).forEach(function(key,index) {
                    myDisabledSitesMap.set(key, entity.disabledSamlSites[key]);
                });
                drawTable(myDisabledSitesMap, "myDisabledSitesMap", "disabledSiteListingTable");
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrGet(xhrArgs);
    }

    function getDefaultIdpConfig(){
        var defaultIdp = "";

        xhrArgs = {
            url: "/api/v1/dotsaml/default",
            sync: true,
            handleAs: "json",
            load: function (data) {
                idp = data.entity;
                if(idp && idp.defaultSamlConfig){
                    defaultIdp = idp.defaultSamlConfig;
                }
            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        }
        deferred = dojo.xhrGet(xhrArgs);

        return defaultIdp;
    }

    function findIdpConfig(id) {
        xhrArgs = {
            url: "/api/v1/dotsaml/idp/" + id,
            handleAs: "json",
            load: function (data) {
                var idp = data.entity;

                dijit.byId('addEditIdPDialog').show();

                resetIdpConfig(mySitesMap, "siteListingTable");

                addEditIdPForm.elements["id"].value = idp.id;
                addEditIdPForm.elements["idpName"].value = idp.idpName;
                if (idp.enabled){
                    document.getElementById("enabledTrue").checked = true;
                } else {
                    document.getElementById("enabledFalse").checked = true;
                }

                addEditIdPForm.elements["sPIssuerURL"].value = idp.sPIssuerURL;
                addEditIdPForm.elements["sPEndponintHostname"].value = idp.sPEndponintHostname;

                dijit.byId("signatureValidationType").set("value", idp.signatureValidationType);

                if(idp.privateKey){
                    document.getElementById("privateKeySavedFile").innerText = idp.privateKey.replace(/^.*[\\\/]/, '');
                }
                if(idp.publicCert){
                    document.getElementById("publicCertSavedFile").innerText = idp.publicCert.replace(/^.*[\\\/]/, '');
                }
                if(idp.idPMetadataFile){
                    document.getElementById("idPMetadataSavedFile").innerText = idp.idPMetadataFile.replace(/^.*[\\\/]/, '');
                }

                var optionalPropertiesText = "";

                Object.keys(idp.optionalProperties).forEach(function(key,index) {
                    optionalPropertiesText += key + "=" + idp.optionalProperties[key]+ "\n"
                });

                Object.keys(idp.sites).forEach(function(key,index) {
                    mySitesMap.set(key, idp.sites[key]);
                });

                addEditIdPForm.elements["optionalProperties"].value = optionalPropertiesText;

                drawTable(mySitesMap, "mySitesMap", "siteListingTable");

            },
            error: function (error) {
                alert("An unexpected error occurred: " + error);
            }
        };
        deferred = dojo.xhrGet(xhrArgs);
    }

    var mySitesMap = new Map();
    var myDisabledSitesMap = new Map();

    function addSite(sitesMap, sitesMapName, selectId, tableId){
        var siteId = dijit.byId(selectId).value;
        var siteName = dijit.byId(selectId).attr('displayedValue');

        sitesMap.set(siteId, siteName);
        drawTable(sitesMap, sitesMapName,  tableId);
    }

    function deleteSite(id, sitesMap, sitesMapName, tableId) {
        sitesMap.delete(id);
        drawTable(sitesMap, sitesMapName, tableId);
    }

    function resetTable(tableId) {
        var tableRows = document.getElementById(tableId).rows.length;
        if (tableRows > 1) {
            for (i = 1; i < tableRows; i++) {
                document.getElementById(tableId).deleteRow(i);
            }
        }
    }

    function drawTable(sitesMap, sitesMapName, tableId){
        resetTable(tableId);

        for (var key of sitesMap.keys()) {

            var table = document.getElementById(tableId);
            var row = table.insertRow(1); // -1 at the end.
            var cell1 = row.insertCell(0);
            var cell2 = row.insertCell(1);
            var cell3 = row.insertCell(2);

            var siteId = key;
            var siteName = sitesMap.get(key);

            cell1.innerHTML = siteName;
            cell2.innerHTML = siteId;
            cell3.innerHTML = '<a href="javascript:deleteSite(\''+ siteId + '\', '+ sitesMapName +',\''+ sitesMapName +'\', \''+tableId+'\');"><span class="deleteIcon"></span></a>';
        }
    }

    function resetIdpConfig(sitesMap, tableId) {
        dojo.byId("addEditIdPForm").reset();

        document.getElementById("privateKeySavedFile").innerText = "";
        document.getElementById("publicCertSavedFile").innerText = "";
        document.getElementById("idPMetadataSavedFile").innerText = "";

        document.getElementById("privateKey").value = "";
        document.getElementById("publicCert").value = "";
        document.getElementById("idPMetadataFile").value = "";

        document.getElementById("optionalProperties").value = "";

        sitesMap.clear();
        resetTable(tableId);
    }

    require(['dojo/_base/declare'], function(declare){
        declare("dotcms.dijit.saml.IdPAdmin", null, {
            constructor: function(){
                this.currentPage = 1;
                this.RESULTS_PER_PAGE = 10;
                this.total = 0;
                this.filter = "";
            },

            tableResultSummaryTemplate: '<%= LanguageUtil.get(pageContext, "Viewing-Results") %> {startRecord} <%= LanguageUtil.get(pageContext, "to") %> \
            {endRecord} <%= LanguageUtil.get(pageContext, "of") %> {total}',

            renderPagination: function (total) {
                //Rendering the results summary bottom section of the table
                var startRecord = (this.currentPage - 1) * this.RESULTS_PER_PAGE + 1;
                if (startRecord > total)
                    startRecord = total;
                var endRecord = startRecord + this.RESULTS_PER_PAGE - 1;
                if (endRecord > total)
                    endRecord = total;

                var summaryHTML = dojo.replace(this.tableResultSummaryTemplate, {
                    startRecord: startRecord,
                    endRecord: endRecord,
                    total: total
                });
                dojo.byId('resultsSummary').innerHTML = summaryHTML;
                //Rendering the next and previous buttons
                dojo.style('buttonNextResultsWrapper', {
                    visibility: 'hidden'
                });
                dojo.style('buttonPreviousResultsWrapper', {
                    visibility: 'hidden'
                });
                if (endRecord < total)
                    dojo.style('buttonNextResultsWrapper', {
                        visibility: 'visible'
                    });
                if (startRecord > 1)
                    dojo.style('buttonPreviousResultsWrapper', {
                        visibility: 'visible'
                    });

                if (total == 0)
                    dojo.style('resultsSummary', {
                        visibility: 'hidden'
                    })
                else
                    dojo.style('resultsSummary', {
                        visibility: 'visible'
                    })
            },

            addIdp : function() {
                dijit.byId('addEditIdPDialog').show();
                resetIdpConfig(mySitesMap, "siteListingTable");
            },
            editIdp : function(id) {
                findIdpConfig(id);
            },
            deleteIdp : function(id) {
                if(confirm('<%= UtilMethods.escapeSingleQuotes(LanguageUtil.get(pageContext, "delete-dialog-text"))%>')) {
                    deleteIdpConfig(id);
                }
            },
            setDefaultIdp : function(id) {
                setDefaultIdpConfig(id);
            },
            disableSAMLPerSite : function() {
                dijit.byId('disableSamlSiteDialog').show();
                getDisabledSites();
            },
            downloadSPMedatadata : function() {
                window.alert("This functionality will be available in the next sprint");
            },
            gotoNextPage: function(){
                this.currentPage++;
                this.renderIdpConfigs();
            },
            gotoPreviousPage: function(){
                this.currentPage--;
                this.renderIdpConfigs();
            },
            searchIdpByName: function(){
                this.filter = document.getElementById("filter").value;
                this.renderIdpConfigs();
            },
            resetSearch: function(){
                document.getElementById("filter").value = "";
                this.filter = "";
                this.renderIdpConfigs();
            },
            renderIdpConfigs : function() {
                //Node List
                var idpList;
                var defaultIdp = getDefaultIdpConfig();

                xhrArgs = {
                    url: "/api/v1/dotsaml/idps",
                    content: {
                        page: this.currentPage,
                        filter: this.filter,
                    },
                    handleAs: "json",
                    load: function (data) {
                        idpList = data.entity;
                        var idpsTableHTML = "";
                        var idpStatusColor = "";

                        dojo.forEach(idpList, function (item, index) {

                            if (item.enabled) {
                                idpStatusColor = "liveIcon";
                            } else {
                                idpStatusColor = "archivedIcon";
                            }

                            var defaultButtonHTML = "<button dojoType='dijit.form.Button' onclick='idpAdmin.setDefaultIdp(\"" + item.id +"\");' class='dijitButtonFlat'>" +
                                "                        <%=LanguageUtil.get(pageContext, "set-as-default")%>" +
                                "                    </button>";

                            if(defaultIdp && defaultIdp == item.id){
                                defaultButtonHTML = "";
                            }

                            var defaultTranslation = ""

                            if(defaultIdp && defaultIdp == item.id){
                                defaultTranslation = "<b>(Default)</b>";
                            }

                            idpsTableHTML +=
                                "        <tr>" +
                                "            <td>" +
                                "               <span class='"+ idpStatusColor + "'></span>" +
                                "            </td>" +
                                "            <td>" + item.idpName + defaultTranslation + "</td>" +
                                "            <td>" +
                                "                <button dojoType='dijit.form.Button' onclick='idpAdmin.editIdp(\"" + item.id + "\");' class='dijitButtonFlat'>" +
                                "                    <%=LanguageUtil.get(pageContext, "edit")%>" +
                                "                </button>" +
                                "                <button dojoType='dijit.form.Button' onclick='idpAdmin.deleteIdp(\"" + item.id +"\");' class='dijitButtonFlat'>" +
                                "                    <%=LanguageUtil.get(pageContext, "delete")%>" +
                                "                </button>" +
                                defaultButtonHTML +
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
                deferred.then(function (response) {
                    var total = deferred.ioArgs.xhr.getResponseHeader("X-Pagination-Total-Entries");
                    idpAdmin.renderPagination(total);
                });

            }
        });
    });

    var idpAdmin = new dotcms.dijit.saml.IdPAdmin({});
