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
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
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
            fillForDisinct(selectClauseSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
        } else {
            ExpressionFiller expressionFiller = new ExpressionFiller();
            for (ExpressionSegment each : selectClauseSegment.getExpressions()) {
                expressionFiller.fill(each, sqlStatement, sql, shardingRule, shardingTableMetaData);
            }
        }
    }
    
    private void fillForDisinct(final SelectClauseSegment selectClauseSegment, SelectStatement selectStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        Iterator<ExpressionSegment> expressionIterator = selectClauseSegment.getExpressions().iterator();
        ExpressionSegment firstExpression = expressionIterator.next();
        ExpressionFiller expressionFiller = new ExpressionFiller();
        Set<String> distinctColumnNames = new HashSet<String>();
        DistinctSelectItem distinctSelectItem = null;
        if (firstExpression instanceof StarExpressionSegment) {
            expressionFiller.fill(firstExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
            selectStatement.getItems().add(new DistinctSelectItem(distinctColumnNames, Optional.<String>absent()));
        } else if (firstExpression instanceof PropertyExpressionSegment) {
            PropertyExpressionSegment propertyExpressionSegment = (PropertyExpressionSegment) firstExpression;
            distinctSelectItem = new DistinctSelectItem(distinctColumnNames, propertyExpressionSegment.getAlias());
            selectStatement.getItems().add(distinctSelectItem);
            distinctColumnNames.add(propertyExpressionSegment.getName());
        } else {
            expressionFiller.fill(firstExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
        }
        while (expressionIterator.hasNext()) {
            ExpressionSegment nextExpression = expressionIterator.next();
            expressionFiller.fill(nextExpression, selectStatement, sql, shardingRule, shardingTableMetaData);
            if (nextExpression instanceof PropertyExpressionSegment) {
                distinctColumnNames.add(((PropertyExpressionSegment) nextExpression).getName());
            }
        }
        if(null != distinctSelectItem) {
            distinctSelectItem.getDistinctColumnNames().addAll(distinctColumnNames);
        }
    }
}
