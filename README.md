Fuse integration Services on OpenShift
=======================================

With the recently released functionality in Fuse integration services on OpenShift we're able to run our integration
services in a microservice style architecture in docker containers. We've been architecting services for Fuse
like this for a long time, but now we can take advantage of the automation, process isolation, and packaging benefits
of Docker which is crucial for a microservices style deployment.

Note, this project is currently focused on using the productized (Red Hat supported) versions of the FIS (Fuse integration services) libraries and frameworks from the [http://fabric8.io](http://fabric8.io) project. The following versions of community projects can be expected in the productized release (please check the Red Hat documentation for official guidance on versions)

Project                                    | Version                   |
-------------------------------------------|:-------------------------:|
| [Apache Camel][camel]                    | 2.15.1.redhat-621084      |
| [Apache ActiveMQ][activemq]              | 5.11.0.redhat-621084      |
| [Apache CXF][cxf]                        | 3.0.4.redhat-621084       |
| [Apache Karaf maven plugin][karaf]       | 4.0.2.redhat-621079       |
| [JBoss Fuse][fuse]                       | 6.2.1.redhat-084          |
| [Fabric8 - kubernetes-model][kube-model] | 1.0.22.redhat-079         |
| [Fabric8 - kubernetes-client][kube-cli]  | 1.3.26.redhat-079         |
| [Fabric8][fabric8]                       | 2.2.0.redhat-079          |
| [iPaaS Quickstarts][quickstart]          | 2.2.0.redhat-079          |

[camel]: http://camel.apache.org
[activemq]: http://activemq.apache.org
[karaf]: http://karaf.apache.org
[fuse]: http://jboss.org/fuse
[cxf]: http://cxf.apache.org
[kube-model]: https://github.com/fabric8io/kubernetes-model 
[kube-cli]: http://github.com/fabric8io/kubernetes-client
[fabric8]: http://fabric8.io
[quickstart]: https://github.com/fabric8io/ipaas-quickstarts




The Camel routes used in this example are explained by the following diagram:

![EIP Diagram](https://raw.github.com/FuseByExample/rider-auto-osgi/master/doc/EIP_Routes_Diagram.png)



Setup
==============================

You should probably set up the basic developer tools to be able to go through these steps, examples. We will also cover more ideal workflows with developer-local setup of docker and openshift, so would be good to install the "optional" tools as well!

- Install JBoss Developer Studio 8.1.0 [https://www.jboss.org/products/devstudio.html]
- Install Apache Maven 3.2.x [http://maven.apache.org]
- Install JBoss Fuse  6.2.1 [https://www.jboss.org/products/fuse.html]
- Have access to an OpenShift installation
- (Optional, but recommended) Install the Red Hat [CDK Beta](https://access.redhat.com/downloads/content/293/ver=2/rhel---7/2.0.0/x86_64/product-software) for local development 
- (Optional, but recommended) Install the [native docker](https://docs.docker.com/engine/installation/binaries/#get-the-docker-binary) _client_ libs for your operating system
- (Optional, but recommended) Install the [native openshift](https://github.com/openshift/origin/releases) _client_ libs for your operating system

Java Build & Run
==============================

### Build this project

> <project home> $ mvn clean install


This project ends up building a few important things:

* the project artifacts 
* builds the jars as Apache Karaf bundles (if wish to deploy into karaf -- not required though)
* builds an Apache Karaf features file (if wish to deploy into karaf -- not required though)
* kubernetes.json files for each module, found in `target/classes/kubernetes.json` of each sub-module
* a single, comprehensive kubernetes.json that has all of the submodule descriptor files to install all in one found in `rider-auto-ose-installer/target/classes/kubernetes.json`

### To build and install the Docker microservices, please have a look at the docs for each module, or the all-in-one installer

* All-in-one: [rider-auto-ose-installer](rider-auto-ose-installer/README.md)
* [rider-auto-ws](rider-auto-ws/README.md)
* [rider-auto-file](rider-auto-file/README.md)
* [rider-auto-backend](rider-auto-backend/README.md)
* [rider-auto-normalizer](rider-auto-normalizer/README.md)

## Deploy A-MQ
This example uses JBoss A-MQ, so we need to have that running in the same project/namespace as the rider-auto apps (including this module).
To deploy AMQ, follow the [instructions from the xPaaS AMQ documentation](https://docs.openshift.com/enterprise/3.1/using_images/xpaas_images/a_mq.html). On the CDK, you can do this:

### Create a template for JBoss A-MQ

> oc create -f https://raw.githubusercontent.com/openshift/openshift-ansible/master/roles/openshift_examples/files/examples/v1.1/xpaas-templates/amq62-basic.json
> oc process amq62-basic -v APPLICATION_NAME=broker -v MQ_USERNAME=admin -v MQ_PASSWORD=admin 
  
Or you can use the template i've included in the root of this project:

> oc create -f amq.json

```
service "broker-amq-tcp" created
deploymentconfig "broker-amq" created
```      
Note that the user name and password need to be `admin/admin` as that's what the rider-auto-osgi project expects.



### Install on a local JBoss Fuse 6.2.1 

<JBoss Fuse home>  $ bin/fuse

1) Add this projects features.xml config to Fuse from the Console
   (makes it easier to install bundles with all required dependencies)

> JBossFuse:karaf@root>  features:addUrl mvn:org.fusesource.examples/rider-auto-common/5.0-SNAPSHOT/xml/features

2) Install the project.

> JBossFuse:karaf@root>  features:install rider-auto-osgi

3) To test the file processing, there are existing files in the
   rider-auto-common module.

> <project home> $ cp rider-auto-common/src/data/message1.xml <JBoss Fuse home>/target/placeorder

   To see what happened look at the log file, either from the console

> JBossFuse:karaf@root>  log:display

   or from the command line

> <JBoss Fuse home> $ tail -f data/log/fuse.log

4) To test the WS, use your favorite WS tool (e.g. SoapUI) against the following
   WSDL hosted by the rider-auto-ws bundle.
   * http://localhost:8182/cxf/order?wsdl

Getting Help
============================

If you hit any problems please let the Fuse team know on the forums
  [https://community.jboss.org/en/jbossfuse]
