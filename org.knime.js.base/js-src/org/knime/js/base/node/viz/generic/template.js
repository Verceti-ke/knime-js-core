knime_generic_view = function() {
	
	view = {};
	
	view.init = function(representation, value) {
		if (representation.jsCode == null) {
			document.body.innerHTML = 'Error: No script available.';
		} else {
			// Define KNIME table and set data
			var knimeDataTable = new kt();
			knimeDataTable.setDataTable(representation.table);
			
			// Import style dependencies
			var head = document.getElementsByTagName('head')[0];
			for ( var j = 0; j < representation.cssDependencies.length; j++) {
				var styleDep = document.createElement('link');
				styleDep.type = 'text/css';
				styleDep.rel = 'stylesheet';
				styleDep.href = representation.cssDependencies[j];
				head.appendChild(styleDep);
			}
			// Import own style declaration
			var styleElement = document.createElement('style');
			styleElement.type = 'text/css';
			styleElement.appendChild(document.createTextNode(representation.cssCode));
			head.appendChild(styleElement);
			
			// Import JS dependencies and call JS code after loading
			require(representation.jsDependencies, function() {
				try {
				    eval(representation.jsCode); 
				} catch (e) {
					var errorString = "Error in script\n";
					if (e.stack) {
						errorString += e.stack;
					} else {
						errorString += e;
					}
				    alert(errorString);
				}
			});
		}
	};
	
	view.validate = function() {
		return true;
	};
	
	view.getComponentValue = function() {
		return null;
	};
	
	return view;
}();