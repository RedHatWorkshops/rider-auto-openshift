NOTE: If you need the example from the "Getting Started with Apache Servicemix" 
webinar, please use the code from the rider-auto-osgi-smx-4.4.1-fuse-00-08 tag instead.



Example from "Getting Started with Apache Servicemix" webinar
========================================================

The Camel routes used in this example are explained by the following diagram:

![EIP Diagram](https://raw.github.com/FuseByExample/rider-auto-osgi/master/doc/EIP_Routes_Diagram.png)

To run this example project build the project and deploy to ServiceMix  
according to the steps below. 

Setup
==============================

- Install JBoss Developer Studio 8.1.0 [https://www.jboss.org/products/devstudio.html]
- Install Apache Maven 3+ [http://maven.apache.org]
- Install JBoss Fuse  6.2.x [https://www.jboss.org/products/fuse.html]

Build & Run
==============================

1) Build this project so bundles are deployed into your local maven repo

<project home> $ mvn clean install

2) Start JBoss Fuse

<JBoss Fuse home>  $ bin/fuse

3) Add this projects features.xml config to Fuse from the Console
   (makes it easier to install bundles with all required dependencies)

JBossFuse:karaf@root>  features:addUrl mvn:org.fusesource.examples/rider-auto-common/4.0-SNAPSHOT/xml/features

4) Install the project.

JBossFuse:karaf@root>  features:install rider-auto-osgi

5) To test the file processing, there are existing files in the
   rider-auto-common module.

<project home> $ cp rider-auto-common/src/data/message1.xml <JBoss Fuse home>/target/placeorder

   To see what happened look at the log file, either from the console

JBossFuse:karaf@root>  log:display

   or from the command line

<JBoss Fuse home> $ tail -f data/log/fuse.log

6) To test the WS, use your favorite WS tool (e.g. SoapUI) against the following
   WSDL hosted by the rider-auto-ws bundle.
   * http://localhost:8182/cxf/order?wsdl

Getting Help
============================

If you hit any problems please let the Fuse team know on the forums
  [https://community.jboss.org/en/jbossfuse]
