<!-- Aperte Workflow Substitution Manager -->
<!-- @author: mpawlak@bluesoft.net.pl, mkrol@bluesoft.net.pl -->

<%@ page
	import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp"%>
<%@include file="../../utils/apertedatatable.jsp"%>
<%@include file="../../actionsList.jsp"%>

<c:set var="isPermitted" scope="session"
	value="${aperteUser.hasRole('Administrator')}" />

<form id="NewSubstitution">
	<!-- Modal -->
	<div class="modal fade" id="NewSubstitutionModal" tabindex="-1"
		role="dialog" aria-labelledby="categoryModalLabel" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title" id="categoryModalLabel">
						<spring:message code="admin.substitution.modal.add.title" />
					</h4>
				</div>
				<div class="modal-body">
					<div class="form-horizontal" role="form">
						<div class="control-group">
							<label name="tooltip"
								title="<spring:message code='substituting.user.label.tooltip' />"
								for="UserLogin" class="col-sm-2 control-label"><spring:message
									code="substituting.user.label" /></label>
							<div class="controls">
								<input style="width: 215px" type="hidden" name="UserLogin"
									id="UserLogin" class="form-control select2"
									data-placeholder="<spring:message code='substituting.user.input.placeholder' />" />
							</div>
						</div>
						<div class="control-group">
							<label name="tooltip"
								title="<spring:message code='substitute.user.label.tooltip' />"
								for="UserSubstituteLogin" class="col-sm-2 control-label"><spring:message
									code="substitute.user.label" /></label>
							<div class="controls">
								<input style="width: 215px" type="hidden"
									name="UserSubstituteLogin" id="UserSubstituteLogin"
									class="form-control select2 required"
									data-placeholder="<spring:message code='substituting.user.input.placeholder' />" />
							</div>
						</div>
						<div class="control-group">
							<label name="tooltip"
								title="<spring:message code='substituting.date.from.tooltip' />"
								for="SubstitutingDateFrom" class="col-sm-2 control-label"><spring:message
									code="substituting.date.from.label" /></label>
							<div class="controls input-append date"
								id="SubstitutingDateFromPicker" data-date-format="yyyy-mm-dd">
								<input style="width: 100%" name="SubstitutingDateFrom"
									id="SubstitutingDateFrom" class="span2 form-control required"
									size="16" type="text"> <span class="add-on"><i
									class="icon-th"></i></span>
							</div>
						</div>
						<div class="control-group">
							<label name="tooltip"
								title="<spring:message code='substituting.date.to.tooltip' />"
								for="SubstitutingDateTo" class="col-sm-2 control-label"><spring:message
									code="substituting.date.to.label" /></label>
							<div class="controls input-append date"
								id="SubstitutingDateToPicker" data-date-format="yyyy-mm-dd">
								<input style="width: 100%" name="SubstitutingDateTo"
									id="SubstitutingDateTo" class="span2 form-control required"
									size="16" type="text"> <span class="add-on"><i
									class="icon-th"></i></span>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button type="reset" class="btn btn-default" data-dismiss="modal"><spring:message
									code="button.cancel" /></button>
					<button type="submit" class="btn btn-primary"><spring:message
									code="admin.substitution.modal.action.submit" /></button>
				</div>
			</div>
			<!-- /.modal-content -->
		</div>
		<!-- /.modal-dialog -->
	</div>
	<!-- /.modal -->
</form>

<div class="process-queue-name apw_highlight">
	Aperte Workflow Substitution Manager
	<div class="btn-group  pull-right">
		<button class="btn btn-info" id="substitution-add-button"
			data-toggle="modal" data-target="#NewSubstitutionModal"
			data-original-title="" title="">
			<span class="glyphicon glyphicon-plus"></span>
			<spring:message code="admin.substitution.action.add" />
		</button>
	</div>
</div>

<div class="process-tasks-view" id="task-view-processes">
	<table id="substitutionTable" class="process-table table table-striped"
		border="1">
		<thead>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.substituted" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.substituting" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.dateFrom" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.dateTo" /></th>
			<c:if test="${isPermitted}">
				<th style="width: 20%;"><spring:message
						code="admin.substitution.table.action" /></th>
			</c:if>
		</thead>
		<tbody></tbody>
	</table>
</div>


<script type="text/javascript">
	function editSubstitution(id) {
	}
	
	function removeSubstitution(id) {
		$.ajax({
			url : dispatcherPortlet,
			type : "POST",
			data : {
				controller : 'substitutionController',
				action : 'deleteSubstitution',
				substitutionId : id
			}
		}).done(function(resp) {
			dataTable.reloadTable(dispatcherPortlet);
		});
	}

	function validateSubstitution() {
		clearAlerts();
		
		isValid=true;

		if ($("#SubstitutingDateFrom").val() == "") {
			addAlert('<spring:message code="admin.substitution.alert.required.dateFrom" />');
			isValid=false;
		}
		
		if ($("#SubstitutingDateTo").val() == "") {
			addAlert('<spring:message code="admin.substitution.alert.required.dateTo" />');
			isValid=false;
		}
		
		if ($("#UserLogin").val() == "") {
			addAlert('<spring:message code="admin.substitution.alert.required.UserLogin" />');
			isValid=false;
		}
		
		if ($("#UserSubstituteLogin").val() == "") {
			addAlert('<spring:message code="admin.substitution.alert.required.UserSubstituteLogin" />');
			isValid=false;
		}

		if (new Date($("#SubstitutingDateFrom").val()) > new Date($(
				"#SubstitutingDateTo").val())) {
			addAlert('<spring:message code="admin.substitution.alert.invalid.date" />');
			isValid=false;
		}

		return isValid;
	}
	
	function onCancel(e)
	{
		e.preventDefault();
		
		$("#NewSubstitution")[0].reset();
		$("#NewSubstitution input.select2").select2("val", "");
	}

	function onNewSubstitution(e) {
		e.preventDefault();

		if (!validateSubstitution())
			return;

		var form = this;
		var postData = $(form).serializeObject();
		postData.controller = 'substitutionController'
		postData.action = 'addSubstitution'
		var formUrl = dispatcherPortlet
		$.ajax({
			url : dispatcherPortlet,
			type : "POST",
			data : postData,
			success : function(data, textStatus, jqXHR) {
			}
		}).done(function(resp) {
			$("#NewSubstitutionModal").modal("hide");
			form.reset();
			$("#NewSubstitution input.select2").select2("val", "");
			dataTable.reloadTable(dispatcherPortlet)
		});
	}
	
	var usersSelector =
	{
		 ajax: {
             url: dispatcherPortlet,
             dataType: 'json',
             quietMillis: 200,
             data: function (term, page) {
                 return {
                     q: term, // search term
                     page_limit: 10,
                     controller: "usercontroller",
                     page: page,
                     action: "getAllUsers"
                 };
             },
             results: function (data, page)
             {
                 var results = [];
                   $.each(data.data, function(index, item)
                   {
                     if('${user.getLogin()}' != item.login)
                     {
                         results.push({
                           id: item.login,
                           text: item.realName + ' ['+item.login+']'
                         });
                     }
                   });
                   return {
                       results: results
                   };
             }
         },
         initSelection: function(element, callback)
         {
             var id=$(element).val();
             if(id != "")
             {
                 var data = {id:'${substitutingUserLogin}',text:'${substitutingUserLoginFullName}'};
                 callback(data);
             }
         }
	}
	

	$(document)
			.ready(
					function() {
						$("#SubstitutingDateFromPicker").datepicker().on(
								'changeDate',
								function(ev) {
									$("#SubstitutingDateFromPicker")
											.datepicker("hide")
								});
						$("#SubstitutingDateToPicker").datepicker().on(
								'changeDate',
								function(ev) {
									$("#SubstitutingDateToPicker").datepicker(
											"hide")
								});

						dataTable = new AperteDataTable(
								"substitutionTable",
								[
										{
											"sName" : "userLogin",
											"bSortable" : true,
											"mData" : "userLogin"
										},
										{
											"sName" : "userSubstituteLogin",
											"bSortable" : true,
											"mData" : "userSubstituteLogin"
										},
										{
											"sName" : "dateFrom",
											"bSortable" : true,
											"mData" : function(object) {
												return $.format.date(
														object.dateFrom,
														'dd-MM-yyyy, HH:mm:ss');
											}
										},
										{
											"sName" : "dateTo",
											"bSortable" : true,
											"mData" : function(object) {
												return $.format.date(
														object.dateTo,
														'dd-MM-yyyy, HH:mm:ss');
											}
										},
										<c:if test="${isPermitted}">
										{
											"sName" : "action",
											"bSortable" : true,
											"mData" : function(o) {
												out = '<button class="btn btn-mini" onclick="editSubstitution('+o.id+')" data-toggle="modal" data-target="#NewSubstitutionModal">';
												out += '<i class="icon-edit"></i></button>';
												out += '<button class="btn btn-danger btn-mini" onclick="removeSubstitution('+o.id+')">';
												out += '<i class="icon-trash"></i></button>';
												return out;
											}
										}</c:if> ], [ [ 3, "desc" ] ]);

						dataTable.addParameter("controller",
								"substitutionController");
						dataTable.addParameter("action", "loadSubstitutions");
						dataTable.reloadTable(dispatcherPortlet);

						$("#NewSubstitution").submit(onNewSubstitution);
						$("#NewSubstitution button[type=reset]").click(onCancel);
						$("#UserLogin").select2(usersSelector);
						$("#UserSubstituteLogin").select2(usersSelector);
					});
</script>