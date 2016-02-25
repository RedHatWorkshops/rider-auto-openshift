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
package org.fusesource.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class RestRouteBuilder extends RouteBuilder{
    @Override
    public void configure() throws Exception {
        restConfiguration().component("netty-http").host("0.0.0.0").port(8080).bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        rest("/demo")
                .get("/").consumes("json/text").produces("json/text")
                .route().process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                // spin the CPU for a bit...
                byte[] input = "asdflkajsklfsadlkfalksdjflkasdjklfjasldfjalskjflasdjklfajsdlkj".getBytes();
                byte[] keyBytes = "fooobarr".getBytes();
                byte[] ivBytes = "fooobarr".getBytes();
                IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
                Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
                SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
                cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
                byte[] encrypted= new byte[cipher.getOutputSize(input.length)];
                for (int i = 0; i < 1000000;i++ ) {
                    cipher.update(input, 0, input.length, encrypted, 0);
                }
                exchange.getIn().setBody(new RestResponseDTO());
            }
        });
    }
}
