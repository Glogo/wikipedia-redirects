<!DOCTYPE html>
<html ng-app="wikiPagesApp" resize>
    <head>
        <meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<title>English Wikipedia alternative pages titles</title>

		<!-- Favicon -->
		<link rel="shortcut icon" type="image/png" href="favicon.png"/>

		<!-- Bootstrap -->
		<link href="css/bootstrap.min.css" rel="stylesheet">
		
		<!-- Style -->
		<link href="css/style.css" rel="stylesheet">

		<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
		<!--[if lt IE 9]>
  		    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
		    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
		<![endif]-->

		<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
		<script src="js/jquery-1.11.1.min.js"></script>
		<!-- Include all compiled plugins (below), or include individual files as needed -->
		<script src="js/bootstrap.min.js"></script>
		<!-- Angular -->
		<script src="js/angular.min.js"></script>
		<!-- Chart -->
		<script src="js/Chart.min.js"></script>
		<!-- App script -->
		<script src="js/app.js"></script>
	</head>
	<body>
		<!-- Main container -->
		<div id="main-container" class="container" ng-controller="searchPagesController">
			<h2>Wiki pages search</h2>
			<div class="panel panel-default">
				<div class="panel-body">
					<form class="navbar-form" role="search" ng-submit="doFilter()">
						<div class="input-group">
							<input type="text" class="form-control" placeholder="Enter page title" ng-model="searchTerm">
							<div class="input-group-btn">
								<button class="btn btn-default" type="submit"><i class="glyphicon glyphicon-search" title="Search wiki pages"></i></button>
								<button class="btn btn-default" type="button" onclick="$('#metrics').slideToggle();return false;" title="Show/hide metrics"><i class="glyphicon glyphicon-stats"></i></button>
							</div>
						</div>
					</form>
					
					<div id="metrics" style="display:none;">
						<figure id="metric1">
							<canvas></canvas>
							<figcaption>Redirect and non-redirect pages</figcaption>
						</figure>

						<figure id="metric2">
							<table class="statistics">
								<tr>
									<td class="left">Total pages:</td><td>{{statistics.totalPagesCnt}}</td>
								</tr>
								<tr>
									<td class="left">Non-redirect pages:</td><td>{{statistics.nonRedirPagesCnt}}</td>
								</tr>
								<tr>
									<td class="left">Redirect pages:</td><td>{{statistics.redirPagesCnt}}</td>
								</tr>
								<tr>
									<td class="left">Non-redirect pages with alt title:</td><td>{{statistics.pagesWithAltCnt}}</td>
								</tr>
							</table>
						</figure>
					</div>
					
				</div>
				
				<table class="table table-striped table-bordered">
					<thead>
						<tr>
							<th class="col-md-6">Page title</th>
							<th class="col-md-6">Alternative titles<div class="sub">Titles of pages redirected to current page</div></th>
						</tr>
					</thead>
					<tbody>
						<tr ng-repeat="page in pages">
							<td>{{page.title}}</td>
							<td>
								<ul>
									<li ng-repeat="altTitle in page.alternative track by $index">
										{{altTitle}}
									</li>
								</ul>
							</td>
						</tr>
					</tbody>
				</table>

				<div class="panel-footer clearfix" ng-if="noData">
					<div>
						<p>No results found. Search term must be at least 3 characters long.</p>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>