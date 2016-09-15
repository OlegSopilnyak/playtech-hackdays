package com.mobenga.hm.openbet.controller;

import com.mobenga.hm.openbet.dto.MonitorCriteria;
import com.mobenga.hm.openbet.dto.MonitorOperation;
import com.mobenga.hm.openbet.service.OpenbetOperationsManipulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller of REST requests
 */
@Controller
@RequestMapping("/monitor")
public class RestOperationsController {

    @Autowired
    private OpenbetOperationsManipulationService storage;

    @RequestMapping( value = "/openbet/operations",method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<MonitorOperation> searchOperartions(@RequestBody MonitorCriteria criteria){
        return storage.selectOperationsByCriteria(criteria);
    }

    @RequestMapping( value = "/openbet/operations/count",method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public long countOpeartions(@RequestBody MonitorCriteria criteria){
        return storage.countOperationByCriteria(criteria);
    }

    @RequestMapping( value = "/openbet/types",method = RequestMethod.GET )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<String> operationTypes(){
        return storage.supportedOperationTypes();
    }

}

