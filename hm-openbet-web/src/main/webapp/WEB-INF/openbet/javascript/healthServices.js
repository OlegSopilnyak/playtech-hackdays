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
        var service = new HealthScannerService();
        return service;
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
};
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
    var DELAY_PATH = "health.monitor.service.external.heartbeat.delay";
    var delay = 2000;
    this.configuration[DELAY_PATH] = "2000";

    var timerId = null;
    var firstTime = true;
    var registeredModules = [this];

    /**
     * Method called when configuration was changed by server
     *
     * @private
     */
    this._configurationChanged = function(){
        var newDelay = +this.configuration[DELAY_PATH];
        if (isNaN(newDelay)) {
            // restore value by default
            delay = 2000;
            this.configuration[DELAY_PATH] = "2000";
        }else {
            // changing the value of delay
            delay = newDelay;
        }
    };
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
     * Exchange data with server
     * @param snapshot snapshot of module state
     * @returns {Array} changed configurations
     */
    function exchange(snapshot) {
        var json = JSON.stringify(snapshot, null, 2);
        json = json.replace('\\"', '"');
        console.log("Sending: "+json);
        var configurationItem = {
            path: DELAY_PATH,
            type: "STRING",
            value: "1300s"
        };
        return [configurationItem];
    }
    /**
     * To start scan
     */
    this.start = function () {
        var self = this;
        var logger = this.createOutputDevice("log");
        console.log("Starting scaner...");
        logger.print("Starting scaner...");
        function processRegistered() {
            logger = self.createOutputDevice("log");
            logger.print("Processing ",registeredModules.length," module(s) ...");
            console.log("Processing "+registeredModules.length+" module(s) ...");
            registeredModules
                .filter(function (module) {

                    // logging begin
                    var moduleDescription = module.healthPK().description();
                    logger.associateAction("checkModule", "Checking the module "+ moduleDescription);
                    console.log("Processing module : "+moduleDescription);
                    logger.print("Processing module : ",moduleDescription);
                    // logging end

                    return firstTime || timerId != null;
                })
                .forEach(function (module, i, arr) {
                    logger.actionBegin();
                    var moduleDescription = module.healthPK().description();
                    try {

                        console.log("grabbing module's snapshot... ");
                        logger.print("grabbing module's snapshot... ");

                        var snapshot = module.snapshot();

                        console.log("grabed module's snapshot... ");
                        logger.print("grabed module's snapshot... ");

                        var updatedConfiguration = exchange(snapshot);
                        snapshot = null;

                        if (updatedConfiguration.length > 0) {

                            console.log("updating module " + moduleDescription);
                            logger.print("updating module ", moduleDescription);
                            module.configurationChanged(updatedConfiguration);
                        }
                        console.log("Check is module works well active:" + module.active());
                        logger.print("Check is module works well active:", module.active());
                        if (!module.active()) {
                            console.log("Module restarting....");
                            logger.print("Module restarting....");
                            module.restart();
                        }
                        logger.print("Module ", module.healthPK().description(), " verified well.");
                        logger.actionSuccess();
                    }catch (err){
                        console.log("Check of ["+moduleDescription+"] interrupted", err)
                        logger.print("Check interrupted", err);
                        logger.actionFail();
                    }
                }
            );
            if (firstTime || timerId != null) {
                console.log("Next iteration in "+delay+" ms.");
                logger.print("Next iteration in "+delay+" ms.");
                timerId = setTimeout(processRegistered, delay);
            }else{
                console.log("Service stopped.");
                logger.print("Service stopped.");
            }
        }
        processRegistered();
        firstTime = false;
    }
    /**
     * To check is service works
     * @returns {boolean} true if works
     */
    this.isWorks = function () {return firstTime || timerId != null;}
    /**
     * To stop scan
     */
    this.stop = function (){
        if (timerId){
            clearTimeout(timerId);
            timerId = null;
            firstTime = true;
        }
    }
};
HealthScannerService.prototype = Object.create(MonitoredService.prototype);
HealthScannerService.prototype.constructor = HealthScanner;
HealthScannerService.prototype.active = function () {return this.isWorks();};
HealthScannerService.prototype.restart = function () {this.stop(); this.start();};
HealthScannerService.prototype._applyConfigurationChanges = function () {
    this._configurationChanged();
};

HealthScanner.getInstance().start();

