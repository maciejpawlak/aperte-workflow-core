﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<div class="process-tasks-view" id="task-view-processes" hidden="true">
	<table id="processesTable" class="process-table table table-striped" border="1">
		<thead>
				<th style="width:10%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.step" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creator" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.assignee" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.clientType" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creationdate" /></th>
                <th style="width:10%;"><spring:message code="processes.list.table.process.title" /></th>
		</thead>
		<tbody></tbody>
	</table>
</div>


<script type="text/javascript">
//<![CDATA[

  	$(document).ready(function()
	{
		var loadedProcess = loadCookies("process");
		var parsedProcess = parseCookie(loadedProcess);
		
		var dataTable = new AperteDataTable("processesTable", 
			[
				 { "sName":"name", "bSortable": true ,"bVisible":parsedProcess.name, "mData": function(object){return generateNameColumn(object);}},
				 { "sName":"step", "bSortable": true ,"bVisible":parsedProcess.step, "mData": "step" },
				 { "sName":"creator", "bSortable": true ,"bVisible":parsedProcess.creator,"mData": "creator" },
				 { "sName":"assignee", "bSortable": true ,"bVisible":parsedProcess.assignee,"mData": function(object){return generateAssigneColumn(object);} },
				 { "sName":"clientType","bVisible":true ,"bVisible":parsedProcess.clientType, "bSortable": true,"mData": function(object){ if(object.highlight) return '<font color="red">'+object.clientType+'<span class="glyphicon glyphicon-exclamation-sign" style="margin-left: 5px" /></font>'; else return object.clientType;}},
				 { "sName":"creationDate", "bSortable": true ,"bVisible":parsedProcess.creationDate,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yy, HH:mm:ss');}},
				 { "sName":"title", "bSortable": true ,"bVisible":parsedProcess.title, "mData": "title"}
			 ],
			 [[ 4, "desc" ],[ 5, "desc" ]]
			);

		dataTable.addParameter('taskListViewName','LOTComplaintView');

		dataTable.enableMobileMode = function()
		{
			this.toggleColumnButton("deadline", false);
			this.toggleColumnButton("assignee", false);
			this.toggleColumnButton("creationDate", false);
		}

		dataTable.enableTabletMode = function()
		{
			this.toggleColumnButton("creator", false);
		}
		
		dataTable.disableMobileMode = function()
		{
			this.toggleColumnButton("deadline", true);
			this.toggleColumnButton("assignee", true);
			this.toggleColumnButton("creationDate", true);
		}
		
		dataTable.disableTabletMode = function()
		{
			this.toggleColumnButton("creator", true);
		}
		
		queueViewManager.addTableView('process', dataTable, 'task-view-processes');
	});
	
	function generateStatusColumn(task)
	{
		if(!task.businessStatus)
		{
			return "";
		}
		else
		{
			return task.businessStatus;
		}
	}
	
	function generateAssigneColumn(task)
	{
		if(!task.assignee)
		{
			return generateButtons(task);
		}
		else
		{
			return task.assignee;
		}
	}


	function generateNameColumn(task)
	{
		var showOnClickCode = 'onclick="loadProcessView('+task.taskId+')"';
		
	    var linkBody = '<a class="process-view-link"  '+showOnClickCode+' >' + task.processName + '</a>';

        return linkBody;
    }

	function loadProcessView(taskId)
	{
		windowManager.changeUrl('?taskId='+taskId);
		windowManager.showLoadingScreen();

		var widgetJson = $.post('<portlet:resourceURL id="loadTask"/>',
		{
			"taskId": taskId
		})
		.done(function(data) 
		{
			clearAlerts();
			windowManager.showProcessData();
			$('#process-data-view').empty();
			$("#process-data-view").append(data);
			checkIfViewIsLoaded();
		})
		.fail(function(data, textStatus, errorThrown) {
			
		});
	}
//]]>
</script>