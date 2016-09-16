package com.mobenga.hm.openbet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller of application's HTTP requests
 */
@Controller
@RequestMapping("/i")
public class ApplicationController {
//    @Autowired
//    private OpenBetOperationStorageImpl operationStorage;

    @Autowired
    private ApplicationContext appContext;
//    @RequestMapping(method = RequestMethod.GET)
//    public ModelAndView gotoIndex(){
//        ModelAndView view = new ModelAndView();
//        view.setViewName("index"); //name of the jsp-file in the "jsp" folder
//        return view;
//    }
    @RequestMapping(method = RequestMethod.GET)
    public String  gotoIndex(Model model){
        return "/index";
    }
}
