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

package org.apache.shardingsphere.shadow.route.retriever.dml;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowDeleteStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowInsertStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowSelectStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl.ShadowUpdateStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.hint.ShadowTableHintDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;

import java.util.Collection;
import java.util.Map;

/**
 * Shadow DML statement data source mappings retriever.
 */
@HighFrequencyInvocation
public final class ShadowDMLStatementDataSourceMappingsRetriever implements ShadowDataSourceMappingsRetriever {
    
    private final Collection<String> tableNames;
    
    private final ShadowTableHintDataSourceMappingsRetriever tableHintDataSourceMappingsRetriever;
    
    private final ShadowColumnDataSourceMappingsRetriever shadowColumnDataSourceMappingsRetriever;
    
    public ShadowDMLStatementDataSourceMappingsRetriever(final QueryContext queryContext, final ShadowOperationType operationType) {
        tableNames = queryContext.getSqlStatementContext().getTablesContext().getTableNames();
        tableHintDataSourceMappingsRetriever = new ShadowTableHintDataSourceMappingsRetriever(operationType, queryContext.getHintValueContext().isShadow());
        shadowColumnDataSourceMappingsRetriever = createShadowDataSourceMappingsRetriever(queryContext);
    }
    
    private ShadowColumnDataSourceMappingsRetriever createShadowDataSourceMappingsRetriever(final QueryContext queryContext) {
        if (queryContext.getSqlStatementContext() instanceof InsertStatementContext) {
            return new ShadowInsertStatementDataSourceMappingsRetriever((InsertStatementContext) queryContext.getSqlStatementContext());
        }
        if (queryContext.getSqlStatementContext() instanceof DeleteStatementContext) {
            return new ShadowDeleteStatementDataSourceMappingsRetriever((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
        }
        if (queryContext.getSqlStatementContext() instanceof UpdateStatementContext) {
            return new ShadowUpdateStatementDataSourceMappingsRetriever((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
        }
        if (queryContext.getSqlStatementContext() instanceof SelectStatementContext) {
            return new ShadowSelectStatementDataSourceMappingsRetriever((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters());
        }
        return null;
    }
    
    @Override
    public Map<String, String> retrieve(final ShadowRule rule) {
        Collection<String> shadowTables = rule.filterShadowTables(tableNames);
        Map<String, String> result = tableHintDataSourceMappingsRetriever.retrieve(rule, shadowTables);
        return result.isEmpty() && null != shadowColumnDataSourceMappingsRetriever ? shadowColumnDataSourceMappingsRetriever.retrieve(rule, shadowTables) : result;
    }
}
