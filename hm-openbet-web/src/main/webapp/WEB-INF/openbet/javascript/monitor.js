var Monitor = function () {
    /* module to retrieve data from the server */
    var server = ServerFactory.getServer();

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
        console.log("Setting active page to: " + page);
        activePage(page);
    };

    /* 	returns true if parameter matches active page, false otherwise */
    var isActivePage = function (page) {
        return activePage() === page;
    };


    /* sets the selected account */
    var setSelectedOperation = function (operation) {
        console.log("Setting selected operation: " + operation);
        model.selectedOperation(operation);
    };

    /* returns true if the account matches selected account, false otherwise */
    var isSelectedOperation = function (operation) {
        return operation === model.selectedOperation();
    };

    /* process operations count received from server */
    var processOperationsCount = function(data){
        console.log("Operations count retrieved from server: " + ko.toJSON(data));
        operationsCount(data);
    }

    /* process operations array received from server */
    var processOperations = function (data) {
        console.log("Operations data retrieved from server: " + ko.toJSON(data));
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
        console.log("Data retrieved from server: " + ko.toJSON(data));
        // clear model for types
        model.types([]);
        //add received types to the model
        model.types.push({
            value: "--- None ---", setOperationType: function () {
                criteria.operationType(null);
            }
        });

        data.forEach(function (name) {
            model.types.push({
                value: name, setOperationType: function () {
                    criteria.operationType(name);
                }
            });
        });
    };
    /* method to start retrieving the data from the server side and sets it in the model */
    var retrieveData = function () {
        console.log("Using criteria : " + ko.toJSON(criteria) + " ...")
        console.log("Retrieving operations data from server......");
        server.getOperationsData(criteria, processOperations);

        console.log("Retrieving operation types from server......");
        server.getOperationTypes(processTypes);
    };

    var retrieveCount = function(){
        console.log("Using criteria : " + ko.toJSON(criteria) + " ...")
        console.log("Retrieving operations count from server......");
        server.getOperationsCount(criteria, processOperationsCount);
    };

    var subscribeCriteria = function(){
        criteria.operationType.subscribe(retrieveCount);
        criteria.fromDate.subscribe(retrieveCount);
        criteria.toDate.subscribe(retrieveCount);
        criteria.customer.subscribe(retrieveCount);
        criteria.bet.subscribe(retrieveCount);
    };

    /* computed observable for title drop down text change */
    var typeSelected = ko.pureComputed(function () {
        if (criteria.operationType() == null) {
            return "select"
        } else {
            return criteria.operationType();
        }
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
        setActivePage: setActivePage,
        isActivePage: isActivePage,
        setSelectedOperation: setSelectedOperation,
        isSelectedOperation: isSelectedOperation,
        typeSelected: typeSelected,
        search: retrieveData,
        searchCriteria: criteria
    };

}();



