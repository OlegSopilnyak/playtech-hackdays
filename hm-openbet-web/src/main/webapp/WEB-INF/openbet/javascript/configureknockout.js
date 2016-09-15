var ConfigureKnockout = function () {

	/* method to add custom currency binding */
	var applyCurrencyBinding = function () {
		/* custom binding for currency */
		ko.bindingHandlers.currency = {
			update: function(element, valueAccessor){
				// retrieve observable value
				var value = ko.utils.unwrapObservable(valueAccessor()) || 0;
		        //convert to number of string
				value = + value;
				//format currency
				var formattedText =  "$" + value.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, "$1,");
				//apply formatted text to the underlying DOM element
				$(element).text(formattedText);
			}
		};		
	};
	
	/* method to add memento observable */
	var createMementoObservable = function () {
		/* memento custom observable */
		ko.mementoObservable = function(initialValue) {
		    //the current state 
			var state = ko.observable(initialValue);
			//the remembered state 
		    var mementoState = initialValue;
		    //commit the state 
		    state.commit = function() {
		        mementoState = state();
		    };
		    //reset state from memory
		    state.reset = function() {
		    	state(mementoState);
		    };
		    //return the current state
		    return state;
		};		
	};
	
	var configureValidationPlugin = function () {
		//initialise and configure the validation plugin 
		ko.validation.init({
			errorElementClass: 'has-error',
		    errorMessageClass: 'help-block'
		});		
	};
	
	/* add code to initialise this module */
	var init = function () {
		applyCurrencyBinding();
		createMementoObservable();
		configureValidationPlugin();
	}();
	
	return {
		/* nothing to return */
	};
}();