/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.filler.impl.dql;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.filler.impl.ExpressionFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import io.shardingsphere.core.parsing.parser.context.selectitem.DistinctSelectItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Select clause filler.
 *
 * @author duhongjun
 */
public final class SelectClauseFiller implements SQLStatementFiller<SelectClauseSegment> {
    
    @Override
    public void fill(final SelectClauseSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setFirstSelectItemStartPosition(sqlSegment.getFirstSelectItemStartPosition());
        selectStatement.setSelectListLastPosition(sqlSegment.getSelectListLastPosition());
        if (sqlSegment.getExpressions().isEmpty()) {
            return;
        }
        if (sqlSegment.isHasDistinct()) {
            fillDistinct(sqlSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
        } else {
            ExpressionFiller expressionFiller = new ExpressionFiller();
            int offset = 0;
            for (ExpressionSegment each : sqlSegment.getExpressions()) {
                offset = setDistinctFunctionAlias(each, offset);
                expressionFiller.fill(each, sqlStatement, sql, shardingRule, shardingTableMetaData);
            }
        }
    }
    
    private void fillDistinct(final SelectClauseSegment selectClauseSegment, final SelectStatement selectStatement, final String sql, final ShardingRule shardingRule,
                              final ShardingTableMetaData shardingTableMetaData) {
        Iterator<ExpressionSegment> expressionIterator = selectClauseSegment.getExpressions().iterator();
        ExpressionSegment firstExpression = expressionIterator.next();
        ExpressionFiller expressionFiller = new ExpressionFiller();
        Set<String> distinctColumnNames = new LinkedHashSet<>();
        DistinctSelectItem distinctSelectItem = null;
        int offset = 0;
        if (firstExpression instanceof StarExpressionSegment) {
            expressionFiller.fill(firstExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
            selectStatement.getItems().add(new DistinctSelectItem(distinctColumnNames, Optional.<String>absent()));
        } else if (firstExpression instanceof PropertyExpressionSegment) {
            PropertyExpressionSegment propertyExpressionSegment = (PropertyExpressionSegment) firstExpression;
            distinctSelectItem = new DistinctSelectItem(distinctColumnNames, propertyExpressionSegment.getAlias());
            selectStatement.getItems().add(distinctSelectItem);
            distinctColumnNames.add(propertyExpressionSegment.getName());
        } else if (firstExpression instanceof CommonExpressionSegment) {
            distinctSelectItem = createDistinctCommonItem(selectStatement, sql, distinctColumnNames, (CommonExpressionSegment) firstExpression);
        } else {
            offset = setDistinctFunctionAlias(firstExpression, offset);
            expressionFiller.fill(firstExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
        }
        while (expressionIterator.hasNext()) {
            ExpressionSegment nextExpression = expressionIterator.next();
            expressionFiller.fill(nextExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
            if (nextExpression instanceof PropertyExpressionSegment) {
                offset = setDistinctFunctionAlias(nextExpression, offset);
                distinctColumnNames.add(((PropertyExpressionSegment) nextExpression).getName());
            }
        }
        if (null != distinctSelectItem) {
            distinctSelectItem.getDistinctColumnNames().addAll(distinctColumnNames);
        }
    }
    
    private int setDistinctFunctionAlias(final ExpressionSegment expressionSegment, final int offset) {
        if (expressionSegment instanceof FunctionExpressionSegment) {
            FunctionExpressionSegment functionExpressionSegment = (FunctionExpressionSegment) expressionSegment;
            Optional<String> alias = functionExpressionSegment.getAlias();
            if (functionExpressionSegment.hasDistinct() && !alias.isPresent()) {
                ((FunctionExpressionSegment) expressionSegment).setAlias(DerivedAlias.AGGREGATION_DISTINCT_DERIVED.getDerivedAlias(offset));
                return offset + 1;
            }
        }
        return offset;
    }
    
    private DistinctSelectItem createDistinctCommonItem(final SelectStatement selectStatement, final String sql, final Set<String> distinctColumnNames,
                                                        final CommonExpressionSegment expressionSegment) {
        DistinctSelectItem distinctSelectItem = new DistinctSelectItem(distinctColumnNames, expressionSegment.getAlias());
        String commonExpression = sql.substring(expressionSegment.getStartPosition(), expressionSegment.getEndPosition() + 1);
        int leftParenPosition = commonExpression.indexOf("(");
        if (0 <= leftParenPosition) {
            int rightParenPosition = commonExpression.lastIndexOf(")");
            if (0 > rightParenPosition) {
                rightParenPosition = commonExpression.length();
            }
            distinctColumnNames.add(commonExpression.substring(leftParenPosition + 1, rightParenPosition));
        }
        selectStatement.getItems().add(distinctSelectItem);
        return distinctSelectItem;
    }
}
