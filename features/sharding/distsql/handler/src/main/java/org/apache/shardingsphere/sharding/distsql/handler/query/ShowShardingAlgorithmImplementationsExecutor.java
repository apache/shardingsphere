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

import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingAlgorithmImplementationsStatement;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show sharding algorithm implementations executor.
 */
public final class ShowShardingAlgorithmImplementationsExecutor implements QueryableRALExecutor<ShowShardingAlgorithmImplementationsStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "class_path");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowShardingAlgorithmImplementationsStatement sqlStatement, final ShardingSphereMetaData metaData) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<ShardingAlgorithm> shardingAlgorithms = ShardingSphereServiceLoader.getServiceInstances(ShardingAlgorithm.class);
        for (ShardingAlgorithm each : shardingAlgorithms) {
            result.add(new LocalDataQueryResultRow(each.getClass().getSimpleName(), each.getType(), each.getClass().getName()));
        }
        return result;
    }
    
    @Override
    public Class<ShowShardingAlgorithmImplementationsStatement> getType() {
        return ShowShardingAlgorithmImplementationsStatement.class;
    }
}
