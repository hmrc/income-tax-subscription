Feature: Fetch Hello Sandbox Sandbox

  Background:
    Given header 'Accept' is 'valid'

  Scenario: Fetch World for Live
    And I GET the SANDBOX resource '/sandbox/income-tax/hello-world'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello Sandbox World"
    }
    """


  Scenario: Fetch User for Live
    And I GET the SANDBOX resource '/sandbox/income-tax/hello-user'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello Sandbox User"
    }
    """

  Scenario: Fetch Application for Live
    And I GET the SANDBOX resource '/sandbox/income-tax/hello-application'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello Sandbox Application"
    }
    """