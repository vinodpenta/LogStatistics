<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en"><head>
<!-- Bootstrap core CSS -->
<link href="../static/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../static/bootstrap/css/docs.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="../static/css/fancylog.css" rel="stylesheet">

	<style type="text/css" id="holderjs-style"></style>
    <title>Signin</title>
  </head>

  <body>
  <%@include file="jspf/header.jspf" %>

    <div class="container">

      <form class="form-signin" role="form">
        <h2 class="form-signin-heading">Please sign in</h2>
        <input class="form-control" placeholder="Email address" required="" autofocus="" type="email">
        <input class="form-control" placeholder="Password" required="" type="password">
        <label class="checkbox">
          <input value="remember-me" type="checkbox"> Remember me
        </label>
        <button class="btn btn-lg btn-primary btn-block" type="submit">Sign in</button>
      </form>

    </div>   

</body></html>