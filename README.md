[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Build Status](https://travis-ci.org/hmrc/income-tax-subscription.svg?branch=master)](https://travis-ci.org/hmrc/income-tax-subscription) [ ![Download](https://api.bintray.com/packages/hmrc/releases/income-tax-subscription/images/download.svg) ](https://bintray.com/hmrc/releases/income-tax-subscription/_latestVersion)

# Income Tax Subscription MicroService

This is a scala/Play protected backend MicroService for the Sign Up to Report your Income and Expenses Quarterly (MTD ITSA) service.

This service provides the backend interactions with other backend MicroServices.

Local development requires:

  * [sbt](http://www.scala-sbt.org/)
  * MongoDB available on port 27017
  * HMRC Service manager (if using the provided scripts)
    * [Install Service-Manager](https://github.com/hmrc/service-manager/wiki/Install#install-service-manager)**
  * The services in the ITSA_SUBSC_ALL profile (a subset can be used)

# How to start this service (main section)

See `scripts/start`

The active port is 9560

# How to use

The entry page for this service running locally is

  http://localhost:9561/report-quarterly/income-and-expenses/sign-up

The entry page for this service on staging (requires HMRC VPN) is

  https://www.staging.tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up

# How to test

There are two built in test sets: `test` and `it:test`. See build.sbt for details.

# Persistence

Data is stored as key/value in Mongo DB. See json reads/writes implementations (especially tests) for details.

To connect to the mongo db provided by docker (recommended) please use

```
docker exec -it mongo-db mongosh
```

### License
  
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
   

