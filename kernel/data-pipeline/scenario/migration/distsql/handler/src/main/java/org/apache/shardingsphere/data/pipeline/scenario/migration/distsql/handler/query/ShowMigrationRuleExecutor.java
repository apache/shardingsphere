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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.distsql.handler.transmission.query.ShowTransmissionRuleQueryResult;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationRuleStatement;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;

/**
 * Show migration rule executor.
 */
public final class ShowMigrationRuleExecutor implements DistSQLQueryExecutor<ShowMigrationRuleStatement> {
    
    private final ShowTransmissionRuleQueryResult queryResult = new ShowTransmissionRuleQueryResult("MIGRATION");
    
    @Override
    public Collection<String> getColumnNames(final ShowMigrationRuleStatement sqlStatement) {
        return queryResult.getColumnNames();
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowMigrationRuleStatement sqlStatement, final ContextManager contextManager) {
        return queryResult.getRows();
    }
    
    @Override
    public Class<ShowMigrationRuleStatement> getType() {
        return ShowMigrationRuleStatement.class;
    }
}
