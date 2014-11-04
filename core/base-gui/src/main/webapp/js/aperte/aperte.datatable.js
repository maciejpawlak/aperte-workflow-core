	function AperteDataTable(tableId, columnDefs, sortingOrder)
	{
		this.tableId = tableId;
		this.requestUrl = '';
		this.columnDefs = columnDefs;
		this.sortingOrder = sortingOrder;
		this.dataTable;
		this.requestParameters = [];
		this.firstRow;

		this.initialized = false;

		this.setParameters = function(parameters)
		{
           this.requestParameters =  parameters;
		}

		this.addParameter = function(name, value)
		{
			this.requestParameters.push({ "name": name, "value": value });
		}

		this.clearState = function()
		{
		    this.dataTable.state.clear();
		    this.dataTable.page('first');
		}


		this.reloadTable = function(requestUrl)
		{
			$.each(this.requestParameters, function (index, parameter)
			{
				requestUrl += portletNamespace + parameter["name"] + "=" + parameter["value"];
			});

			this.requestUrl = requestUrl;
			if(this.initialized == false)
			{
				this.createDataTable();
				this.initialized = true;
			}
			else
			{
				this.dataTable.ajax.url(requestUrl).load(null, false);
			}
		}

		this.enableMobileMode = function()
		{
		}

		this.enableTabletMode = function()
		{
		}

		this.disableMobileMode = function()
		{
		}

		this.disableTabletMode = function()
		{
		}

		this.createDataTable = function(tableElementsPlacement)
		{
		    var sDom = (tableElementsPlacement !== undefined) ? tableElementsPlacement : 'R<"top"t><"bottom"plr>';

		    var dataTableOtions =
		    {
                            serverSide: true,
                            ordering: true,
                            lengthChange: true,
                            stateSave: true,
                            dom: sDom,
                            processing: true,
                            order: sortingOrder,
            				ajax: {
                                    dataType: 'json',
                                    type: "POST",
                                    url: this.requestUrl
                                },
            				columns: this.columnDefs,
            				language: dataTableLanguage
                        };

            if(this.firstRow)
            {
                dataTableOtions["displayStart"] = this.firstRow;
            }

			this.dataTable = $('#'+this.tableId).DataTable(dataTableOtions);
			if(typeof windowManager != 'undefined')
			{
				if(windowManager.mobileMode == true)
				{
					this.enableMobileMode();
				}

				if(windowManager.tabletMode == true)
				{
					this.enableTabletMode();
				}
			}
		}

		this.toggleColumnButton = function(columnName, active)
		{
			var checkbox = $("#button-"+this.tableId+'-'+columnName);
			checkbox.trigger('click');
		}

		this.toggleColumn = function(columnName)
		{
			var dataTable = this.dataTable;
			$.each(dataTable.fnSettings().aoColumns, function (columnIndex, column)
			{
				if (column.sName == columnName)
				{
					  dataTable.fnSetColumnVis(columnIndex, column.bVisible ? false : true, false);
				}
		    });
		}
	}