# Dropwizard Smart Logging Bundle

## Changelog

* v0.8.0: Dependency updates including dropwizard 2.0.28 and bring in dependency check and spotbugs plugins
* v0.7.0: Update to dropwizard 2.0.20 and replace deprecated io.dropwizard.Bundle interface
* v0.6.0: Update to dropwizard 1.3.13 and to be a java 11 project
* v0.5.0: Update dropwizard to 1.1.1
* v0.4.9: Considering only GET requests for healthcheck URLs
* v0.4.8: Configurable healthcheck URLs
* v0.4.6: Not swallowing root cause of exception

## Why

APIs are great. Microservices are great. But how do we keep track of a user who uses multiple APIs throughout a session? 
One approach is to add an additional value to each request i.e. user_id or session_id. This forces every API call to have 
an additional id attached to it (if available) and somewhere in the code we need to assign this to a log message.

Wouldn't it be easier to let an API client send identifiable information to the host via a header?

It would also be nice to be able to configure some extra fields to appear in every log message.

## How

Add the corresponding configuration to your config file (MyServiceConfiguration in this example) and implement interface

```java

    public class MyServiceConfiguration extends Configuration implements PrependLogConfiguration
    
    @JsonProperty
    private SmartLogging smartLogging;
```

Specify what header and any extra fields you want to log in your requests by adding this to your configuration:

```YAML
smartLogging:
    useHeader: X-REQ-ID
    extraFields:
      someExtraKey: someExtraValue
    
    requestLoggingFilter:
      excluded: 
        - "^/healthcheck$"

```

This bundle can be added to a dropwizard app using:

```Java
    @Override
    public void initialize(Bootstrap<MyServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new PrependLogBundle());
    }
```

## Clearing MDC on each request

This is important to avoid MDC context leaking through request threads.

```Java
    @Override
    public void initialize(Bootstrap<MyServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new MdcClearingBundle());
    }
```

## Json logging
This bundle now includes (as of version 0.4.3) a json logging encoder which produces logs formatted for logstash. To use simply define a json appender in your dropwizard config file as follows.

Add your new log format to your chosen appender

```YAML
- 
    type: json
    appname: my-name
    apphostname: localhost
    appsecurityzone: dap
    apptype: dropwizard
    appenvironment: dev
    threshold: DEBUG
    currentLogFilename: ./logs/rest-dap-application-store.log
    archivedLogFilenamePattern: ./logs/rest-dap-application-store-%d.log.gz
    archivedFileCount: 5
```

## Security

Be careful of what you log, make sure you validate headers to ensure your logs don't become a potential attack
vector.
