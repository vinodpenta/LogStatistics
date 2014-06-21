<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<!-- JQuery -->
<script src="../static/jquery/jquery-1.10.2.min.js"	type="text/javascript"></script>
<script src="../static/jquery/jquery-ui-1.10.3.custom.min.js" type="text/javascript"></script>
<link href="../static/jquery/jquery-ui-1.10.3.custom.min.css" rel="stylesheet" type="text/css" />

<!-- Bootstrap core CSS -->
<link href="../static/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../static/bootstrap/css/docs.css" rel="stylesheet">

<script href="../static/booststrap/js/bootstrap.js"/>

<link href="../static/datepicker/css/datepicker.css" rel="stylesheet">
<script href="../static/datepicker/js/bootstrap-datepicker.js"/>

<!-- JTable -->
<link href="../static/jtable/themes/lightcolor/gray/jtable.css" rel="stylesheet" type="text/css" />
<script src="../static/jtable/jquery.jtable.min.js"	type="text/javascript"></script>
 
<!-- Custom styles for this template -->
<link href="../static/css/fancylog.css" rel="stylesheet">

<style type="text/css" id="holderjs-style"></style>

<script type="text/javascript">
	$(document).ready(function() {
		$('.date-picker').datepicker();
		$('#date-picker-2').change(function() {
			alert("Handler for .change() called.");

		});
	});
</script>

<title>Exceptions</title>
</head>
<body>
	<%@include file="jspf/header.jspf"%>
	<div class="bs-example page-fancylog">
		<%@include file="jspf/exceptionmenu.jspf"%>
		<nav class="navbar navbar-default" role="navigation">
		<div class="control-group">
        	<label for="date-picker-2" class="control-label">Select Data</label>
	        <div class="controls">
	            <div id="datepicker" class="input-group">
	                <input id="date-picker-2" type="text" class="date-picker form-control"/>
	                <label for="date-picker-2" class="input-group-addon btn"><span class="glyphicon glyphicon-calendar"></span>
	                </label>
	            </div>
	        </div>
    	</div>
	</div>
</body>
</html>