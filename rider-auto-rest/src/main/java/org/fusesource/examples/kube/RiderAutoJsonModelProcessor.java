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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.generator.annotation.KubernetesModelProcessor;

import javax.inject.Named;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
@KubernetesModelProcessor
public class RiderAutoJsonModelProcessor {


    @Named("rider-auto-rest")
    public void withResourceLimits(ContainerBuilder builder) {
        builder.withNewResources()
                .addToLimits("memory", new Quantity("512Mi"))
                .addToLimits("cpu", new Quantity("100m"))
                .addToRequests("memory", new Quantity("256Mi"))
                .addToRequests("cpu", new Quantity("100m"))
                .endResources()
                .build();
    }

}
