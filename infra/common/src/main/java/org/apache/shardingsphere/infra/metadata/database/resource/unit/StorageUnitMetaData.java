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

package org.apache.shardingsphere.infra.metadata.database.resource.unit;

import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;

import javax.sql.DataSource;

/**
 * Storage unit meta data.
 */
@Getter
public final class StorageUnitMetaData {
    
    private final StorageNode storageNode;
    
    private final DataSourcePoolProperties dataSourcePoolProperties;
    
    private final DataSource dataSource;
    
    private final StorageUnit storageUnit;
    
    public StorageUnitMetaData(final String databaseName, final StorageNode storageNode, final DataSourcePoolProperties dataSourcePoolProperties, final DataSource dataSource) {
        this.storageNode = storageNode;
        this.dataSourcePoolProperties = dataSourcePoolProperties;
        this.dataSource = dataSource;
        storageUnit = new StorageUnit(databaseName, dataSource, dataSourcePoolProperties, storageNode);
    }
}
