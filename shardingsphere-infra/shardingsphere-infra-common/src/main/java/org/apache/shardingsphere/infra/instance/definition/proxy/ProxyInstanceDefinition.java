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

package org.apache.shardingsphere.infra.instance.definition.proxy;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;
import org.apache.shardingsphere.infra.instance.definition.InstanceType;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;

/**
 * Proxy instance definition.
 */
@Getter
public final class ProxyInstanceDefinition implements InstanceDefinition {
    
    private static final String DELIMITER = "@";
    
    private final String instanceId;
    
    private final String ip;
    
    private final int port;
    
    public ProxyInstanceDefinition(final String instanceId, final int port) {
        this.instanceId = instanceId;
        ip = IpUtils.getIp();
        this.port = port;
    }
    
    public ProxyInstanceDefinition(final String instanceId, final String attributes) {
        this.instanceId = instanceId;
        String[] attributesList = attributes.split(DELIMITER);
        ip = attributesList[0];
        port = Integer.parseInt(attributesList[1]);
    }
    
    @Override
    public InstanceType getInstanceType() {
        return InstanceType.PROXY;
    }
    
    @Override
    public String getAttributes() {
        return Joiner.on(DELIMITER).join(ip, port);
    }
}
