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

package org.apache.shardingsphere.infra.instance.definition;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Instance definition.
 */
@Getter
public final class InstanceDefinition {
    
    private static final String DELIMITER = "@";
    
    private static final AtomicLong ATOMIC_LONG = new AtomicLong();
    
    private final InstanceType instanceType;
    
    private final String instanceId;
    
    private String ip;
    
    private String uniqueSign;
    
    public InstanceDefinition(final InstanceType instanceType, final String instanceId) {
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        ip = IpUtils.getIp();
        uniqueSign = String.join("", ManagementFactory.getRuntimeMXBean().getName().split(DELIMITER)[0], String.valueOf(ATOMIC_LONG.incrementAndGet()));
    }
    
    public InstanceDefinition(final InstanceType instanceType, final Integer port, final String instanceId) {
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        ip = IpUtils.getIp();
        uniqueSign = String.valueOf(port);
    }
    
    public InstanceDefinition(final InstanceType instanceType, final String instanceId, final String attributes) {
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        List<String> attributesList = Splitter.on(DELIMITER).splitToList(attributes);
        ip = attributesList.get(0);
        uniqueSign = attributesList.get(1);
    }
    
    /**
     * Get instance attributes.
     * 
     * @return ip@uniqueSign
     */
    public String getAttributes() {
        return Joiner.on(DELIMITER).join(ip, uniqueSign);
    }
}
