# Miro Widgets API

## Prerequisites:
* All changes to widgets must occur atomically.
* Assuming 90% of load distribution is read operations.
* The Z-index must be unique.
* Environment: Java 11 / Spring Boot

## Storage Configuration
Application can be configured to run against a local (in-memory) or DB based (H2) storages. 
Storage type can be explicitly specified in the configuration file "application.yml" where 
one of two possible values should be specified: <code>local | database</code>

Alternatively the storage type can be set by specifying a Spring execution profile when 
running the server

#### Note: explicit configuration has a priority

## Build
* <code>mvn clean install</code> \
    This will compile the sources, run the tests and build the execution bundle

#### Note: Test coverage is around ~81%

## Run
There are multiple ways you can start/run the Server:
1. <code>mvn spring-boot:run</code> \
    This will start the server using the storage provider explicitly configured in the "application.yml".
     
2. <code>mvn spring-boot:run -Dspring-boot.run.profiles=database</code> \
    This will start the spring boot app with specified profile, using the corresponding storage provider 
    if is not configured in the main configuration file.
     
3. You can build a Docker image using provided Dockerfile and run the server from the container:
    * <code>docker build -t miro-widgets .</code>
    * <code>docker run -p 8080:8080 miro-widgets</code> 

