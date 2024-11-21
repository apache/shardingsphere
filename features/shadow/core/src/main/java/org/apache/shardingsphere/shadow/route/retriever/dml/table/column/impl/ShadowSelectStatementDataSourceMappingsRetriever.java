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
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.ShadowColumnDataSourceMappingsRetriever;
import org.apache.shardingsphere.shadow.route.util.ShadowExtractor;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow select statement data source mappings retriever.
 */
@HighFrequencyInvocation
public final class ShadowSelectStatementDataSourceMappingsRetriever extends ShadowColumnDataSourceMappingsRetriever {
    
    private final SelectStatementContext sqlStatementContext;
    
    private final List<Object> parameters;
    
    public ShadowSelectStatementDataSourceMappingsRetriever(final SelectStatementContext sqlStatementContext, final List<Object> parameters) {
        super(ShadowOperationType.SELECT);
        this.sqlStatementContext = sqlStatementContext;
        this.parameters = parameters;
    }
    
    @Override
    protected Collection<ShadowColumnCondition> getShadowColumnConditions(final String shadowColumnName) {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        for (ExpressionSegment each : getWhereSegment()) {
            Collection<ColumnSegment> columns = ColumnExtractor.extract(each);
            if (1 != columns.size()) {
                continue;
            }
            ShadowExtractor.extractValues(each, parameters).map(values -> new ShadowColumnCondition(getOwnerTableName(columns.iterator().next()), shadowColumnName, values)).ifPresent(result::add);
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getWhereSegment() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (WhereSegment each : sqlStatementContext.getWhereSegments()) {
            for (AndPredicate predicate : ExpressionExtractor.extractAndPredicates(each.getExpr())) {
                result.addAll(predicate.getPredicates());
            }
        }
        return result;
    }
    
    private String getOwnerTableName(final ColumnSegment columnSegment) {
        Optional<OwnerSegment> owner = columnSegment.getOwner();
        return owner.isPresent()
                ? sqlStatementContext.getTablesContext().getTableAliasNameMap().get(owner.get().getIdentifier().getValue())
                : sqlStatementContext.getTablesContext().getTableNames().iterator().next();
    }
}
