package com.mobenga.hm.openbet.behavior.test.first;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * Describe the steps of first.feature
 */
@ContextConfiguration({"/cucumber.xml"})
public class FirstSteps {
    private final static Logger LOG = LoggerFactory.getLogger(FirstSteps.class);
    
    private String warrior;
    @Given("^the ninja has a third level black-belt$")
    public void the_ninja_has_a_third_level_black_belt(){
        LOG.info("Given works.");
        System.out.println("Given works.");
    }
    @When("^attacked by \"(.*?)\"$")
    public void attacked_by(String braveMan){
        LOG.info("Under attack of ",warrior = braveMan);
        System.out.println("Under attack of ["+warrior+"]");
    }
    @Then("^the ninja should apologise$")
    public void the_ninja_should_apologise(){
        LOG.info("Forgive us the warrior:", warrior);
        System.out.println("Forgive us the warrior:"+ warrior+" please...");
    }
}
