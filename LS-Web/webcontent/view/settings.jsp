<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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

<!-- Bootstrap javaScript -->
<script src="../static/bootstrap/js/bootstrap.js" type="text/javascript"></script>

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
            sorting: true,
            defaultSorting: 'ApplicationName ASC',
            actions: {
                listAction: '../config/settings',
                deleteAction: '../config/settings/delete',
                updateAction: '../config/settings/save',
                createAction: '../config/settings/new'
            },
            fields: {
                id: {
                    key: true,
                    create: false,
                    edit: false,
                    list: false
                },
                applicationName: {
                    title: 'Application Name'
                },
                fancyLogURLPattern: {
                    title: 'URL Pattern',
                    sorting: false,
                    width: '20%'
                },
                host: {
                	title: 'Host'
                },
                nodeList: {
                    title: 'Server Names(separated by comma)',
                    width: '18%',
                    sorting: false
                },
                instance: {
                    title: 'Instance',
                    options: { 'a': 'a', 'b': 'b', 'c':'c'}
                },
                sessionIdPosition: {
                    title: 'SessionId Possition',
                    sorting: false
                },
                noOfDays: {
                	title: 'No of Days'
                },
                fileName: {
                	title: 'File name',
                	visibility:'hidden'
                },
                downloadLocation: {
                	title: 'Dowload location',
                	visibility:'hidden'
                },
                exceptionFiles:{
                	title: 'Exception file names(separated by comma)',
                	sorting: false,
                	visibility:'hidden'
                },                
                logInURL: {
                	title: 'Login URL',
                	sorting: false,
                	visibility:'hidden'
                },
                userName: {
                	title: 'Username',
                	sorting: false,
                	visibility:'hidden'
                },
                passWord: {
                	title: 'Password',
                	sorting: false,
                	visibility:'hidden'
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

<div class="bs-example page-fancylog">
<div id="SettingsContainer"></div>
</div>

</body>
</html>