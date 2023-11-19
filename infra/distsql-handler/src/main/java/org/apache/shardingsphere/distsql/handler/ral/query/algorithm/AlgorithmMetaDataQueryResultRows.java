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

package org.apache.shardingsphere.distsql.handler.ral.query.algorithm;

import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseSupportedTypedSPI;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Algorithm meta data query result rows.
 */
public final class AlgorithmMetaDataQueryResultRows {
    
    private final boolean containsDatabaseTypes;
    
    private final Collection<AlgorithmMetaDataQueryResultRow> rows;
    
    public AlgorithmMetaDataQueryResultRows(final Class<? extends ShardingSphereAlgorithm> algorithmClass) {
        containsDatabaseTypes = DatabaseSupportedTypedSPI.class.isAssignableFrom(algorithmClass);
        rows = ShardingSphereServiceLoader.getServiceInstances(algorithmClass).stream().map(AlgorithmMetaDataQueryResultRow::new).collect(Collectors.toList());
    }
    
    /**
     * Get rows.
     * 
     * @return rows
     */
    public Collection<LocalDataQueryResultRow> getRows() {
        return rows.stream().map(AlgorithmMetaDataQueryResultRow::toLocalDataQueryResultRow).collect(Collectors.toList());
    }
    
    /**
     * Get column names.
     * 
     * @return column names
     */
    public Collection<String> getColumnNames() {
        return containsDatabaseTypes ? Arrays.asList("type", "type_aliases", "supported_database_types", "description") : Arrays.asList("type", "type_aliases", "description");
    }
}
