var ServerFactory = function(){
    var server = function (){
        return MonitorServerREST();
    };
    return {
        getServer : server
    };
}();
var MonitorServerREST = function () {

    var getOperationsData = function(criteria, processAnswer){
        $.ajax({
            url: 'monitor/openbet/operations',
            data: ko.toJSON(criteria),
            error:  function(jqXHR, textStatus, errorThrown){
                alert('getOperations error: ' + textStatus + ' thrown: ' + errorThrown);
            },
            contentType: "application/json",
            dataType: 'json',
            success: function(data) {
                //operationData = data;
            },
            type: 'POST'
        }).then(processAnswer);
    };

    var getOperationsCount = function(criteria, processAnswer){
        $.ajax({
            url: 'monitor/openbet/operations/count',
            data: ko.toJSON(criteria),
            error:  function(jqXHR, textStatus, errorThrown){
                alert('getOperations error: ' + textStatus + ' thrown: ' + errorThrown);
            },
            contentType: "application/json",
            dataType: 'json',
            success: function(data) {
                //operationData = data;
            },
            type: 'POST'
        }).then(processAnswer);
    };

    var getOperationTypes = function(processAnswer){
        $.ajax({
            url: 'monitor/openbet/types',
            data: {},
            error:  function(jqXHR, textStatus, errorThrown){
                alert('getTypes error: ' + textStatus + ' thrown: ' + errorThrown);
            },
            contentType: "application/json",
            dataType: 'json',
            success: function(data) {
                //operationTypes = data;
            },
            type: 'GET'
        }).then(processAnswer);
    };

	return {
		/* add members that will be exposed publicly */
        getOperationsCount: getOperationsCount,
		getOperationsData: getOperationsData,
		getOperationTypes: getOperationTypes

	};
};