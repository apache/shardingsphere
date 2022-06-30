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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Instance definition builder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstanceDefinitionBuilderFactory {
    
    static {
        ShardingSphereServiceLoader.register(InstanceDefinitionBuilder.class);
    }
    
    /**
     * Create instance of instance definition.
     *
     * @param type type
     * @param instanceId instance ID
     * @param port port 
     * @return created instance of instance definition
     */
    public static InstanceDefinition newInstance(final String type, final String instanceId, final int port) {
        return TypedSPIRegistry.getRegisteredService(InstanceDefinitionBuilder.class, type).build(instanceId, port);
    }
    
    /**
     * Create instance of instance definition.
     * 
     * @param type type
     * @param instanceId instance ID
     * @param attributes attributes 
     * @return created instance of instance definition
     */
    public static InstanceDefinition newInstance(final String type, final String instanceId, final String attributes) {
        return TypedSPIRegistry.getRegisteredService(InstanceDefinitionBuilder.class, type).build(instanceId, attributes);
    }
    
    /**
     * Get all builder types.
     * 
     * @return got all builder types
     */
    public static Collection<String> getAllTypes() {
        return ShardingSphereServiceLoader.getServiceInstances(InstanceDefinitionBuilder.class).stream().map(TypedSPI::getType).collect(Collectors.toList());
    }
}
