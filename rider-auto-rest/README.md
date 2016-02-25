# REST module

This module exposes a REST endpoint using Camel's rest dsl and returns information about the host it's running on.  


## Building

You can build this module with Maven:

> mvn clean install

If you'd like to skip tests:

> mvn clean install -Dtest=false -DfailIfNoTests=false

## Running locally
You should be able to run this locally using mvn, and it should work as expected. We highly recommend you test your services out locally before packaging as Docker containers:

> mvn camel:run

You can then test that it works by using a SOAP tool, or hitting it in a web browser:

> curl http://localhost:8080/demo

You should end up with the WSDL returned:

    ceposta@postamac(~) $ curl http://localhost:8080/demo
    {
      "timestamp" : 1456410711899,
      "hostIP" : "10.0.1.20",
      "hostname" : "postamac.local"
    } 
    
    
You may also want to try the same thing using `mvn exec:java`

>  mvn exec:java

This will run the app's main file (from [camel-boot](camel-boot)) using the classpath from maven.

## Building a docker image

FIS includes a supported maven plugin for building Docker images from your project. Unless you're running on Linux, you'll need to install a guest VM that has Docker. We recommend the Red Hat [Container Development Kit v2](https://www.redhat.com/en/about/blog/introducing-red-hat-container-development-kit-2-beta)

For the maven plugin to work, it will need to be able to locate a Docker Daemon (ideally running in a guest-local VM). To do this, you'll want to have the following environment variables set:

    export DOCKER_HOST=tcp://10.1.2.2:2376
    export DOCKER_CERT_PATH=/path/to/cdk/.vagrant/machines/default/virtualbox/.docker
    export DOCKER_TLS_VERIFY=1
    
Note the `DOCKER_HOST` needs to point to the location of the docker daemon, and `DOCKER_CERT_PATH` needs to point to the location of the cert for your docker daemon.

It would also be great if you had the native docker CLI tools installed on your Host machine (ie your Windows or Mac machine) so you can `docker images` and `docker ps`

### Running as camel-boot microservice

Before we build the docker image, we should specify whether we're going to use [camel-boot][camel-boot] (Plain Old Java Main with flat-classloader) or a Karaf osgi classloader. We recommend building using [camel-boot][camel-boot]. Basically what happens is we zip up the maven classpath and inject a little helper script to run the app using the classpath. This simplifies the deployment so we don't have to guess about which classpath we'll be using; it'll be exactly the same as when you ran locally.

To do this, run the following command:

> $ mvn clean install -Pfabric8 -Phawtapp 

(Skip tests can be done with adding `-Dtest=false -DfailIfNoTests=false`)

After the build you should see the following in the target dir:
  
```  
drwxr-xr-x  6 ceposta  staff   204B Feb 25 07:13 classes
drwxr-xr-x  3 ceposta  staff   102B Feb 25 07:27 docker
drwxr-xr-x  3 ceposta  staff   102B Feb 25 07:08 fabric8
drwxr-xr-x  3 ceposta  staff   102B Feb 25 07:13 generated-sources
drwxr-xr-x  4 ceposta  staff   136B Feb 25 07:25 hawt-app
drwxr-xr-x  3 ceposta  staff   102B Feb 25 07:04 maven-status
-rw-r--r--  1 ceposta  staff    49M Feb 25 07:32 rider-auto-rest-5.0-SNAPSHOT-app.tar.gz
-rw-r--r--  1 ceposta  staff   7.5K Feb 25 07:32 rider-auto-rest-5.0-SNAPSHOT.jar
```

Notice the *.tar.gz file (can also generate zip file by setting the hawtapp-maven-plugin archiver to "zip").
In that zip/tar file, you'll find a completely packed up and ready to run app. For example if you unzip that file, you'll get the following contents:

```
  drwxr-xr-x    3 ceposta  staff   102B Jan 26 08:39 bin
  drwxr-xr-x  200 ceposta  staff   6.6K Jan 26 08:39 lib

```

From that folder, you can run:

> ./bin/run.sh
  
Which should bootstrap the application and run it as a standalone camel-boot app.

### Building the camel-boot service as a docker container

Now that you understand the camel-boot packaging using hawtapp-maven-plugin, we can build a docker container:

> $ mvn clean install -Pfabric8 -Phawtapp docker:build
  
(Skip tests can be done with adding `-Dtest=false -DfailIfNoTests=false`)
  
Note, we have to have a working docker daemon available as mentioned above.

You should end up with output similar to this:

```
[INFO] --- docker-maven-plugin:0.13.6:build (default-cli) @ rider-auto-rest ---
[INFO] Copying files to /Users/ceposta/dev/sandbox/RedHatWorkshops/rider-auto-openshift/rider-auto-rest/target/docker/fabric8/rider-auto-rest/5.0-SNAPSHOT/build/maven
[INFO] Building tar: /Users/ceposta/dev/sandbox/RedHatWorkshops/rider-auto-openshift/rider-auto-rest/target/docker/fabric8/rider-auto-rest/5.0-SNAPSHOT/tmp/docker-build.tar
[INFO] DOCKER> [fabric8/rider-auto-rest:5.0-SNAPSHOT] : Built image 50b666dd2bd3
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 17.368 s
[INFO] Finished at: 2016-02-25T07:35:50-07:00
[INFO] Final Memory: 53M/889M
[INFO] ------------------------------------------------------------------------
  
```  
  
Now if you do a `docker images` you should see your new docker image:

```
  ceposta@postamac(rider-auto-rest (fis-enable)) $ docker images
  REPOSITORY                                                                      TAG                 IMAGE ID            CREATED              VIRTUAL SIZE
  fabric8/rider-auto-rest                                                           5.0-SNAPSHOT        f140fa83fc04        About a minute ago   491.7 MB
```

You can even try to run you docker container as is and map the ports locally so you can see the service running within the docker image:

> docker run -it --rm -p 8080:8080 fabric8/rider-auto-rest:5.0-SNAPSHOT
> curl http://localhost:8080/demo

Note for that to work, we need to have the guest VM map port 8080 to the host VM.

Yay! You now have your microservice packaged as a docker image ready to go. Let's take a look at what that looks like if you want to build the karaf-based microservice:


### Running as Karaf-based microservice

We can also convert existing karaf-based deployments over to the FIS deployment model. Just like with the camel-boot option (above) that packages the entire JVM together with its dependencies, we'll be doing that with Karaf. What this means is that your build will _actually produce a completely independent Karaf assembly_ which can then be used to run your application. No more build your app and chuck it into a running Karaf and hope it resolves; now all OSGI resolution is done at build time and the resulting output is a fully baked Karaf distribution with your app inside it. 

Run the following command to do this:


>  mvn clean install -Pfabric8 -Pkaraf-distro

(Skip tests can be done with adding `-Dtest=false -DfailIfNoTests=false`)

That should produce output like this in /target

```
  drwxr-xr-x  7 ceposta  staff   238B Jan 26 08:58 assembly
  drwxr-xr-x  6 ceposta  staff   204B Jan 26 08:58 classes
  drwxr-xr-x  3 ceposta  staff   102B Jan 26 08:58 fabric8
  drwxr-xr-x  3 ceposta  staff   102B Jan 26 08:58 generated-sources
  drwxr-xr-x  3 ceposta  staff   102B Jan 26 08:58 generated-test-sources
  drwxr-xr-x  3 ceposta  staff   102B Jan 26 08:58 maven-status
  -rw-r--r--  1 ceposta  staff   6.8K Jan 26 08:58 rider-auto-ws-5.0-SNAPSHOT.jar
  -rw-r--r--  1 ceposta  staff    48M Jan 26 08:59 rider-auto-ws-5.0-SNAPSHOT.tar.gz
  -rw-r--r--  1 ceposta  staff    49M Jan 26 08:59 rider-auto-ws-5.0-SNAPSHOT.zip
  drwxr-xr-x  4 ceposta  staff   136B Jan 26 08:58 test-classes
```  
  
  
The *.zip file is the fully-baked karaf assembly. If you unzip it, it looks like a Karaf/Fuse distribution as you'd expect:

```
  drwxr-xr-x  20 ceposta  staff   680B Jan 26 08:58 bin
  drwxr-xr-x   3 ceposta  staff   102B Jan 26 08:58 data
  drwxr-xr-x  45 ceposta  staff   1.5K Jan 26 08:58 etc
  drwxr-xr-x  13 ceposta  staff   442B Jan 26 08:58 lib
  drwxr-xr-x   7 ceposta  staff   238B Jan 26 08:58 system
```
  
Can run `./bin/karaf` to boot up the karaf distro.


### Building the karaf-based service as a docker container

> $ mvn clean install -Pfabric8 -Pkaraf-distro docker:build
  
(Skip tests can be done with adding `-Dtest=false -DfailIfNoTests=false`)
  
Note, we have to have a working docker daemon available as mentioned above.

You should end up with output similar to this:

```
  [INFO] 
  [INFO] --- docker-maven-plugin:0.13.6:build (default-cli) @ rider-auto-ws ---
  [INFO] Copying files to /Users/ceposta/dev/sandbox/RedHatWorkshops/rider-auto-osgi/rider-auto-ws/target/docker/fabric8/rider-auto-rest/5.0-SNAPSHOT/build/maven
  [INFO] Building tar: /Users/ceposta/dev/sandbox/RedHatWorkshops/rider-auto-osgi/rider-auto-rest/target/docker/fabric8/rider-auto-rest/5.0-SNAPSHOT/tmp/docker-build.tar
  [INFO] DOCKER> [fabric8/rider-auto-rest:5.0-SNAPSHOT] : Built image f140fa83fc04
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  [INFO] Total time: 20.133 s
  [INFO] Finished at: 2016-01-26T08:45:53-07:00
  [INFO] Final Memory: 79M/889M
  [INFO] ------------------------------------------------------------------------
```  
  
Now if you do a `docker images` you should see your new docker image:

```
  ceposta@postamac(rider-auto-ws (fis-enable)) $ docker images
  REPOSITORY                                                                      TAG                 IMAGE ID            CREATED              VIRTUAL SIZE
  fabric8/rider-auto-rest                                                           5.0-SNAPSHOT        f140fa83fc04        About a minute ago   491.7 MB
```
  
You can even try to run you docker container as is and map the ports locally so you can see the service running within the docker image:

> docker run -it --rm -p 8080:8080 fabric8/rider-auto-rest:5.0-SNAPSHOT
> > curl http://localhost:8080/demo

Note for that to work, we need to have the guest VM map port 8080 to the host VM (or run the docker commands from the docker host and you don't need to worry about VM port mapping).

## Deploying to OpenShift


To deploy into openshift, we need to generate the correct JSON manifest which includes all of our Services, Replication Controllers and Pods. We can [do this a few different ways](http://blog.christianposta.com/typesafe-kubernetes-dsl-for-yaml-json-generation/) but for this module we use the [fabric8 maven plugin](http://fabric8.io/guide/mavenFabric8Json.html). The plugin basically scans the list of maven properties and will generate a Kubernetes json or OpenShift template depending on the properties.

For example:

          <fabric8.service.name>${project.artifactId}</fabric8.service.name>
          <fabric8.service.headless>true</fabric8.service.headless>
  
          <fabric8.metrics.scrape>true</fabric8.metrics.scrape>
          <fabric8.metrics.port>9779</fabric8.metrics.port>
          <docker.port.container.rest>8080</docker.port.container.rest>
  
          <fabric8.service.name>${project.artifactId}</fabric8.service.name>
          <fabric8.service.port>80</fabric8.service.port>
          <fabric8.service.containerPort>8080</fabric8.service.containerPort>
  
          <fabric8.label.component>${project.artifactId}</fabric8.label.component>
          <fabric8.label.container>java</fabric8.label.container>
          <fabric8.label.group>rider-auto</fabric8.label.group>
          <fabric8.iconRef>camel</fabric8.iconRef>
          
These maven properties will produce a JSON output when you build the project:

  $ mvn clean install
  
The location of the `kubernetes.json` file is in `target/classes/kubernetes.json`

```
  {
    "apiVersion" : "v1",
    "kind" : "Template",
    "labels" : { },
    "metadata" : {
      "annotations" : {
        "fabric8.rider-auto-ws/iconUrl" : "https://cdn.rawgit.com/fabric8io/fabric8/master/fabric8-maven-plugin/src/main/resources/icons/camel.svg",
      },
      "labels" : { },
      "name" : "rider-auto-ws"
    },
    "objects" : [ {
      "apiVersion" : "v1",
      "kind" : "Service",
      "metadata" : {
        "annotations" : {
          "prometheus.io/port" : "9779",
          "prometheus.io/scrape" : "true"
        },
        "labels" : {
          "container" : "java",
          "component" : "rider-auto-ws",
          "provider" : "fabric8",
          "project" : "rider-auto-ws",
          "version" : "5.0-SNAPSHOT",
          "group" : "rider-auto"
        },
        "name" : "rider-auto-ws"
      },
      "spec" : {
        "deprecatedPublicIPs" : [ ],
        "externalIPs" : [ ],
        "ports" : [ {
          "port" : 80,
          "protocol" : "TCP",
          "targetPort" : 8183
        } ],
        "selector" : {
          "container" : "java",
          "project" : "rider-auto-ws",
          "component" : "rider-auto-ws",
          "provider" : "fabric8",
          "group" : "rider-auto"
        }
      }
    }, {
      "apiVersion" : "v1",
      "kind" : "ReplicationController",
      "metadata" : {
        "annotations" : { },
        "labels" : {
          "container" : "java",
          "component" : "rider-auto-ws",
          "provider" : "fabric8",
          "project" : "rider-auto-ws",
          "version" : "5.0-SNAPSHOT",
          "group" : "rider-auto"
        },
        "name" : "rider-auto-ws"
      },
      "spec" : {
        "replicas" : 1,
        "selector" : {
          "container" : "java",
          "component" : "rider-auto-ws",
          "provider" : "fabric8",
          "project" : "rider-auto-ws",
          "version" : "5.0-SNAPSHOT",
          "group" : "rider-auto"
        },
        "template" : {
          "metadata" : {
            "annotations" : { },
            "labels" : {
              "container" : "java",
              "component" : "rider-auto-ws",
              "provider" : "fabric8",
              "project" : "rider-auto-ws",
              "version" : "5.0-SNAPSHOT",
              "group" : "rider-auto"
            }
          },
          "spec" : {
            "containers" : [ {
              "args" : [ ],
              "command" : [ ],
              "env" : [ {
                "name" : "KUBERNETES_NAMESPACE",
                "valueFrom" : {
                  "fieldRef" : {
                    "fieldPath" : "metadata.namespace"
                  }
                }
              } ],
              "image" : "fabric8/rider-auto-ws:5.0-SNAPSHOT",
              "name" : "rider-auto-ws",
              "ports" : [ {
                "containerPort" : 8183,
                "name" : "soap"
              }, {
                "containerPort" : 8778,
                "name" : "jolokia"
              } ],
              "securityContext" : { },
              "volumeMounts" : [ ]
            } ],
            "imagePullSecrets" : [ ],
            "nodeSelector" : { },
            "volumes" : [ ]
          }
        }
      }
    } ],
    "parameters" : [ ]
  }
  
```
  
We can take that json and "apply" it to a running OpenShift installation. Note that for this plugin to work, you must
already be logged into openshift with `oc login` and have the following environment variable set:

```
  export KUBERNETES_MASTER=https://10.1.2.2:8443
  export KUBERNETES_DOMAIN=
  export KUBERNETES_TRUST_CERT=true
```
  
Then you should be able to run the following maven command to deploy:


> $  mvn clean install -Pfabric8 -Pkube-generate fabric8:apply

Note for this to work, you must have run the `docker:build` previously. Or you can combine all of them:

> $  mvn clean install -Pfabric8 -Phawtapp docker:build fabric8:apply
  
This command assumes everything is running locally (like on the CDK). Otherwise, if deploying to a centralized openshift
  installation, you can build the docker image locally, then do `docker:push` to get the image up to OpenShift and then run
  the `fabric8:apply` command to deploy the app. 
  

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
  
  
[camel-boot]: http://camel.apache.org/camel-boot.html
