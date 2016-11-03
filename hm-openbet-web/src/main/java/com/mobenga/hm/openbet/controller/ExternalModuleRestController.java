package com.mobenga.hm.openbet.controller;

import com.mobenga.hm.openbet.dto.ConfigurationUpdate;
import com.mobenga.hm.openbet.dto.ExternalModulePing;
import com.mobenga.hm.openbet.dto.ModuleConfigurationItem;
import com.mobenga.hm.openbet.service.ExternalModuleSupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * The controller for external modules
 */
@Controller
@RequestMapping("/module")
public class ExternalModuleRestController {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalModuleRestController.class);

    @Autowired
    private ExternalModuleSupportService moduleSupport;

    @RequestMapping( value = "/ping",method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<ModuleConfigurationItem> exchange(@RequestBody ExternalModulePing ping, HttpServletRequest request){
        ping.setHost(request.getRemoteHost());
        LOG.debug("Received ping '{}'", ping);
        return moduleSupport.pong(ping);
    }

    @RequestMapping( value = "/update",method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public ModuleConfigurationItem change(@RequestParam("module") String module, @RequestParam("path")String path, @RequestParam("value")String value){
        LOG.debug("Changing config item for '{}' '{}' to '{}'", module, path, value);
        return moduleSupport.changeConfigurationItem(module, path, value);
    }

    @RequestMapping( value = "/batchUpdate",method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    public List<ModuleConfigurationItem> change(@RequestBody ConfigurationUpdate update, HttpServletRequest request){
        update.setHost(request.getRemoteHost());
        LOG.debug("Received batch update '{}'", update);
        return moduleSupport.changeConfiguration(update);
    }
}
