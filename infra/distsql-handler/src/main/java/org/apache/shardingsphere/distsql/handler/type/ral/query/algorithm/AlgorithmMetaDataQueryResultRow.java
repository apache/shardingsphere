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

package org.apache.shardingsphere.distsql.handler.type.ral.query.algorithm;

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Algorithm meta data query result row.
 */
public final class AlgorithmMetaDataQueryResultRow {
    
    private final boolean containsDatabaseTypes;
    
    private final String type;
    
    private final String typeAliases;
    
    private final String supportedDatabaseTypes;
    
    private final String description;
    
    public AlgorithmMetaDataQueryResultRow(final ShardingSphereAlgorithm algorithm) {
        containsDatabaseTypes = algorithm instanceof DatabaseSupportedTypedSPI;
        type = String.valueOf(algorithm.getType());
        typeAliases = algorithm.getTypeAliases().stream().map(Object::toString).collect(Collectors.joining(","));
        supportedDatabaseTypes = containsDatabaseTypes
                ? getSupportedDatabaseTypes(((DatabaseSupportedTypedSPI) algorithm).getSupportedDatabaseTypes()).stream().map(DatabaseType::getType).collect(Collectors.joining(","))
                : "";
        SPIDescription description = algorithm.getClass().getAnnotation(SPIDescription.class);
        this.description = null == description ? "" : description.value();
    }
    
    private Collection<DatabaseType> getSupportedDatabaseTypes(final Collection<DatabaseType> supportedDatabaseTypes) {
        return supportedDatabaseTypes.isEmpty() ? ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class) : supportedDatabaseTypes;
    }
    
    /**
     * To local data query result row.
     * 
     * @return local data query result row
     */
    public LocalDataQueryResultRow toLocalDataQueryResultRow() {
        return containsDatabaseTypes ? new LocalDataQueryResultRow(type, typeAliases, supportedDatabaseTypes, description) : new LocalDataQueryResultRow(type, typeAliases, description);
    }
}
