<!DOCTYPE html>
<html data-ng-app="wikiPagesApp" data-resize>
    <head>
        <meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<title>English Wikipedia alternative pages titles</title>

		<!-- Favicon -->
		<link rel="shortcut icon" type="image/png" href="favicon.png"/>

		<!-- Bootstrap -->
		<link href="libs/bootstrap/bootstrap.min.css" rel="stylesheet">

		<!-- Style -->
		<link href="css/style.css" rel="stylesheet">

		<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
		<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
		<!--[if lt IE 9]>
  		    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
		    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
		<![endif]-->

		<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
		<script src="libs/jquery/jquery-1.11.1.min.js"></script>
		<!-- Include all compiled plugins (below), or include individual files as needed -->
		<script src="libs/bootstrap/bootstrap.min.js"></script>
		<!-- Angular -->
		<script src="libs/angular/angular.min.js"></script>
		<!-- App script -->
		<script src="src/app.js"></script>
	</head>
	<body>
		<!-- Main container -->
		<div id="main-container" class="container" data-ng-controller="searchPagesController">
			<h2>Wiki pages search</h2>
			<div class="panel panel-default">
				<div class="panel-body">
					<form class="navbar-form" data-role="search" data-ng-submit="findPages()">
						<div class="input-group">
							<input type="text" class="form-control" placeholder="Enter page title" data-ng-model="searchTerm">
							<div class="input-group-btn">
								<button class="btn btn-default" type="submit"><i class="glyphicon glyphicon-search" title="Search wiki pages"></i></button>
							</div>
						</div>
						<div class="sub"><b>Total pages:</b> {{info.totalPagesCnt}}. <b>Redirects: </b> {{info.redirPagesCnt}}. <b>Pages redirected to: </b>{{info.pagesWithAltCnt}}</div>
					</form>
				</div>

				<table class="table table-striped table-bordered">
					<thead>
						<tr>
							<th class="col-md-6">Page title</th>
							<th class="col-md-6">Alternative titles<div class="sub">Titles of pages redirected to current page</div></th>
						</tr>
					</thead>
					<tbody>
						<tr data-ng-repeat="page in pages">
							<td>{{page.title}}</td>
							<td>
								<ul>
									<li data-ng-repeat="altTitle in page.alternative track by $index">
										{{altTitle}}
									</li>
								</ul>
							</td>
						</tr>
					</tbody>
				</table>

				<div class="panel-footer clearfix" data-ng-if="noData || emptySearchTerm">
					<div>
						<p data-ng-if="noData">No results found for search term: '{{lastSearchTerm}}'</p>
						<p data-ng-if="emptySearchTerm">Search term could not be empty.</p>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
