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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.type.query.ral.algorithm.AlgorithmMetaDataQueryResultRows;
import org.apache.shardingsphere.distsql.handler.type.query.rql.ShowSPIImplementationsBuilder;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Collection;

/**
 * Sharding SPI implementations result row builder.
 */
public final class ShardingSPIImplementationsResultRowBuilder implements ShowSPIImplementationsBuilder {
    
    private final AlgorithmMetaDataQueryResultRows algorithmMetaDataQueryResultRows = new AlgorithmMetaDataQueryResultRows(ShardingAlgorithm.class);
    
    @Override
    public Collection<LocalDataQueryResultRow> generateRows() {
        return algorithmMetaDataQueryResultRows.getRows();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return algorithmMetaDataQueryResultRows.getColumnNames();
    }
    
    @Override
    public String getType() {
        return "SHARDING";
    }
}
