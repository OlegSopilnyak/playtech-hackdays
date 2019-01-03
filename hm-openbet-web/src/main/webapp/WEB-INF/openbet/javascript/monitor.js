var Monitor = function () {
    /**
 * The module of service
 *
 * @constructor
 */
function OperationsCountModule() {
    Module.call(this, "HealthMonitor");
    this.sysId = "openBetOperations";
    this.appId = "operationsCounter";
    this.ver = "0.01";
    this.descr = "Service to count operations that suit search criteria";
}
OperationsCountModule.prototype = Object.create(Module.prototype);
OperationsCountModule.prototype.constructor = OperationsCountModule;
OperationsCountModule.prototype.systemId = function () {return this.sysId;};
OperationsCountModule.prototype.applicationId = function () {return this.appId;};
OperationsCountModule.prototype.version = function () {return this.ver;};
OperationsCountModule.prototype.description = function () {return this.descr;};

/**
 * The service to calculate OpenBet operations suitable for the search criteria
 *
 * @constructor
 */
function OperationsCounterService(){
    MonitoredService.call(this, new OperationsCountModule());
    var self = this;
    var DELAY_PATH = "registry.monitor.service.external.ob.counter.delay";
    var delay = 2000;
    var BORDER_COLOR_PATH = "registry.monitor.service.external.ob.counter.border.color";
    var borderColor = "green";
    var selected = 0;
    // thread properties
    var timerId = null;
    var firstTime = true;

    // define external configuration parameters
    this.configuration[DELAY_PATH] = "2000";
    this.configuration[BORDER_COLOR_PATH] = "green";

    this.selectedBorderColor = ko.observable("1px dashed "+borderColor+"");
    this.logger = self.createOutputDevice("log");

    this.exchange = function (logger) {
        try{
            logger.print("Retrieving operations count ","by criteria : ",  ko.toJSON(criteria), " ...");
// console.log("Using criteria : " + ko.toJSON(criteria) + " ...");
// console.log("Retrieving operations count from server......");
            self.selectedBorderColor("1px dashed yellow");
            server.getOperationsCount(criteria, counterReceived);
        }catch (err){
            logger.print("Error ", err);
            logger.actionFail();
// console.log("Error", err);
            self.selectedBorderColor("1px dotted yellow");
        }
        function counterReceived(data){
            logger.print("Received ", data, " selected operations.");
            operationsCount(data);
            self.selectedBorderColor("1px dashed "+borderColor+"");
            logger.actionSuccess();
        }
    };

    this.isWorks = function () {return firstTime || timerId != null;};

    this.start = function(){
        firstTime = true;
        this.logger.print(" Service-counter starting...");
console.log("Starting the service-counter");
        function processOperationsCounting(){
            var logger = self.createOutputDevice("log");
            logger.associateAction("Count operations suitable by criteria.");
            logger.actionBegin();
            self.exchange(logger);
            if (firstTime || timerId != null) {
                self.logger.print("Next iteration in ", delay, " ms.");
// console.log("Next iteration in "+delay+" ms.");
                // start the new iteration
                timerId = setTimeout(processOperationsCounting, delay);
            }else{
                // service finished
                self.logger.print("Service-counter stopped.");
console.log("Service-counter is stopped.");
                // unregister module
                HealthScanner.getInstance().unregister(this);
            }
        }
        processOperationsCounting();
        firstTime = false;
        HealthScanner.getInstance().register(this);
    };
    this.stop = function (){
        if (timerId){
            // forced service shutdown
            clearTimeout(timerId);
            timerId = null;
            firstTime = true;
        }
    };
    this._configurationChanged = function(){
        // processing the "delay" attribute
        checkDelayChangeConfiguration(this.logger);
        // processing "borderColor" attribute
        checkBorderColorChangeConfiguration(this.logger);
    };

    var checkDelayChangeConfiguration = function(logger) {
        var newValue = +self.configuration[DELAY_PATH];
        if (isNaN(newValue)) {
            // restore value by default
// console.log("Restore value to default.");
            logger.print("Restore value to default.");
            delay = 2000;
            self.configuration[DELAY_PATH] = "2000";
        } else {
            logger.print("Change value to ", newValue);
// console.log("Change value to " + newValue);
            // changing the value of delay
            delay = newValue;
        }
    };
    var checkBorderColorChangeConfiguration = function(logger) {
        var newValue = self.configuration[BORDER_COLOR_PATH];
        if (newValue) {
            // changing the value of pingUrl
            logger.print("Change value to ", newValue);
// console.log("Change value to " + newValue);
            borderColor = newValue;
        } else {
            // do nothing
// console.log("Do nothing.");
        }
    };
}
OperationsCounterService.prototype = Object.create(MonitoredService.prototype);
OperationsCounterService.prototype.constructor = OperationsCounterService;
OperationsCounterService.prototype.active = function () {return this.isWorks();};
OperationsCounterService.prototype.restart = function () {this.stop(); this.start();};
OperationsCounterService.prototype._applyConfigurationChanges = function () {this._configurationChanged();};


    /* module to retrieve data from the server */
    var server = ServerFactory.getServer();

    // the service to count selected operations
    var counterService = new OperationsCounterService();

    /* the model */
    var model = {
        types: ko.observableArray(),
        operations: ko.observableArray(),
        selectedOperation: ko.observable()
    };

    var criteria = {
        operationType: ko.observable(),
        fromDate: ko.observable(),
        toDate: ko.observable(),
        customer: ko.observable(),
        bet: ko.observable()

    };
    var operationsCount = ko.observable(0);

    /* attribute to hold the active page */
    var activePage = ko.observable("OpenBet");

    /* method to set the active page */
    var setActivePage = function (page) {
        counterService.logger.print("Setting active page to: ", page);
// console.log("Setting active page to: " + page);
        activePage(page);
    };

    /* 	returns true if parameter matches active page, false otherwise */
    var isActivePage = function (page) {
        return activePage() === page;
    };


    /* sets the selected account */
    var setSelectedOperation = function (operation) {
        counterService.logger.print("Setting selected operation: ", operation);
// console.log("Setting selected operation: " + operation);
        model.selectedOperation(operation);
    };

    /* returns true if the account matches selected account, false otherwise */
    var isSelectedOperation = function (operation) {
        return operation === model.selectedOperation();
    };

    /* process operations array received from server */
    var processOperations = function (data) {
        counterService.logger.print("Operations data retrieved from server.");
// console.log("Operations data retrieved from server: " + ko.toJSON(data));
        // clear model for operations
        model.operations([]);
        //add operations to the model
        data.forEach(function (operation) {
            operation.inputXML = $.format(operation.inputXML, 4);
            operation.outputXML = $.format(operation.outputXML, 4);
            model.operations.push(operation);
        });
    };
    /* process operation types received from server */
    var processTypes = function (data) {
        counterService.logger.print("Operation types data retrieved from server.");
// console.log("Data retrieved from server: " + ko.toJSON(data));
        // clear model for types
        model.types([]);
        //add received types to the model
        model.types.push({
            value: "--- None ---",
            setOperationType: function () {criteria.operationType(null);}
        });

        data.forEach(function (name) {
            model.types.push({
                value: name,
                setOperationType: function () {criteria.operationType(name);}
            });
        });
    };
    /* method to start retrieving the data from the server side and sets it in the model */
    var retrieveData = function () {
        counterService.logger.print("Retrieving operations data by criteria ", ko.toJSON(criteria));
// console.log("Using criteria : " + ko.toJSON(criteria) + " ...");
// console.log("Retrieving operations data from server......");
        server.getOperationsData(criteria, processOperations);

        counterService.logger.print("Retrieving operation types from server...");
// console.log("Retrieving operation types from server......");
        server.getOperationTypes(processTypes);
    };

    function retrieveCount(){
        var logger = counterService.createOutputDevice("log");
        logger.associateAction("Count operations suitable by criteria.");
        logger.actionBegin();
        counterService.exchange(logger);
    }

    var subscribeCriteria = function(){
        criteria.operationType.subscribe(retrieveCount);
        criteria.fromDate.subscribe(retrieveCount);
        criteria.toDate.subscribe(retrieveCount);
        criteria.customer.subscribe(retrieveCount);
        criteria.bet.subscribe(retrieveCount);
    };

    /* computed observable for title drop down text change */
    var typeSelected = ko.pureComputed(function () {
        return criteria.operationType() ? criteria.operationType() : "select";
    });

    /* Add handler for datepicker */
    var configureKnockoutJS = function () {
        ko.bindingHandlers.dateTimePicker = {
            init: function (element, valueAccessor, allBindingsAccessor) {
                //initialize datepicker with some optional options
                var options = allBindingsAccessor().dateTimePickerOptions || {
                        format: "YYYY-MM-DD HH:mm",
                        showClear: true,
                        useCurrent: false
                    };
                $(element).datetimepicker(options);

                //when a user changes the date, update the view model
                ko.utils.registerEventHandler(element, "dp.change", function (event) {
                    var value = valueAccessor();
                    if (ko.isObservable(value)) {
                        if (event.date != null && (event.date instanceof Date)) {
                            value(event.date.toDate());
                        } else {
                            value(event.date);
                        }
                    }
                });

                ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
                    var picker = $(element).data("DateTimePicker");
                    if (picker) {
                        picker.destroy();
                    }
                });
            },
            update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {

                var picker = $(element).data("DateTimePicker");
                //when the view model is updated, update the widget
                if (picker) {
                    var koDate = ko.utils.unwrapObservable(valueAccessor());

                    //in case return from server datetime i am get in this form for example /Date(93989393)/ then fomat this
                    if (koDate != null) {
                        koDate = (typeof (koDate) !== 'object') ? new Date(parseFloat(koDate.replace(/[^0-9]/g, ''))) : koDate;
                    } else {
                        koDate = null;
                    }

                    picker.date(koDate);
                }
            }
        };
    };

    var init = function () {

        retrieveData();
        retrieveCount();
        counterService.start();

        /* subscribe to criteria fields change */
        subscribeCriteria();

        /* configure knockout js handlers */
        configureKnockoutJS();

        //apply ko bindings
        ko.applyBindings(Monitor);
    };

    /* execute the init function when the DOM is ready */
    $(init);

    return {
        model: model,
        operationsTypes: model.types,
        operationsCount: operationsCount,
        selectedBorderStyle: counterService.selectedBorderColor,
        setActivePage: setActivePage,
        isActivePage: isActivePage,
        setSelectedOperation: setSelectedOperation,
        isSelectedOperation: isSelectedOperation,
        typeSelected: typeSelected,
        search: retrieveData,
        searchCriteria: criteria
    };

}();



