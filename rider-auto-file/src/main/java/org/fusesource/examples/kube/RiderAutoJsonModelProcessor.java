/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.examples.kube;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.generator.annotation.KubernetesModelProcessor;
import io.fabric8.openshift.api.model.TemplateBuilder;

import javax.inject.Named;
import java.util.HashMap;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@KubernetesModelProcessor
public class RiderAutoJsonModelProcessor {

    // comment this out for now; we'll include the PV in a different manifest since
    // this is not something typically installed as part of the application and is done
    // an adminsitrator
/*    public void withPersistentVolume(TemplateBuilder builder) {
        builder.addNewPersistentVolumeObject()
                .withNewMetadata()
                    .withName("auto-rider-pv")
                    .addToLabels("provider", "fabric8")
                    .addToLabels("project", "rider-auto-file")
                    .addToLabels("component", "rider-auto-file")
                    .addToLabels("group", "rider-auto")
                .endMetadata()
                .withNewSpec()
                    .addToCapacity("storage", new Quantity("100Ki"))
                    .addToAccessModes("ReadWriteOnce")
                    .withHostPath(new HostPathVolumeSource("/home/vagrant/camel"))
                .endSpec()
                .endPersistentVolumeObject()
                .build();
    }*/

    public void withPersistentVolumeClaim(TemplateBuilder builder){
        builder.addNewPersistentVolumeClaimObject()
                .withNewMetadata()
                    .withName("rider-auto-file-pvc")
                    .addToLabels("provider", "fabric8")
                    .addToLabels("project", "rider-auto-file")
                    .addToLabels("component", "rider-auto-file")
                    .addToLabels("group", "rider-auto")
                .endMetadata()
                .withNewSpec()
                    .withAccessModes("ReadWriteOnce")
                .withResources(getResourceRequirement())
                .endSpec()
            .endPersistentVolumeClaimObject().build();
    }

    private ResourceRequirements getResourceRequirement() {
        ResourceRequirements rc = new ResourceRequirements();

        Quantity claimSize = new Quantity("100Ki");
        HashMap<String, Quantity> requests = new HashMap<>();
        requests.put("storage", claimSize);
        rc.setRequests(requests);
        return rc;
    }

    public void withPodTemplate(PodTemplateSpecBuilder builder) {
        builder.withSpec(builder.getSpec()).editSpec()
                .addNewVolume()
                    .withName("rider-auto-file-volume")
                    .withPersistentVolumeClaim(getPersistentVolumeClaimSource())
                .endVolume()
                .endSpec().build();
    }

    private PersistentVolumeClaimVolumeSource getPersistentVolumeClaimSource() {
        PersistentVolumeClaimVolumeSource rc = new PersistentVolumeClaimVolumeSource("rider-auto-file-pvc", false);
        return rc;
    }

    @Named("rider-auto-file")
    public void withVolumeMounts(ContainerBuilder builder) {
        builder.withVolumeMounts(new VolumeMount("/deployments/target/placeorder", "rider-auto-file-volume", false))
                .build();
    }

}
