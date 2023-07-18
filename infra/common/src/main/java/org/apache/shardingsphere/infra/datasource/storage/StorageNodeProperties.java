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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;

/**
 * Storage node properties.
 */
@RequiredArgsConstructor
@Getter
public final class StorageNodeProperties {
    
    private final String name;
    
    private final DatabaseType databaseType;
    
    private final DataSourceProperties dataSourceProperties;
    
    private final String database;
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof StorageNodeProperties) {
            StorageNodeProperties storageNodeProperties = (StorageNodeProperties) obj;
            return storageNodeProperties.name.equals(name);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(name.toUpperCase());
    }
}
