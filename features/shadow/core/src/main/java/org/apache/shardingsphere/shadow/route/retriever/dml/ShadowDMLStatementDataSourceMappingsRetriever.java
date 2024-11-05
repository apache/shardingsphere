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
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowDeleteStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowInsertStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowSelectStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowUpdateStatementDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.hint.ShadowTableHintDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shadow DML statement data source mappings retriever.
 */
@HighFrequencyInvocation
public final class ShadowDMLStatementDataSourceMappingsRetriever implements ShadowDataSourceMappingsRetriever {
    
    private final Map<String, String> tableAliasAndNameMappings;
    
    private final ShadowTableHintDataSourceMappingsRetriever tableHintDataSourceMappingsRetriever;
    
    private final ShadowColumnDataSourceMappingsRetriever shadowColumnDataSourceMappingsRetriever;
    
    public ShadowDMLStatementDataSourceMappingsRetriever(final QueryContext queryContext, final ShadowOperationType operationType) {
        tableAliasAndNameMappings = getTableAliasAndNameMappings(((TableAvailable) queryContext.getSqlStatementContext()).getTablesContext().getSimpleTables());
        tableHintDataSourceMappingsRetriever = new ShadowTableHintDataSourceMappingsRetriever(operationType, queryContext.getHintValueContext().isShadow());
        shadowColumnDataSourceMappingsRetriever = createShadowDataSourceMappingsRetriever(queryContext, tableAliasAndNameMappings);
    }
    
    private Map<String, String> getTableAliasAndNameMappings(final Collection<SimpleTableSegment> tableSegments) {
        Map<String, String> result = new LinkedHashMap<>(tableSegments.size(), 1F);
        for (SimpleTableSegment each : tableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAliasName().isPresent() ? each.getAliasName().get() : tableName;
            result.put(alias, tableName);
        }
        return result;
    }
    
    private ShadowColumnDataSourceMappingsRetriever createShadowDataSourceMappingsRetriever(final QueryContext queryContext, final Map<String, String> tableAliasAndNameMappings) {
        if (queryContext.getSqlStatementContext() instanceof InsertStatementContext) {
            return new ShadowInsertStatementDataSourceMappingsRetriever((InsertStatementContext) queryContext.getSqlStatementContext(), tableAliasAndNameMappings);
        }
        if (queryContext.getSqlStatementContext() instanceof DeleteStatementContext) {
            return new ShadowDeleteStatementDataSourceMappingsRetriever((DeleteStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), tableAliasAndNameMappings);
        }
        if (queryContext.getSqlStatementContext() instanceof UpdateStatementContext) {
            return new ShadowUpdateStatementDataSourceMappingsRetriever((UpdateStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), tableAliasAndNameMappings);
        }
        if (queryContext.getSqlStatementContext() instanceof SelectStatementContext) {
            return new ShadowSelectStatementDataSourceMappingsRetriever((SelectStatementContext) queryContext.getSqlStatementContext(), queryContext.getParameters(), tableAliasAndNameMappings);
        }
        return null;
    }
    
    @Override
    public Map<String, String> retrieve(final ShadowRule rule) {
        Collection<String> shadowTables = rule.filterShadowTables(tableAliasAndNameMappings.values());
        Map<String, String> result = tableHintDataSourceMappingsRetriever.retrieve(rule, shadowTables);
        return result.isEmpty() && null != shadowColumnDataSourceMappingsRetriever ? shadowColumnDataSourceMappingsRetriever.retrieve(rule, shadowTables) : result;
    }
}
