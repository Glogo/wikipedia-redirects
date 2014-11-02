angular.module('wikiPagesApp', [])

    .service('metricsService', ['$rootScope', function ($rootScope) {
        const colors = {
            red : "#F7464A",
            redHighlight : "#FF5A5E",
            yellow : "#FDB45C",
            yellowHighlight : "#FFC870",
        };

        /**
         * Chart 1: Redirect & non-redirect pages
         */
        function createChart1() {
            // Get metrics charts canvas
            var ctx = $("#metric1 canvas").get(0).getContext("2d");
            
            var data = [
                {
                    value: $rootScope.statistics.nonRedirPagesCnt,
                    color: colors.yellow,
                    highlight: colors.yellowHighlight,
                    label: "Non-redirect pages"
                },
                {
                    value: $rootScope.statistics.redirPagesCnt,
                    color: colors.red,
                    highlight: colors.redHighlight,
                    label: "Redirect pages"
                }
            ];

            new Chart(ctx).Pie(data);
        }

		return {
			createCharts : function(){
                createChart1();
			}
		}
    }])
    .controller('searchPagesController', ['$scope', '$rootScope', 'metricsService', '$http', function($scope, $rootScope, metricsService, $http) {
	
        /**
         * Do filtering with search term
         */
        $scope.doFilter = function(){
        	
        	$scope.pages = [];
        	
        	// Simple GET request example :
        	$http.get('http://localhost:8080/WikipediaRedirectsServer/webapi/getRedirects/' + $scope.searchTerm).
        	  success(function(data, status, headers, config) {
        		  
        		  // Iterate over all pages
        		  for (var key in data) {
        			    if (data.hasOwnProperty(key)) {
							$scope.pages.push({
								title: key,
								alternative: data[key]
							});
        			    }
        			}
        	  }).
        	  error(function(data, status, headers, config) {
        		  alert("Could not connect to server.");
        	  });
        }
		
		// Set default values
		$scope.searchTerm = "Information";
        $scope.noData = false;
        //$rootScope.statistics = pagesData.info; // TODO read from sepparate rest get
        
        // Initialize charts
        //metricsService.createCharts();
		
		// Run filter with default search settings on first show
		$scope.doFilter();
    }]);