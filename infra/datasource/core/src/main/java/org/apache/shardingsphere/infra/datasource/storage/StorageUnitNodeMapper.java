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

package org.apache.shardingsphere.infra.datasource.storage;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Storage unit and node mapper.
 */
@RequiredArgsConstructor
@Getter
public final class StorageUnitNodeMapper {
    
    private final String name;
    
    private final String nodeName;
    
    private final String catalog;
    
    private final String url;
    
    public StorageUnitNodeMapper(final String name, final String nodeName, final String url) {
        this(name, nodeName, null, url);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof StorageUnitNodeMapper) {
            StorageUnitNodeMapper storageUnitNodeMapper = (StorageUnitNodeMapper) obj;
            return storageUnitNodeMapper.name.equalsIgnoreCase(name) && storageUnitNodeMapper.nodeName.equalsIgnoreCase(nodeName) && isSameCatalog(storageUnitNodeMapper);
        }
        return false;
    }
    
    private boolean isSameCatalog(final StorageUnitNodeMapper storageUnitNodeMapper) {
        return null == catalog ? null == storageUnitNodeMapper : catalog.equalsIgnoreCase(storageUnitNodeMapper.getCatalog());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(name.toUpperCase(), nodeName.toUpperCase(), null == catalog ? null : catalog.toUpperCase());
    }
}
