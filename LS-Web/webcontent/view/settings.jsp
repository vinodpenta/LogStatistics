<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

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

<title>Fancy Log Settings</title>

<script type="text/javascript">
 
    $(document).ready(function () {
 
        $('#SettingsContainer').jtable({
            title: 'Admintool Settings',
            paging: true,
            sorting: true,
            defaultSorting: 'ApplicationName ASC',
            actions: {
                listAction: '../config/settings',
                deleteAction: '../config/settings/delete',
                updateAction: '../config/settings/save',
                createAction: '../config/settings/new'
            },
            fields: {
                Id: {
                    key: true,
                    create: false,
                    edit: false,
                    list: false
                },
                ApplicationName: {
                    title: 'Application Name'
                },
                FancyLogURLPattern: {
                    title: 'URL Pattern',
                    sorting: false,
                    width: '20%'
                },
                Host: {
                	title: 'Host'
                },
                NodeList: {
                    title: 'Server Names(separated by comma)',
                    width: '18%',
                    sorting: false
                },
                Instance: {
                    title: 'Instance',
                    options: { 'a': 'a', 'b': 'b', 'c':'c'}
                },
                SessionIdPosition: {
                    title: 'SessionId Possition',
                    sorting: false
                },
                NoOfDays: {
                	title: 'No of Days'
                }
             }
        });
 
        //Load all records when page is first shown
        $('#SettingsContainer').jtable('load');
    });
 
</script>
</head>
<body>

<%@include file="jspf/header.jspf" %>

<div class="bs-example">
<div id="SettingsContainer"></div>
</div>

</body>
</html>