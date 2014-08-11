<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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
 
    $(document).ready(function () {
 
        //Prepare jtable plugin
        $('#ExceptionDetailsContainer').jtable({
            title: ' Exception logs',
            actions: {
                listAction: '../config/exceptionlogs?id='+getParameterByName('ObjectId')
            },
            fields: {
                date: {
                    title: 'Record Date',
                    width: '7%'
                },
                serviceName: {
                    title: 'Service name',
                    width: '8%'
                },
                pnr: {
                	title:'PNR'
                },
                host: {
                	title: 'Host',
                	width: '3%'
                },
                market: {
                	title: 'Market',
                	width: '3%'
                },
                channel: {
                	title:'Channel',
                	width: '3%'
                },
                errorCode: {
                    title: 'Error Code',
                    width: '5%'
                },
                errorDescription: {
                    title: 'Error Description(If any)',
                    width: '15%'
                },
                view: {
                	title: ' ',
                	width: '1%',
					display : function(data) {
						    	return '<a href="../config/searchLogs/logs?id='+data.record.sessionID+'"><span class="glyphicon glyphicon-floppy-open glyphicon-size"></span></a>';
						    }
                },
                download: {
                	title: ' ',
                	width: '1%',
					display : function(data) {
					    	return '<a href="../config/searchLogs/download?id='+data.record.sessionID+'"><span class="glyphicon glyphicon-save glyphicon-size"></span></a>';
					    }
                }
            }
        });
        
        //Load records list from server
        $('#ExceptionDetailsContainer').jtable('load');
        
        function getParameterByName(name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                results = regex.exec(location.search);
            return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        }
	});
    
</script>
</head>
<body>
	<div class="bs-example page-fancylog">
		<nav class="navbar navbar-default" role="navigation">
		</nav>
    	<div id="ExceptionDetailsContainer"></div>
	
</body>
</html>