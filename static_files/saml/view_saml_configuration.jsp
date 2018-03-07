<%@ include file="/html/plugins/plugin-com.dotcms.dotsaml/saml/view_saml_configuration_js_inc.jsp" %>

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

   <!-- Empty table to be used when reading from configuration json is done

	<table class="listingTable idp-list">
		<thead id="idpPTableHeader"></thead>
		<tbody id="idpTableBody"></tbody>
		<tbody id="noResultsSection">
			<tr class="alternate_1" id="rowNoResults">
				<td colspan="5">
					<div class="noResultsMessage">
						<%=LanguageUtil.get(pageContext, "No-Results-Found")%>
					</div>
				</td>
			</tr>
		</tbody>
	</table>

	-->

	<!-- Placeholder table -->

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
            <tr>
                <td>
                    <i class="statusIcon green"></i>
                </td>
                <td>idp1.site.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "edit")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "delete")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "set-as-default")%>
                    </button>
                </td>
                <td>Link to SP Metadata coming soon</td>
            </tr>
            <tr>
                <td>
                    <i class="statusIcon green"></i>
                </td>
                <td>idp2.site.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "edit")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "delete")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "set-as-default")%>
                    </button>
                </td>
                <td>Link to SP Metadata coming soon</td>
            </tr>
            <tr>
                <td>
                    <i class="statusIcon green"></i>
                </td>
                <td>idp3.site.com</td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "Edit")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "Delete")%>
                    </button>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "Set as Defult")%>
                    </button>
                </td>
                <td>Link to SP Metadata coming soon</td>
            </tr>
            <tr>
                <td>
                </td>
                <td><%=LanguageUtil.get(pageContext, "disabled-sites")%></td>
                <td>
                    <button dojoType="dijit.form.Button" onclick="" class="dijitButtonFlat">
                        <%=LanguageUtil.get(pageContext, "disable-site")%>
                    </button>
                </td>
                <td>
                </td>
            </tr>
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