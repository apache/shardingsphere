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

/**
 * Instance definition factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstanceDefinitionFactory {
    
    /**
     * Create instance of instance definition.
     * 
     * @param type type
     * @param instanceId instance ID
     * @param attributes attributes 
     * @return created instance of instance definition
     */
    public static InstanceDefinition newInstance(final InstanceType type, final String instanceId, final String attributes) {
        if (InstanceType.JDBC == type) {
            return new JDBCInstanceDefinition(instanceId);
        }
        if (InstanceType.PROXY == type) {
            return new ProxyInstanceDefinition(instanceId, attributes);
        }
        throw new UnsupportedOperationException(type.name());
    }
}
