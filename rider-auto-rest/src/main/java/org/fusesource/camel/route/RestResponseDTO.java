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

import java.net.InetAddress;

/**
 * Created by ceposta 
 * <a href="http://christianposta.com/blog>http://christianposta.com/blog</a>.
 */
public class RestResponseDTO {

    private long timestamp;
    private String hostIP;
    private String hostname;

    public RestResponseDTO() throws Exception{
        this.timestamp = System.currentTimeMillis();
        this.hostIP = InetAddress.getLocalHost().getHostAddress();
        this.hostname = InetAddress.getLocalHost().getHostName();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHostIP() {
        return hostIP;
    }

    public String getHostname() {
        return hostname;
    }
}
