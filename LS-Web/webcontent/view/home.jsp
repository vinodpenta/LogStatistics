
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<!-- JQuery -->
<script src="../static/jquery/jquery-1.10.2.min.js" type="text/javascript"></script>
<script src="../static/jquery/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<link href="../static/jquery/jquery-ui-1.10.3.custom.min.css" rel="stylesheet" type="text/css" />

<!-- Bootstrap core CSS -->
<link href="../static/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../static/bootstrap/css/docs.css" rel="stylesheet">

<!-- JTable -->
<link href="../static/jtable/themes/lightcolor/gray/jtable.css" rel="stylesheet" type="text/css" />
<script src="../static/jtable/jquery.jtable.min.js" type="text/javascript"></script>


      
<!-- Custom styles for this template -->
<link href="../static/css/fancylog.css" rel="stylesheet">

<style type="text/css" id="holderjs-style"></style>

<title>Welcome</title>
		<script type="text/javascript">
 
    $(document).ready(function () {
 
        //Prepare jtable plugin
        $('#FancyLogContainer').jtable({
            title: ' Fancy logs',
            paging: true,
            defaultSorting: 'Date ASC',
            selecting: true, //Enable selecting
            multiselect: true, //Allow multiple selecting
            selectingCheckboxes: true, //Show checkboxes on first column
            //selectOnRowClick: false, //Enable this to only select using checkboxes
            actions: {
                listAction: '../config/searchLogs',
            },
            fields: {
                sessionId: {
                    key: true,
                    edit: false,
                    list: false
                },
                date: {
                    title: 'Record Date',
                    width: '20%'
                },
                serviceName: {
                    title: 'Service name',
                    width: '20%'
                },
                host: {
                	title: 'Host'
                },
                market: {
                	title: 'Market'
                },
                channel: {
                	title:'Channel'
                },
                errorCode: {
                    title: 'Error Code'
                },
                error: {
                    title: 'Error Description(If any)'
                },
                view: {
                	title: '',
                	width: '1%',
					display : function(data) {
						    	return '<a href="../config/searchLogs/logs?id='+data.record.sessionId+'"><span class="glyphicon glyphicon-open"></span></a>';
						    }
                },
                download: {
                	title: '',
                	width: '1%',
					display : function(data) {
					    	return '<a href="../config/searchLogs/download?id='+data.record.sessionId+'"><span class="glyphicon glyphicon-save"></span></a>';
					    }
                }
            }/* ,
            //Register to selectionChanged event to hanlde events
            selectionChanged: function () {
                //Get all selected rows
                var $selectedRows = $('#FancyLogContainer').jtable('selectedRows');
 
                $('#SelectedRowList').empty();
                if ($selectedRows.length > 0) {
                    //Show selected rows
                    $selectedRows.each(function () {
                        var record = $(this).data('record');
                        $('#SelectedRowList').append(
                            '<b>StudentId</b>: ' + record.StudentId +
                            '<br /><b>Name</b>:' + record.Name + '<br /><br />'
                            );
                    });
                } else {
                    //No rows selected
                    $('#SelectedRowList').append('No row selected! Select rows to see here...');
                }
            },
            rowInserted: function (event, data) {
                if (data.record.Name.indexOf('Andrew') >= 0) {
                    $('#FancyLogContainer').jtable('selectRows', data.row);
                }
            } */
        });
        
        //load records when user click 'load records' button.
        $('#LoadRecords').click(function (e) {
            e.preventDefault();
            $('#FancyLogContainer').jtable('load', {
            	pnr: $('#pnr').val()
            });
        });
        
        
/*         //Delete selected students
        $('#DeleteAllButton').button().click(function () {
            var $selectedRows = $('#FancyLogContainer').jtable('selectedRows');
            $('#FancyLogContainer').jtable('deleteRows', $selectedRows);
        }); */
    });
    
</script>
</head>
<body>

<!-- <div class="bs-example"> -->
<%@include file = "jspf/header.jspf" %>
	<!-- </div> -->
	<div class="bs-example">
		<nav class="navbar navbar-default" role="navigation">
		<div class="container-fluid">
			<div class="navbar-header">
				<button class="navbar-toggle" data-target="#bs-example-navbar-collapse-2" data-toggle="collapse"
					type="button">
					<!-- <span class="sr-only">Toggle navigation</span> -->
					<span class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
				<a class="navbar-brand">Enter your PNR:</a>
			</div>
			<form class="navbar-form navbar-left" role="search">
				<div class="form-group">
					<input type="text" class="form-control" placeholder="Search" name="pnr" id="pnr">
				</div>
				<button type="submit" class="btn btn-default" id="LoadRecords">
					<span class="glyphicon glyphicon-search"></span> Search
				</button>
			</form>
		</div>
		</nav>
		<div id="FancyLogContainer"></div>
	</div>
</body>
</html>