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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowReadQueryLoadBalanceAlgorithmImplementationsStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show read query load balance algorithm implementations executor.
 */
public final class ShowReadQueryLoadBalanceAlgorithmImplementationsExecutor implements QueryableRALExecutor<ShowReadQueryLoadBalanceAlgorithmImplementationsStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "class_path");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowReadQueryLoadBalanceAlgorithmImplementationsStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Collection<ReadQueryLoadBalanceAlgorithm> shardingAlgorithms = ShardingSphereServiceLoader.getServiceInstances(ReadQueryLoadBalanceAlgorithm.class);
        for (ReadQueryLoadBalanceAlgorithm each : shardingAlgorithms) {
            result.add(new LocalDataQueryResultRow(each.getClass().getSimpleName(), each.getType(), each.getClass().getName()));
        }
        return result;
    }
    
    @Override
    public Class<ShowReadQueryLoadBalanceAlgorithmImplementationsStatement> getType() {
        return ShowReadQueryLoadBalanceAlgorithmImplementationsStatement.class;
    }
}
