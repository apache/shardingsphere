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

package org.apache.shardingsphere.shadow.route.future.engine.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.future.engine.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
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
    protected Optional<Collection<ShadowColumnCondition>> parseShadowColumnConditions() {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        selectStatementContext.getWhere().ifPresent(whereSegment -> parseWhereSegment(whereSegment, result));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private void parseWhereSegment(final WhereSegment whereSegment, final Collection<ShadowColumnCondition> shadowColumnConditions) {
        ExpressionExtractUtil.getAndPredicates(whereSegment.getExpr()).forEach(each -> parseAndPredicate(each.getPredicates(), shadowColumnConditions));
    }
    
    private void parseAndPredicate(final Collection<ExpressionSegment> predicates, final Collection<ShadowColumnCondition> shadowColumnConditions) {
        predicates.forEach(each -> parseExpressionSegment(each, shadowColumnConditions));
    }
    
    private void parseExpressionSegment(final ExpressionSegment expressionSegment, final Collection<ShadowColumnCondition> shadowColumnConditions) {
        Collection<ColumnSegment> columnSegments = ColumnExtractor.extract(expressionSegment);
        for (ColumnSegment each : columnSegments) {
            String columnName = each.getIdentifier().getValue();
            String tableName = extractTableName(each);
            ShadowExtractor.extractValues(expressionSegment, parameters).ifPresent(values -> shadowColumnConditions.add(new ShadowColumnCondition(tableName, columnName, values)));
        }
    }
    
    private String extractTableName(final ColumnSegment columnSegment) {
        Optional<OwnerSegment> owner = columnSegment.getOwner();
        return owner.isPresent() ? getTableAliasNameMappings().get(owner.get().getIdentifier().getValue()) : getTableAliasNameMappings().keySet().iterator().next();
    }
    
    @Override
    protected ShadowDetermineCondition createShadowDetermineCondition() {
        return new ShadowDetermineCondition(ShadowOperationType.SELECT);
    }
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return selectStatementContext.getAllTables();
    }
    
    // FIXME refactor the method when sql parses the note and puts it in the statement context
    @Override
    protected Optional<Collection<String>> parseSqlNotes() {
        Collection<String> result = new LinkedList<>();
        selectStatementContext.getSqlStatement().getCommentSegments().forEach(each -> result.add(each.getText()));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
}
