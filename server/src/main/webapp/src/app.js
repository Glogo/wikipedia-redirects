angular.module('wikiPagesApp', [])

    .controller('searchPagesController', ['$scope', '$http', function($scope, $http) {

        /**
         * Send rest request to get pages data info
         */
        $scope.getInfo = function(){

        	$scope.info = {};

        	$http.get('http://localhost:8080/redirects/webapi/alt/info').
        	    success(function(data, status, headers, config) {

        		    // Remember info
        		    $scope.info = data;
        	    }).
        	    error(function(data, status, headers, config) {
        		    alert("Could not connect to server.");
        	    }
        	);
        }

        /**
         * Send rest request with search term to find pages
         */
        $scope.findPages = function(){

        	$scope.pages = [];
        	$scope.noData = false;
        	$scope.emptySearchTerm = false;

        	if($scope.searchTerm == undefined || $scope.searchTerm == null || $scope.searchTerm.length == 0){
        		$scope.emptySearchTerm = true;
        		return;
        	}

        	$http.get('http://localhost:8080/redirects/webapi/alt/search/' + $scope.searchTerm).
        	    success(function(data, status, headers, config) {

        	    	if(Object.keys(data).length == 0){
        	    		$scope.noData = true;

        	    		// Remember last search term to fix bug on editing
        	    		$scope.lastSearchTerm = $scope.searchTerm;

        	    		return;
        	    	}

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
        	    }
            );
        }

		// Set default values
		$scope.searchTerm = "Information";
        $scope.noData = false;
        $scope.emptySearchTerm = false;
		$scope.getInfo();
		$scope.findPages();
    }]);
