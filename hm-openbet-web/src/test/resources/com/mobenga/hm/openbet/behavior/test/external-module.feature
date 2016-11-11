#----------------------------------
# Cucumber .feature file for test behavior of external modules service
#----------------------------------

@RunWith
Feature: External Modules Communication

  # External module ping pong scenario
  Scenario: External Module pings the Service
    Given test-external-module which ping the Service

