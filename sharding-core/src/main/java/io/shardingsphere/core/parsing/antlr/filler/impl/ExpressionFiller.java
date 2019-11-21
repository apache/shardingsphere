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

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.filler.impl.dql.SubqueryFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.FunctionExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.PropertyExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.StarExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import io.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Expression filler.
 *
 * @author duhongjun
 */
public final class ExpressionFiller implements SQLStatementFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (sqlSegment instanceof PropertyExpressionSegment) {
            fillPropertyExpression((PropertyExpressionSegment) sqlSegment, selectStatement, sql);
            return;
        }
        if (sqlSegment instanceof CommonExpressionSegment) {
            CommonExpressionSegment commonSegment = (CommonExpressionSegment) sqlSegment;
            String expression = sql.substring(commonSegment.getStartPosition(), commonSegment.getEndPosition() + 1);
            selectStatement.getItems().add(new CommonSelectItem(expression, commonSegment.getAlias()));
            return;
        }
        if (sqlSegment instanceof StarExpressionSegment) {
            fillStarExpression((StarExpressionSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof FunctionExpressionSegment) {
            fillFunctionExpression((FunctionExpressionSegment) sqlSegment, selectStatement, sql);
            return;
        }
        if (sqlSegment instanceof SubquerySegment) {
            SubquerySegment subquerySegment = (SubquerySegment) sqlSegment;
            new SubqueryFiller().fill(subquerySegment, sqlStatement, sql, shardingRule, shardingTableMetaData);
        }
    }
    
    private void fillStarExpression(final StarExpressionSegment starSegment, final SelectStatement selectStatement) {
        selectStatement.setContainStar(true);
        Optional<String> owner = starSegment.getOwner();
        selectStatement.getItems().add(new StarSelectItem(owner.orNull()));
        if (!owner.isPresent()) {
            return;
        }
        Optional<Table> table = selectStatement.getTables().find(owner.get());
        if (table.isPresent() && !table.get().getAlias().isPresent()) {
            selectStatement.addSQLToken(new TableToken(starSegment.getStartPosition(), 0, owner.get()));
        }
    }
    
    private void fillPropertyExpression(final PropertyExpressionSegment propertySegment, final SelectStatement selectStatement, final String sql) {
        Optional<String> owner = propertySegment.getOwner();
        if (owner.isPresent() && selectStatement.getTables().getTableNames().contains(owner.get())) {
            selectStatement.addSQLToken(new TableToken(propertySegment.getStartPosition(), 0, owner.get()));
        }
        String expression = sql.substring(propertySegment.getStartPosition(), propertySegment.getEndPosition() + 1);
        selectStatement.getItems().add(new CommonSelectItem(expression, propertySegment.getAlias()));
    }
    
    private void fillFunctionExpression(final FunctionExpressionSegment functionSegment, final SelectStatement selectStatement, final String sql) {
        AggregationType aggregationType = null;
        for (AggregationType eachType : AggregationType.values()) {
            if (eachType.name().equalsIgnoreCase(functionSegment.getFunctionName())) {
                aggregationType = eachType;
                break;
            }
        }
        String innerExpression = sql.substring(functionSegment.getInnerExpressionStartIndex(), functionSegment.getInnerExpressionEndIndex() + 1);
        String functionExpression = sql.substring(functionSegment.getFunctionStartIndex(), functionSegment.getInnerExpressionEndIndex() + 1);
        if (null != aggregationType) {
            if (functionSegment.hasDistinct()) {
                String columnName = sql.substring(functionSegment.getDistinctColumnNameStartPosition(), functionSegment.getInnerExpressionEndIndex());
                selectStatement.getItems().add(new AggregationDistinctSelectItem(aggregationType, innerExpression, functionSegment.getAlias(), columnName));
                Optional<String> autoAlias = Optional.absent();
                if (DerivedAlias.isDerivedAlias(functionSegment.getAlias().get())) {
                    autoAlias = Optional.of(functionSegment.getAlias().get());
                }
                selectStatement.getSQLTokens().add(new AggregationDistinctToken(functionSegment.getFunctionStartIndex(), functionExpression, columnName, autoAlias));
            } else {
                selectStatement.getItems().add(new AggregationSelectItem(aggregationType, innerExpression, functionSegment.getAlias()));
            }
        } else {
            selectStatement.getItems().add(new CommonSelectItem(functionExpression, functionSegment.getAlias()));
        }
    }
}
