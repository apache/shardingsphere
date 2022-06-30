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
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

import java.util.List;

/**
 * Instance definition.
 */
@Getter
public final class InstanceDefinition {
    
    private static final String DELIMITER = "@";
    
    private final InstanceType instanceType;
    
    private final String instanceId;
    
    private String ip;
    
    private int port;
    
    public InstanceDefinition(final String instanceId) {
        instanceType = InstanceType.JDBC;
        this.instanceId = instanceId;
    }
    
    public InstanceDefinition(final int port, final String instanceId) {
        instanceType = InstanceType.PROXY;
        this.instanceId = instanceId;
        ip = IpUtils.getIp();
        this.port = port;
    }
    
    public InstanceDefinition(final InstanceType instanceType, final String instanceId, final String attributes) {
        this.instanceType = instanceType;
        this.instanceId = instanceId;
        if (!Strings.isNullOrEmpty(attributes) && attributes.contains(DELIMITER)) {
            List<String> attributesList = Splitter.on(DELIMITER).splitToList(attributes);
            ip = attributesList.get(0);
            port = Integer.valueOf(attributesList.get(1));
        }
    }
    
    /**
     * Get instance attributes.
     * 
     * @return got instance attributes, format is ip@uniqueSign
     */
    public String getAttributes() {
        return instanceType == InstanceType.JDBC ? "" : Joiner.on(DELIMITER).join(ip, port);
    }
}
