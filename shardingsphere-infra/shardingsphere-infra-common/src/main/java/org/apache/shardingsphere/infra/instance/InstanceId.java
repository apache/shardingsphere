/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.instance;

import lombok.Getter;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

import java.lang.management.ManagementFactory;

/**
 * Instance id.
 */
@Getter
public final class InstanceId {
    
    private static final String DELIMITER = "@";
    
    private final String id;
    
    private final String ip;
    
    private final Integer port;
    
    public InstanceId(final String ip, final Integer port) {
        this.ip = ip;
        this.port = port;
        id = String.join(DELIMITER, ip, String.valueOf(port));
    }
    
    public InstanceId(final Integer port) {
        this.port = port;
        ip = IpUtils.getIp();
        id = String.join(DELIMITER, ip, String.valueOf(port));
    }
    
    public InstanceId() {
        port = 0;
        ip = IpUtils.getIp();
        id = String.join(DELIMITER, ip, ManagementFactory.getRuntimeMXBean().getName().split(DELIMITER)[0]);
    }
}
