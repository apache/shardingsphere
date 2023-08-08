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

package org.apache.shardingsphere.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationSourceStorageUnitsStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Show migration source storage units executor.
 */
public final class ShowMigrationSourceStorageUnitsExecutor implements QueryableRALExecutor<ShowMigrationSourceStorageUnitsStatement> {
    
    private final MigrationJobAPI jobAPI = new MigrationJobAPI();
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationSourceStorageUnitsStatement sqlStatement) {
        Iterator<Collection<Object>> data = jobAPI.listMigrationSourceResources(PipelineContextKey.buildForProxy()).iterator();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            result.add(new LocalDataQueryResultRow((List<Object>) data.next()));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "host", "port", "db", "connection_timeout_milliseconds", "idle_timeout_milliseconds",
                "max_lifetime_milliseconds", "max_pool_size", "min_pool_size", "read_only", "other_attributes");
    }
    
    @Override
    public Class<ShowMigrationSourceStorageUnitsStatement> getType() {
        return ShowMigrationSourceStorageUnitsStatement.class;
    }
}
