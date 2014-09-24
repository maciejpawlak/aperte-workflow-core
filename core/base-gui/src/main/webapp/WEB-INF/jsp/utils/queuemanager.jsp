<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<script type="text/javascript">

  	var queueViewManager = new QueueViewManager();
	
	function QueueView(tableObject, viewName)
	{
		this.tableObject = tableObject;
		this.viewName = viewName;
	}
	
	function QueueViewManager()
	{
		this.views = {};
		
		this.defaultQueueId = '';
		this.defaultOwnerLogin = '';
		
		this.currentQueue = '';
		this.currentOwnerLogin = '${aperteUser.login}';
		
		this.loadQueue = function(queueId, ownerLogin)
		{
			this.removeCurrentQueue();
				
			this.currentQueue = queueId;
			this.currentOwnerLogin = ownerLogin;
			
			
			
			windowManager.showLoadingScreen();
			//windowManager.changeUrl('?queueId='+queueId);

			var widgetJson = $.post('<portlet:resourceURL id="loadQueue"/>',
			{
				"queueId": queueId,
				"ownerLogin": ownerLogin
			})
			.done(function(data) 
			{
				clearAlerts();
				windowManager.showProcessList(data);
				
				checkIfViewIsLoaded();
			})
			.fail(function(data, textStatus, errorThrown) {
				
			});
		}
		
		this.reloadCurrentQueue = function()
		{
			if(this.currentQueue == '')
			{
				this.currentQueue = this.defaultQueueId;
				this.currentOwnerLogin = this.defaultOwnerLogin;
			}
			this.loadQueue(this.currentQueue, this.currentOwnerLogin);
		}
		
		this.addTableView = function(queueId, tableObject, viewName)
		{
			this.views[queueId] = new QueueView(tableObject, viewName);
		}
		
		this.removeQueue = function(queueId)
		{
			var queue = this.views[queueId];
			if(queue)
			{
				queue.tableObject.dataTable.fnDestroy();
			}	

		}
		
		this.removeCurrentQueue = function()
		{

			if(this.currentQueue && this.currentQueue != '')
			{
				//this.removeQueue(this.currentQueue);
			}
		}
		
		this.toggleColumn = function(viewName, columnName)
		{
			this.views[viewName].tableObject.toggleColumn(columnName);
		}
		
		this.enableMobileMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.enableMobileMode();
				}
			});
		}
		
		this.enableTabletMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.enableTabletMode();
				}
			});
		}
		
		this.disableMobileMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.disableMobileMode();
				}
			});
		}
		
		this.disableTabletMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.disableTabletMode();
				}
			});
		}
	}

    function claimTaskFromQueue(button, queueName, processStateConfigurationId, taskId)
    {
        $(button).prop('disabled', true);
        windowManager.showLoadingScreen();

        var bpmJson = $.post(claimTaskFromQueuePortlet,
        {
            "queueName": queueName,
            "taskId": taskId,
            "userId": queueViewManager.currentOwnerLogin
        }, function(task)
        {
            clearAlerts();
            reloadQueues();
            loadProcessView(task.taskId);
        })
        .fail(function(request, status, error)
        {

        });
    }
</script>