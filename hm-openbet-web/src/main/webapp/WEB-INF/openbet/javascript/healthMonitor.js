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
    var snapshot = {
        module:{
            systemId : this.module.systemId(),
            applicationId: this.module.applicationId(),
            versionId: this.module.version(),
            description: this.module.description()
        },
        host: "localhost",
        state: this.active() ? "active" : "passive",
        configuration: [],
        output: [],
        actions: []
    };
    // iterate the configuration
    for(var key in this.configuration){
        var configurationItem = {
            path: key,
            type: "STRING",
            value: this.configuration[key]
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
    this.rawOutput = [];

    // iterate actions output
    var notDoneActionsCounter = 0;
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
            notDoneActionsCounter++;
        }
    });
    // clear actions array
    if (notDoneActionsCounter == 0){
        this.actions = [];
    }
    return snapshot;
};
/**
 * To get current module
 * @returns {module with appropriate redefined functions}
 */
MonitoredService.prototype.healthPK = function () {
    return this.module;
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
 * @param updatedConfiguration updated module's configuration received from server
 */
MonitoredService.prototype.configurationChanged = function(updatedConfiguration){
    for(var path in updatedConfiguration){
        this.configuration[path] = updatedConfiguration[path];
    }
    this._applyConfigurationChanges();
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
    var output = {
        messageType: messageType,
        whenOccurred: whenOccurred,
        payload: payload
    };
    this.rawOutput.push( output );
};
/**
 * To create the action and save it in module's database
 *
 * @param name the name of action
 * @param description the description of action
 * @returns {{id: Number, name: *, description: *, state: string, duration: number, startTime: null, finishTime: null, output: Array}}
 *
 * @private
 */
MonitoredService.prototype._createAction = function(name, description){
    var action = {
        id: this.actions.length,
        name: name,
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
    this.actions.push(action);
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
    /**
     * Associate the action with module's output device (logger)
     *
     * @param name the name of action to associate
     * @param description action's description
     */
    var associateAction = function (name, description) {
        associatedAction = this._createAction(name, description);
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
            this._storeRawOutput(outputType, new Date(), payload);
        }
    }
    return {
            associateAction: associateAction,
            actionBegin: actionBegin,
            actionSuccess: actionSuccess,
            actionFail: actionFail,
            log: out
    };
};