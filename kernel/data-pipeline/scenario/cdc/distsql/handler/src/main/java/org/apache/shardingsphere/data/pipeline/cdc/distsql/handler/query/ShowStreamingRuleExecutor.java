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

package org.apache.shardingsphere.data.pipeline.cdc.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.cdc.distsql.statement.queryable.ShowStreamingRuleStatement;
import org.apache.shardingsphere.data.pipeline.distsql.handler.transmission.query.ShowTransmissionRuleQueryResult;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;

/**
 * Show streaming rule executor.
 */
public final class ShowStreamingRuleExecutor implements DistSQLQueryExecutor<ShowStreamingRuleStatement> {
    
    private final ShowTransmissionRuleQueryResult queryResult = new ShowTransmissionRuleQueryResult("STREAMING");
    
    @Override
    public Collection<String> getColumnNames(final ShowStreamingRuleStatement sqlStatement) {
        return queryResult.getColumnNames();
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStreamingRuleStatement sqlStatement, final ContextManager contextManager) {
        return queryResult.getRows();
    }
    
    @Override
    public Class<ShowStreamingRuleStatement> getType() {
        return ShowStreamingRuleStatement.class;
    }
}
