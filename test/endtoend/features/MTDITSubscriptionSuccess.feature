@MTDITSuccess

Feature: MTD IT Subscription Success

  Background: User has token
    Given The user has a valid access token

  Scenario Outline: Subscribe to the MTD IT service and receive a mocked successful subscription response
    Given Agreement to Terms & Conditions is 'true'
    And A valid email address has been submitted as '<email>'
    When I access the MTD IT Subscription API endpoint
    Then The response code is status 201
    Examples:
      | email                                                                  |
      | firstnamez.lastnamez@domain.co.uk                                      |
      | “email”@"domain".test.com                                              |
      | email-@123.456.789.012                                                 |
      | email_@_test-one.com                                                   |
      | 1234567890@test.com                                                    |
      | 1@1.co.uk                                                              |
      | firstname11.middlename11.lastname1@maximumlengthtestingof70chars.co.uk |
      | firstname2.middlename11.middlename33.middlename4.middle5.70chars@1.com |
      | first@name1.middlename11.lastname1@maximumlengthtestingof70chars.co.uk |



#  SVR2300_Email_Address_Format
#  The current valid format is two strings of between 1 & 64 allowed characters separated by one @ sign.
#  The allowed characters are; A to Z, a to z, 0 to 9, period ('.'), hyphen ('-') & underscore ('_'). The overall length is limited to 70 characters.

