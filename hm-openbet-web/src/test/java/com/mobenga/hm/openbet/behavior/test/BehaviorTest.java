package com.mobenga.hm.openbet.behavior.test;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * Behavior test
 */
@RunWith(Cucumber.class)
@Cucumber.Options(
        format={"pretty", "html:target/cucumber"}
)
public class BehaviorTest { 
}
