<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<!-- JQuery -->
<script src="../static/jquery/jquery-1.10.2.min.js"	type="text/javascript"></script>
<script src="../static/jquery/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<link href="../static/jquery/jquery-ui-1.10.3.custom.min.css" rel="stylesheet" type="text/css" />

<!-- Bootstrap core CSS -->
<link href="../static/bootstrap/css/bootstrap.css" rel="stylesheet"/>
<link href="../static/bootstrap/css/docs.css" rel="stylesheet"/>

<!-- JTable -->
<link href="../static/jtable/themes/lightcolor/gray/jtable.css" rel="stylesheet" type="text/css" />
<script src="../static/jtable/jquery.jtable.min.js"	type="text/javascript"></script>
 
<!-- Custom styles for this template -->
<link href="../static/css/fancylog.css" rel="stylesheet"/>

<style type="text/css" id="holderjs-style"></style>

<script type="text/javascript">
	$(document).ready(function() {
		
		$(".date-picker").datepicker({
			minDate : -10,
			maxDate : "-1D",
			autoclose: true
		});

		//Prepare jtable plugin
        $('#ExceptionCountContainer').jtable({
            title: ' Fancy logs',
            //paging: true,
            //sorting: true,
            //defaultSorting: 'date ASC',
            selecting: true, //Enable selecting
            //multiselect: true, //Allow multiple selecting
            //selectingCheckboxes: true, //Show checkboxes on first column
            selectOnRowClick: true, //Enable this to only select using checkboxes
            actions: {
                listAction: '../config/exceptions'
            },
            fields: {
            	className: {
                    title: 'Class Name',
                    width: '8%'
                    
                },
                exception: {
                    title: 'Exception Description',
                    width: '50%'
                },
                count: {
                	title: 'Count',
                	width: '2%'
                }
            },
            //Register to selectionChanged event to hanlde events
            selectionChanged: function () {
                //Get all selected rows
                var $selectedRows = $('#ExceptionCountContainer').jtable('selectedRows');
                $('#SelectedRowList').empty();
                if ($selectedRows.length > 0) {
                    //Show selected rows
                    $selectedRows.each(function () {
                        var record = $(this).data('record');
                        var NWin = window.open("../view/exceptionpopup.jsp?ObjectId="+record.objectId,"popupWindow",'height=768,width=1366,left=10,top=10,titlebar=no,toolbar=no,menubar=no,location=no,directories=no,status=no');
                        if (window.focus)
                        {
                              NWin.focus();
                        }
                    });
                }
            }
        });
	
		$('#date-picker-2').change(function(e) {
			//e.preventDefault();
			$('#ExceptionCountContainer').jtable('load', {
				date : $('#date-picker-2').val()
			});
		}); 
	});
</script>

<title>Exceptions</title>
</head>
<body>
	<%@include file="jspf/header.jspf"%>
	<div class="bs-example page-fancylog">
		<%-- <%@include file="jspf/exceptionmenu.jspf"%> --%>
		<nav class="navbar navbar-default" role="navigation">
		<div class="control-group">
        	<label for="date-picker-2" class="control-label">Select Date</label>
	        <div class="controls">
	            <div id="datepicker" class="input-group">
	                <input id="date-picker-2" name="date-picker-2" type="text" class="date-picker form-control" placeholder="MM/DD/YYYY"/>
	                <label for="date-picker-2" class="input-group-addon btn"><span class="glyphicon glyphicon-calendar"></span>
	                </label>
	            </div>
	        </div>
    	</div>
    	</nav>
    	<div id="ExceptionCountContainer"></div>
    </div>
</body>
</html>