# How to install rider-auto

The rider-auto app consists of a set of microservices all packaged as Docker containers.


## Create new OpenShift project
For this demo, we'll want to create a new OpenShift project and install all of the rider-auto microservices there.

> oc new-project rider-auto

## Install Persistent Volume

The rider-auto-file module will poll for new orders to start processing and it listens to a directory on a file system. In this example, we'll use Kubernetes [Persistent Volumes](http://kubernetes.io/v1.1/docs/user-guide/persistent-volumes.html) and map the location that Camel polls to a HostPath Persistent Volume which we can use to test out that everything works. For example, this is the yaml for the PV:
 
```
kind: PersistentVolume
apiVersion: v1
metadata:
  name: rider-auto-file-pv
  labels:
    type: local
spec:
  capacity:
    storage: 100Ki
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Recycle
  hostPath:
    path: "/opt/camel"
```


This means that the path on disk "/opt/camel" will be the location that we can send files to and have the rider-auto-file pod pick it up and process it.

To install this HostPath PV, you'll need to be an admin user on the "rider-auto" project and try the following:


> oc create -f rider-auto-file/src/main/fabric8/vagrant-pv.yaml

After doing this, you should double check that it was created properly:

> oc get pv

```
[root@localhost ~]# oc get pv
NAME                 LABELS       CAPACITY   ACCESSMODES   STATUS      CLAIM     REASON    AGE
rider-auto-file-pv   type=local   100Ki      RWO           Available                       4m
```

## Install AMQ

The microservices in this project communicate over JMS and use JBoss AMQ. To do this, we should boot up a broker in the `rider-auto` project. In the root of the project, we have a curated `amq.json` file that contains the broker.

> oc create amq.json

## Builder Docker containers

To run these microservices on openshift, you should build the docker images for each module. See the docs for each module for how to do that.

## Install microservices

From the root directory of `rider-auto-ose-installer` project, run:

> mvn clean install fabric8:json fabric8:apply

