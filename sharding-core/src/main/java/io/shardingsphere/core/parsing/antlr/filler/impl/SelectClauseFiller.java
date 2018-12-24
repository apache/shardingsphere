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

package io.shardingsphere.core.parsing.antlr.filler.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Optional;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import io.shardingsphere.core.parsing.parser.context.selectitem.DistinctSelectItem;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Select clause filler.
 *
 * @author duhongjun
 */
public final class SelectClauseFiller implements SQLStatementFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectClauseSegment selectClauseSegment = (SelectClauseSegment) sqlSegment;
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setSelectListLastPosition(selectClauseSegment.getSelectListLastPosition());
        if (selectClauseSegment.getExpressions().isEmpty()) {
            return;
        }
        if (selectClauseSegment.isHasDistinct()) {
            fillForDistinct(selectClauseSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
        } else {
            ExpressionFiller expressionFiller = new ExpressionFiller();
            int offset = 0;
            for (ExpressionSegment each : selectClauseSegment.getExpressions()) {
                offset = setDistinctFunctionAlias(each, offset);
                expressionFiller.fill(each, sqlStatement, sql, shardingRule, shardingTableMetaData);
            }
        }
    }
    
    private void fillForDistinct(final SelectClauseSegment selectClauseSegment, final SelectStatement selectStatement, final String sql, final ShardingRule shardingRule,
                                 final ShardingTableMetaData shardingTableMetaData) {
        Iterator<ExpressionSegment> expressionIterator = selectClauseSegment.getExpressions().iterator();
        ExpressionSegment firstExpression = expressionIterator.next();
        ExpressionFiller expressionFiller = new ExpressionFiller();
        Set<String> distinctColumnNames = new HashSet<String>();
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
        if(expressionSegment instanceof FunctionExpressionSegment) {
            FunctionExpressionSegment functionExpressionSegment = (FunctionExpressionSegment)expressionSegment;
            Optional<String> alias = functionExpressionSegment.getAlias();
            if(functionExpressionSegment.isHasDistinct() && !alias.isPresent()) {
                alias = Optional.of(DerivedAlias.AGGREGATION_DISTINCT_DERIVED.getDerivedAlias(offset));
                ((FunctionExpressionSegment)expressionSegment).setAlias(alias);
                return offset+1;
            }
        }
        return offset;
    }
}
