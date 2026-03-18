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

package org.apache.shardingsphere.infra.spi.type.typed;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Typed SPI.
 */
public interface TypedSPI extends ShardingSphereSPI {
    
    /**
     * Initialize SPI.
     *
     * @param props properties to be initialized
     */
    default void init(final Properties props) {
    }
    
    /**
     * Get type.
     *
     * @return type
     */
    Object getType();
    
    /**
     * Get type aliases.
     *
     * @return type aliases
     */
    default Collection<Object> getTypeAliases() {
        return Collections.emptyList();
    }
    
    /**
     * Judge whether default service provider.
     *
     * @return is default service provider or not
     */
    default boolean isDefault() {
        return false;
    }
}
