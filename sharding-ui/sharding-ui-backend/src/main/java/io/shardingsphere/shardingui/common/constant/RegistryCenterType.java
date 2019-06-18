/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.common.constant;

import io.shardingsphere.shardingui.common.exception.ShardingUIException;

/**
 * Registry center type.
 *
 * @author chenqingyang
 */
public enum RegistryCenterType {
    
    ZOOKEEPER("Zookeeper"), ETCD("Etcd");
    
    private final String name;
    
    RegistryCenterType(final String name) {
        this.name = name;
    }
    
    /**
     * Get registry center type via name.
     *
     * @param name registry center name
     * @return registry center type
     */
    public static RegistryCenterType nameOf(final String name) {
        for (RegistryCenterType each : RegistryCenterType.values()) {
            if ((each.name).equals(name)) {
                return each;
            }
        }
        throw new ShardingUIException(ShardingUIException.SERVER_ERROR, String.format("Unsupported registry center `%s`", name));
    }
}
