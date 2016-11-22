/**
 * Definition of main parts of monitored modules on the front-end
 */
'use strict';

/**
 * Exception will be thrown if class Module' methods would be not redefined
 *
 * @param message appropriate message
 * @constructor
 */
function ModuleNotDefined(message) {
    Error.call(this,"not-redefined");
    this.name = "ModuleNotDefined";
    this.message = message;
    if (Error.captureStackTrace){
        Error.captureStackTrace(this, ModuleNotDefined);
    }else{
        this.stack = (new Error()).stack;
    }
};

ModuleNotDefined.prototype = Object.create(Error.prototype);
ModuleNotDefined.prototype.constructor = ModuleNotDefined;

/**
 * Interface of monitored module
 * @constructor
 */
function Module(){
    this.moduleKey = function () {
        return [this.systemId(), this.applicationId(), this.version()].join("|");
    };
};
Module.prototype.systemId = function(){
    throw new ModuleNotDefined("Not redefined method systemId");
};
Module.prototype.applicationId = function(){
    throw new ModuleNotDefined("Not redefined method applicationId");
};
Module.prototype.version = function(){
    throw new ModuleNotDefined("Not redefined method version");
};
Module.prototype.description = function(){
    throw new ModuleNotDefined("Not redefined method description");
};

/**
 * The parent of any monitored services
 *
 * @param module the service to be monitored
 * @constructor
 */
function MonitoredService(module) {
    // current module of service
    this.module = module;
    // current configuration of service
    this.configuration = {};
    // module's output without actions
    this.rawOutput = [];
    // module's output with actions
    this.actions = [];
};
/**
 * To shot the state of the module (format is ExternalModulePing.class)
 *
 * @returns {{module: {systemId: *, applicationId: *, versionId: *, description: *}, host: string, state: string, configuration: Array, output: Array, actions: Array}}
 */
MonitoredService.prototype.snapshot = function () {
    var self = this;

    //  prepare snapshot
    var snapshot = {
        module:{
            systemId : self.module.systemId(),
            applicationId: self.module.applicationId(),
            versionId: self.module.version(),
            description: self.module.description(),
            key:  function () {
                return [this.systemId, this.applicationId, this.versionId].join("|");
            }

},
        host: "localhost",
        state: self.active() ? "active" : "passive",
        configuration: [],
        output: [],
        actions: []
    };

    // iterate the configuration
    for(var key in this.configuration){
        var configurationItem = {
            path: key,
            type: "STRING",
            value: self.configuration[key],
            description: "Duration between modules scan."
        };
        snapshot.configuration.push(configurationItem);
    }

    // iterate raw output
    this.rawOutput.forEach(function (item, i, arr) {
        var moduleOutputMessage = {
            messageType: item.messageType,
            whenOccurred: item.whenOccurred,
            payload: item.payload
        };
        snapshot.output.push(moduleOutputMessage);
    });
    // clear raw output array
    self.rawOutput = [];

    var notDoneActions = [];

    // iterate actions output
    this.actions.forEach(function (item, i, arr) {
        if (item.isDone()){
            var action = {
                name: item.name,
                description: item.description,
                state: item.state,
                duration: item.duration,
                startTime: item.startTime,
                finishTime: item.finishTime,
                output: []
            };
            snapshot.actions.push(action);
            // store action's outputs
            item.output.forEach(function (item, i, arr) {
                var moduleOutputMessage = {
                    messageType: item.messageType,
                    whenOccurred: item.whenOccurred,
                    payload: item.payload
                };
                action.output.push(moduleOutputMessage);
            });
            // remove item from the array
            delete arr[i];
        }else {
            notDoneActions.push(item);
        }
    });
    // redefine ids for not finished actions
    notDoneActions.forEach(function (action, i, arr) {action.id = i;});
    // clear actions array
    self.actions = notDoneActions;
    return snapshot;
};
/**
 * To get current module
 * @returns {module with appropriate redefined functions}
 */
MonitoredService.prototype.healthPK = function () {
    var self = this;
    return self.module;
};
/**
 * To restart the service
 */
MonitoredService.prototype.restart = function () {

};
/**
 * To check is module or not
 * @returns {true if active, false otherwise}
 */
MonitoredService.prototype.active = function () {
    throw new ModuleNotDefined("Not redefined method active");
};
/**
 * Module is notified about module's configuration changes
 *
 * @param updatedConfigurationItemsArray updated module's configuration received from server
 */
MonitoredService.prototype.configurationChanged = function(updatedConfigurationItemsArray){
    var self = this;
    updatedConfigurationItemsArray.forEach(function (item, i, arr) {
        self.configuration[item.path] = item.value;
    });
    self._applyConfigurationChanges();
};
/**
 * To apply module's configuration changes. should be redefined in module with configuration
 *
 * @private
 */
MonitoredService.prototype._applyConfigurationChanges = function () {

};
/**
 * Function to store module's output message to appropriate array
 *
 * @param messageType the type of message
 * @param whenOccurred time when output was produced
 * @param payload the payload of message
 *
 * @private
 */
MonitoredService.prototype._storeRawOutput = function(messageType, whenOccurred, payload){
    var self = this;
    var output = {
        messageType: messageType,
        whenOccurred: whenOccurred,
        payload: payload
    };
    self.rawOutput.push( output );
};
/**
 * To create the action and save it in module's database
 *
 * @param description the description of action
 * @returns {{id: Number, name: *, description: *, state: string, duration: number, startTime: null, finishTime: null, output: Array}}
 *
 * @private
 */
MonitoredService.prototype._createAction = function(description){
    var self = this;
    var action = {
        id: this.actions.length,
        description: description,
        state: "INIT",
        duration: 0,
        startTime: null,
        finishTime: null,
        output: [],
        isDone: function () {
            return this.state === "SUCCESS" || this.state === "FAIL";
        }
    };
    self.actions.push(action);
    return action;
};
/**
 * To create device for module's output
 *
 * @param outputType the type of output
 * @returns {{associateAction: associateAction, actionBegin: actionBegin, actionSuccess: actionSuccess, actionFail: actionFail, log: out}}
 */
MonitoredService.prototype.createOutputDevice = function (outputType) {
    var associatedAction = null;
    var outputType = outputType;
    var self = this;
    /**
     * Associate the action with module's output device (logger)
     *
     * @param name the name of action to associate
     * @param description action's description
     */
    var associateAction = function (description) {
        associatedAction = self._createAction(description);
    }

    /**
     * To start the action
     */
    function actionBegin() {
        if (associatedAction){
            associatedAction.state = "PROGRESS";
            associatedAction.startTime = new Date();
        }
    }

    /**
     * To finish the action successfully
     */
    function actionSuccess(){
        if (associatedAction){
            associatedAction.state = "SUCCESS";
            associatedAction.finishTime = new Date();
            associatedAction.duration = associatedAction.finishTime.getTime() - associatedAction.startTime.getTime();
        }
    }

    /**
     * To finish action unsuccessfully
     */
    function actionFail(){
        if (associatedAction){
            associatedAction.state = "FAIL";
            associatedAction.finishTime = new Date();
            associatedAction.duration = associatedAction.finishTime.getTime() - associatedAction.startTime.getTime();
        }
    }

    /**
     * output module's data related or not related to action
     */
    function out(){
        var payload = Array.prototype.slice.call(arguments).join("");
        if (associatedAction){
            var output = {
                messageType: outputType,
                whenOccurred: new Date(),
                payload: payload
            };
            associatedAction.output.push(output);
        }else {
            self._storeRawOutput(outputType, new Date(), payload);
        }
    }
    return {
            associateAction: associateAction,
            actionBegin: actionBegin,
            actionSuccess: actionSuccess,
            actionFail: actionFail,
            print: out
    };
};