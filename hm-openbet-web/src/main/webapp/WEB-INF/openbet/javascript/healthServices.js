/*
 * The services used to support module's health
 */
'use strict';
/**
 * Singleton of the service
 *
 * @type {{getInstance}}
 */
var HealthScanner = (function () {
    var instance;

    function createInstance() {
        return new HealthScannerService();
    }

    return {
        getInstance: function () {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();
/**
 * The module of service
 *
 * @constructor
 */
function HealthScannerModule() {
    Module.call(this, "HealthMonitor");
    this.sysId = "HealthMonitor";
    this.appId = "RegisteredModulesScanner";
    this.ver = "0.01";
    this.descr = "Service to scan registered modules and manage them";
}
HealthScannerModule.prototype = Object.create(Module.prototype);
HealthScannerModule.prototype.constructor = HealthScannerModule;
HealthScannerModule.prototype.systemId = function () {return this.sysId;};
HealthScannerModule.prototype.applicationId = function () {return this.appId;};
HealthScannerModule.prototype.version = function () {return this.ver;};
HealthScannerModule.prototype.description = function () {return this.descr;};

/**
 * Service to scan registered modules and manage them
 *
 * @constructor
 */
function HealthScannerService() {
    MonitoredService.call(this, new HealthScannerModule());
    var self = this;

    var DELAY_PATH = "health.monitor.service.external.heartbeat.delay";
    var delay = 2000;

    var PING_URL_PATH = "health.monitor.service.external.heartbeat.ping.url";
    var pingUrl = "module/ping";

    var timerId = null;
    var firstTime = true;
    var registeredModules = [this];

    // define external configuration parameters
    this.configuration[DELAY_PATH] = "2000";
    this.configuration[PING_URL_PATH] = "module/ping";

    /**
     * To register module for scan
     *
     * @param module to register
     */
    this.register = function(module){
        if (module instanceof MonitoredService){
            var modules = [];
            registeredModules.forEach(function (m) {modules.push(m);});
            modules.push(module);
            registeredModules = modules;
        }
    };
    /**
     * To unregister module for scan
     *
     * @param module to unregister
     */
    this.unregister = function (module) {
        if (module instanceof MonitoredService){
            var modules = [];
            registeredModules.filter(function (m) {return m != module;}).forEach(function (m) {modules.push(m);});
            registeredModules = modules;
        }
    };
    /**
     * To start scan
     */
    this.start = function () {
        var self = this;
        var logger = self.createOutputDevice("log");
console.log("Starting modules scanner...");
        logger.print("Starting scanner...");
        firstTime = true;
        function processRegistered() {
            logger = self.createOutputDevice("log");
            logger.print("Processing ",registeredModules.length," module(s) ...");
// console.log("Processing "+registeredModules.length+" module(s) ...");
            // iteration of registered modules array
            registeredModules
                .filter(function (module) {

                    // logging begin
                    var moduleDescription = module.healthPK().description();
                    logger.associateAction("Checking the module "+ moduleDescription);
console.log("Processing module : "+ moduleDescription);
                    logger.print("Processing module : ",moduleDescription);
                    // logging end

                    return firstTime || timerId != null;
                })
                .forEach(function (module) {
                    var moduleDescription = module.healthPK().description();
                    // start the action "checkModule"
                    logger.actionBegin();
                    try {

// console.log("grabbing module's snapshot... ");
                        logger.print("grabbing module's snapshot... ");
                        // grab module's snapshot
                        var snapshot = module.snapshot();

// console.log("grabbed module's snapshot... ");
                        logger.print("grabbed module's snapshot... ");

                        // send POST request and register response's handlers
                        ping(snapshot, processSuccessPong, processFail, logger);
                        snapshot = null;
                    }catch (err){
// console.log("Check of ["+moduleDescription+"] interrupted", err);
                        logger.print("Check interrupted", err);
                        // action for particular module is failed (bad request)
                        logger.actionFail();
                    }

                    //////////////////////////////////////////////////////////
                    ///////////  private functions and methods   /////////////
                    //////////////////////////////////////////////////////////
                    /**
                     * Callback function for module's ping request
                     *
                     * @param data received data (module's configuration updates (array of configurationItem(s)))
                     */
                    function processSuccessPong (data) {
                        if (data.length > 0) {

// console.log("updating module " + moduleDescription);
                            logger.print("updating module ", moduleDescription);
                            // updating module's configuration
                            module.configurationChanged(data);

// console.log("updated module " + moduleDescription);
                            logger.print("updated module ", moduleDescription);
                        }
// console.log("Check is module works well active:" + module.active());
                        logger.print("Check is module works well active:", module.active());
                        if (!module.active()) {

// console.log("Module restarting....");
                            logger.print("Module restarting....");
                            // restarting passive module
                            module.restart();

// console.log("Module restarted.");
                            logger.print("Module restarted.");
                        }
                        logger.print("Module ", moduleDescription, " verified well.");
console.log("Module " + moduleDescription + " verified well.");
                        // action for particular module is success
                        logger.actionSuccess();
                    }
                    /**
                     * Process the ping request's fail
                     *
                     * @param req request instance
                     * @param status status of request
                     * @param err error obkect
                     */
                    function processFail(req, status, err) {
// console.log("For module "+moduleDescription+" Received error "+err);
                        logger.print("For module ",moduleDescription," Received error ",err);
console.log("Module " + moduleDescription + " verified fail.");
                        // action for particular module is failed (bad response)
                        logger.actionFail();
                    }
                }
            );
            if (firstTime || timerId != null) {
// console.log("Next iteration in "+delay+" ms.");
                logger.print("Next iteration in "+delay+" ms.");
                // start the new iteration
                timerId = setTimeout(processRegistered, delay);
            }else{
                // service finished
console.log("Service-scanner stopped.");
                logger.print("Service stopped.");
            }
        }
        processRegistered();
        firstTime = false;
    };
    /**
     * To check is service works
     * @returns {boolean} true if works
     */
    this.isWorks = function () {return firstTime || timerId != null;};
    /**
     * To stop scan
     */
    this.stop = function (){
        if (timerId){
            // forced service shutdown
            clearTimeout(timerId);
            timerId = null;
            firstTime = true;
        }
    };

    //////////////////////////////////////////////////////////
    ///////////  private functions and methods   /////////////
    //////////////////////////////////////////////////////////
    /**
     * Method called when configuration was changed by server
     *
     * @private
     */
    this._configurationChanged = function(){
        var logger = this.createOutputDevice("log");
        logger.associateAction("Received notification about module's configuration changes.");
        logger.actionBegin();
        // processing the "delay" attribute
        checkDelayChangeConfiguration(logger);
        // processing "pingUrl" attribute
        checkPingUrlChangeConfiguration(logger);
        logger.actionSuccess();
    };

    /**
     * Exchange data with server
     * @param snapshot snapshot of module state
     * @returns {Array} changed configurations
     */
    /**
     * Exchange data with server
     *
     * @param snapshot snapshot of module state
     * @param onSuccess callback function for exchange's success
     * @param onFail callback function for exchange's fail
     * @param logger scanner-module's output logger
     */
    var ping = function(snapshot, onSuccess, onFail, logger) {
        var json = JSON.stringify(snapshot);
        logger.print("Send ping request for ", snapshot.module.key());
// console.log("Sending: "+json);
        $.ajax({
            url: pingUrl,
            data: json,
            contentType: "application/json",
            dataType: 'json',
            type: 'POST'
        }).then(onSuccess, onFail);
    };

    /**
     * To check changes for "delay"
     */
    var checkDelayChangeConfiguration = function(logger) {
        logger.print("Check changes for ", DELAY_PATH);
        var newValue = +self.configuration[DELAY_PATH];
        if (isNaN(newValue)) {
            // restore value by default
// console.log("Restore value to default.");
            logger.print("Restore value to default.");
            delay = 2000;
            self.configuration[DELAY_PATH] = "2000";
        } else {
            logger.print("Change value of ",DELAY_PATH, " to ", newValue);
// console.log("Change value to " + newValue);
            // changing the value of delay
            delay = newValue;
        }
    };

    /**
     * To check changes for "pingUrl"
     */
    var checkPingUrlChangeConfiguration = function(logger) {
        logger.print("Check changes for ", PING_URL_PATH);
        var newValue = self.configuration[PING_URL_PATH];
        if (newValue) {
            // changing the value of pingUrl
            logger.print("Change value of ",PING_URL_PATH, " to ", newValue);
// console.log("Change value to " + newValue);
            pingUrl = newValue;
        } else {
            // do nothing
// console.log("Do nothing.");
        }
    };
}
HealthScannerService.prototype = Object.create(MonitoredService.prototype);
HealthScannerService.prototype.constructor = HealthScannerService;
HealthScannerService.prototype.active = function () {return this.isWorks();};
HealthScannerService.prototype.restart = function () {this.stop(); this.start();};
HealthScannerService.prototype._applyConfigurationChanges = function () {
    this._configurationChanged();
};

HealthScanner.getInstance().start();

