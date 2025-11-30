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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.column.impl;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.util.ShadowExtractor;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Shadow delete statement data source mappings retriever.
 */
@HighFrequencyInvocation
public final class ShadowDeleteStatementDataSourceMappingsRetriever extends ShadowColumnDataSourceMappingsRetriever {
    
    private final DeleteStatementContext sqlStatementContext;
    
    private final List<Object> parameters;
    
    public ShadowDeleteStatementDataSourceMappingsRetriever(final DeleteStatementContext sqlStatementContext, final List<Object> parameters) {
        super(ShadowOperationType.DELETE);
        this.sqlStatementContext = sqlStatementContext;
        this.parameters = parameters;
    }
    
    @Override
    protected Collection<ShadowColumnCondition> getShadowColumnConditions(final String shadowColumnName) {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        for (ExpressionSegment each : getWhereSegment()) {
            String tableName = sqlStatementContext.getTablesContext().getTableNames().iterator().next();
            ShadowExtractor.extractValues(each, parameters).map(values -> new ShadowColumnCondition(tableName, shadowColumnName, values)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getWhereSegment() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (WhereSegment each : sqlStatementContext.getWhereSegments()) {
            result.addAll(ExpressionExtractor.extractAllExpressions(each.getExpr()));
        }
        return result;
    }
}
