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

package org.apache.shardingsphere.spi.type.singleton;

import org.apache.shardingsphere.spi.type.typed.TypedSPI;

import java.util.Map;
import java.util.Optional;

/**
 * Typed singleton SPI holder.
 */
public final class TypedSingletonSPIHolder<T extends TypedSPI & SingletonSPI> {
    
    private final Map<String, T> singletonSPIMap;
    
    private final boolean typeCaseSensitive;
    
    public TypedSingletonSPIHolder(final Class<T> singletonSPIClass, final boolean typeCaseSensitive) {
        this.singletonSPIMap = SingletonSPIRegistry.getSingletonInstancesMap(singletonSPIClass, t -> getTypeKey(t.getType(), typeCaseSensitive));
        this.typeCaseSensitive = typeCaseSensitive;
    }
    
    private String getTypeKey(final String type, final boolean typeCaseSensitive) {
        return typeCaseSensitive ? type : type.toUpperCase();
    }
    
    /**
     * Get typed singleton SPI instance.
     *
     * @param type SPI type
     * @return typed singleton SPI instance
     */
    public Optional<T> get(final String type) {
        return Optional.ofNullable(singletonSPIMap.get(getTypeKey(type, typeCaseSensitive)));
    }
}
