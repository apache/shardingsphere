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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.engine.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow select statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowSelectStatementRoutingEngine extends AbstractShadowDMLStatementRouteEngine {
    
    private final SelectStatementContext selectStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return selectStatementContext.getAllTables();
    }
    
    @Override
    protected ShadowOperationType getShadowOperationType() {
        return ShadowOperationType.SELECT;
    }
    
    @Override
    protected Optional<Collection<String>> parseSQLComments() {
        Collection<String> result = new LinkedList<>();
        selectStatementContext.getSqlStatement().getCommentSegments().forEach(each -> result.add(each.getText()));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    @Override
    protected Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator() {
        return new ShadowColumnConditionIterator(parseWhereSegment(), parameters);
    }
    
    private Collection<ExpressionSegment> parseWhereSegment() {
        Collection<ExpressionSegment> result = new LinkedList<>();
        for (WhereSegment each : selectStatementContext.getWhereSegments()) {
            for (AndPredicate predicate : ExpressionExtractUtil.getAndPredicates(each.getExpr())) {
                result.addAll(predicate.getPredicates());
            }
        }
        return result;
    }
    
    private class ShadowColumnConditionIterator implements Iterator<Optional<ShadowColumnCondition>> {
        
        private final Iterator<ExpressionSegment> iterator;
        
        private final List<Object> parameters;
        
        ShadowColumnConditionIterator(final Collection<ExpressionSegment> predicates, final List<Object> parameters) {
            this.iterator = predicates.iterator();
            this.parameters = parameters;
        }
        
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }
        
        @Override
        public Optional<ShadowColumnCondition> next() {
            ExpressionSegment expressionSegment = iterator.next();
            Optional<ColumnSegment> columnSegment = ShadowExtractor.extractColumn(expressionSegment);
            if (columnSegment.isPresent()) {
                Optional<Collection<Comparable<?>>> values = ShadowExtractor.extractValues(expressionSegment, parameters);
                if (values.isPresent()) {
                    return Optional.of(new ShadowColumnCondition(extractOwnerName(columnSegment.get()), columnSegment.get().getIdentifier().getValue(), values.get()));
                }
            }
            return Optional.empty();
        }
        
        private String extractOwnerName(final ColumnSegment columnSegment) {
            Optional<OwnerSegment> owner = columnSegment.getOwner();
            return owner.isPresent() ? getTableAliasNameMappings().get(owner.get().getIdentifier().getValue()) : getTableAliasNameMappings().keySet().iterator().next();
        }
    }
}
