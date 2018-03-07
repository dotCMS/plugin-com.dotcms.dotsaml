<%@ include file="/html/plugins/plugin-com.dotcms.dotsaml/saml/view_saml_configuration_js_inc.jsp" %>

<script type="text/javascript">

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
                        "                <button dojoType='dijit.form.Button' onclick='' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "edit")%>" +
                        "                </button>" +
                        "                <button dojoType='dijit.form.Button' onclick='' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "delete")%>" +
                        "                </button>" +
                        "                <button dojoType='dijit.form.Button' onclick='' class='dijitButtonFlat'>" +
                        "                    <%=LanguageUtil.get(pageContext, "set-as-default")%>" +
                        "                </button>" +
                        "            </td>" +
                        "            <td>Link to SP Metadata coming soon</td>" +
                        "        </tr>";
                });

                idpsTableHTML += "<tr>" +
                    "                <td>" +
                    "                </td>" +
                    "                <td><%=LanguageUtil.get(pageContext, "disabled-sites")%></td>" +
                    "                <td>" +
                    "                    <button dojoType='dijit.form.Button' onclick='' class='dijitButtonFlat'>" +
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
		    <button dojoType="dijit.form.Button" onClick="" iconClass="plusIcon">
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