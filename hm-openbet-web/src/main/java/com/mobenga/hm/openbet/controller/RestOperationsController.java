package com.mobenga.hm.openbet.controller;

import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.dto.MonitorOperation;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller of REST requests
 */
@RestController
@RequestMapping(value = "/monitor")
public class RestOperationsController {
    private static final Logger LOG = LoggerFactory.getLogger(RestOperationsController.class);

    @Autowired
    private OpenbetOperationsManipulationService storage;

    @RequestMapping( value = "/openbet/operations",method = RequestMethod.POST)
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<MonitorOperation> searchOperations(@RequestBody MonitorCriteria criteria){
        LOG.debug("Searching operations by '{}'", criteria);
        return storage.selectOperationsByCriteria(criteria);
    }

    @RequestMapping( value = "/openbet/operations/count",method = RequestMethod.POST)
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public long countOpeartions(@RequestBody MonitorCriteria criteria){
        LOG.debug("Counting operations by '{}'", criteria);
        return storage.countOperationByCriteria(criteria);
    }

    @RequestMapping( value = "/openbet/types",method = RequestMethod.GET, consumes = {MediaType.APPLICATION_JSON_VALUE} )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<String> operationTypes(){
        LOG.debug("Reporting about supported operations types");
        return storage.supportedOperationTypes();
    }

}

