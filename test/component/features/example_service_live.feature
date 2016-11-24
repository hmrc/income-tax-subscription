Feature: Fetch Example for Live

  Background:
    Given header 'Accept' is 'valid'

  Scenario: Fetch World for Live
    And I GET the LIVE resource '/hello-world'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello World"
    }
    """


  Scenario: Fetch User for Live
    And I GET the LIVE resource '/hello-user'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello User"
    }
    """

  Scenario: Fetch Application for Live
    And I GET the LIVE resource '/hello-application'
    Then the status code should be 'OK'
    And I should receive JSON response:
    """
    {
      "message": "Hello Application"
    }
    """
