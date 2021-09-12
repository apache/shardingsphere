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
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.route.future.engine.AbstractShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.future.engine.util.ShadowExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow update statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowUpdateStatementRoutingEngine extends AbstractShadowRouteEngine {
    
    private final UpdateStatementContext updateStatementContext;
    
    private final List<Object> parameters;
    
    @Override
    protected Optional<Map<String, Collection<Comparable<?>>>> parseColumnValuesMappings() {
        Map<String, Collection<Comparable<?>>> result = new LinkedHashMap<>();
        Optional<WhereSegment> where = updateStatementContext.getWhere();
        where.ifPresent(whereSegment -> parseWhereSegment(whereSegment, result));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private void parseWhereSegment(final WhereSegment whereSegment, final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        ExpressionExtractUtil.getAndPredicates(whereSegment.getExpr()).forEach(each -> parseAndPredicate(each.getPredicates(), columnValuesMappings));
    }
    
    private void parseAndPredicate(final Collection<ExpressionSegment> predicates, final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        for (ExpressionSegment each : predicates) {
            parseExpressionSegment(each, columnValuesMappings);
        }
    }
    
    private void parseExpressionSegment(final ExpressionSegment expressionSegment, final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        if (expressionSegment instanceof BinaryOperationExpression) {
            parseBinaryOperationExpression((BinaryOperationExpression) expressionSegment, columnValuesMappings);
        }
        if (expressionSegment instanceof InExpression) {
            parseInExpression((InExpression) expressionSegment, columnValuesMappings);
        }
    }
    
    private void parseInExpression(final InExpression expression, final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        Optional<String> columnName = ShadowExtractor.extractColumnName(expression);
        columnName.ifPresent(s -> ShadowExtractor.extractValues(expression.getRight(), parameters).ifPresent(values -> columnValuesMappings.put(s, values)));
    }
    
    private void parseBinaryOperationExpression(final BinaryOperationExpression expression, final Map<String, Collection<Comparable<?>>> columnValuesMappings) {
        Optional<String> columnName = ShadowExtractor.extractColumnName(expression);
        columnName.ifPresent(s -> ShadowExtractor.extractValues(expression.getRight(), parameters).ifPresent(values -> columnValuesMappings.put(s, values)));
    }
    
    @Override
    protected ShadowDetermineCondition createShadowDetermineCondition() {
        return new ShadowDetermineCondition(ShadowOperationType.UPDATE);
    }
    
    @Override
    protected Collection<SimpleTableSegment> getAllTables() {
        return updateStatementContext.getAllTables();
    }
    
    // FIXME refactor the method when sql parses the note and puts it in the statement context
    @Override
    protected Optional<Collection<String>> parseSqlNotes() {
        Collection<String> result = new LinkedList<>();
        result.add("/*foo=bar,shadow=true*/");
        result.add("/*aaa=bbb*/");
        return Optional.of(result);
    }
}
