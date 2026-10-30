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

package org.apache.shardingsphere.infra.binder.postgresql;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.DialectProjectionIdentifierExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Projection identifier extractor for PostgreSQL.
 */
public final class PostgreSQLProjectionIdentifierExtractor implements DialectProjectionIdentifierExtractor {
    
    @Override
    public String getIdentifierValue(final IdentifierValue identifierValue) {
        return PostgreSQLIdentifierUtils.isUnicodeQuoted(identifierValue.getValue())
                ? PostgreSQLIdentifierUtils.unquoteUnicode(identifierValue.getValue())
                : PostgreSQLIdentifierUtils.fold(identifierValue.getValue());
    }
    
    @Override
    public String getColumnNameFromFunction(final String functionName, final String functionExpression) {
        return PostgreSQLIdentifierUtils.fold(functionName);
    }
    
    @Override
    public String getColumnNameFromExpression(final ExpressionSegment expressionSegment) {
        if (!(expressionSegment instanceof ExpressionProjectionSegment)) {
            return "?column?";
        }
        IdentifierValue defaultColumnLabel = ((ExpressionProjectionSegment) expressionSegment).getDefaultColumnLabel();
        if (null != defaultColumnLabel) {
            return getIdentifierValue(defaultColumnLabel);
        }
        ExpressionSegment innerExpressionSegment = ((ExpressionProjectionSegment) expressionSegment).getExpr();
        while (innerExpressionSegment instanceof TypeCastExpression) {
            innerExpressionSegment = ((TypeCastExpression) innerExpressionSegment).getExpression();
        }
        if (innerExpressionSegment instanceof FunctionSegment) {
            return getColumnNameFromFunctionSegment((FunctionSegment) innerExpressionSegment);
        }
        if (innerExpressionSegment instanceof AggregationProjectionSegment) {
            AggregationProjectionSegment aggregationProjectionSegment = (AggregationProjectionSegment) innerExpressionSegment;
            return getColumnNameFromFunction(aggregationProjectionSegment.getType().name(), aggregationProjectionSegment.getExpression());
        }
        return "?column?";
    }
    
    private String getColumnNameFromFunctionSegment(final FunctionSegment functionSegment) {
        if (PostgreSQLIdentifierUtils.isUnicodeQuoted(functionSegment.getFunctionName())) {
            return PostgreSQLIdentifierUtils.unquoteUnicode(functionSegment.getFunctionName());
        }
        IdentifierValue functionName = new IdentifierValue(functionSegment.getFunctionName());
        return QuoteCharacter.NONE == functionName.getQuoteCharacter()
                ? getColumnNameFromFunction(functionName.getValue(), functionSegment.getText())
                : functionName.getValue();
    }
    
    @Override
    public String getColumnNameFromSubquery(final SubqueryProjectionSegment subquerySegment) {
        return getColumnNameFromProjection(subquerySegment.getSubquery().getSelect().getProjections().getProjections().iterator().next(),
                new ProjectionIdentifierExtractEngine(subquerySegment.getSubquery().getSelect().getDatabaseType()));
    }
    
    private String getColumnNameFromProjection(final ProjectionSegment projection, final ProjectionIdentifierExtractEngine extractEngine) {
        if (projection instanceof AliasAvailable && ((AliasAvailable) projection).getAlias().isPresent()) {
            return extractEngine.getIdentifierValue(((AliasAvailable) projection).getAlias().get());
        }
        if (projection instanceof ShorthandProjectionSegment) {
            return getColumnNameFromProjection(((ShorthandProjectionSegment) projection).getActualProjectionSegments().iterator().next(), extractEngine);
        }
        if (projection instanceof ColumnProjectionSegment) {
            return extractEngine.getIdentifierValue(((ColumnProjectionSegment) projection).getColumn().getIdentifier());
        }
        if (projection instanceof AggregationProjectionSegment) {
            return getColumnNameFromFunction(((AggregationProjectionSegment) projection).getType().name(), ((AggregationProjectionSegment) projection).getExpression());
        }
        if (projection instanceof SubqueryProjectionSegment) {
            return getColumnNameFromSubquery((SubqueryProjectionSegment) projection);
        }
        return projection instanceof ExpressionSegment ? getColumnNameFromExpression((ExpressionSegment) projection) : projection.getColumnLabel();
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
