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

package org.apache.shardingsphere.core.parsing.antlr.filler.impl.dql;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.SubquerySegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.FunctionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.StarSelectItemSegment;
import org.apache.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parsing.parser.context.table.Table;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Select item filler.
 *
 * @author zhangliang
 */
public final class SelectItemFiller implements SQLStatementFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (sqlSegment instanceof ColumnSelectItemSegment) {
            fillColumn((ColumnSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof ExpressionSelectItemSegment) {
            ExpressionSelectItemSegment expressionSelectItemSegment = (ExpressionSelectItemSegment) sqlSegment;
            String expression = sql.substring(expressionSelectItemSegment.getStartIndex(), expressionSelectItemSegment.getStopIndex() + 1);
            selectStatement.getItems().add(new CommonSelectItem(expression, expressionSelectItemSegment.getAlias()));
            return;
        }
        if (sqlSegment instanceof StarSelectItemSegment) {
            fillStarSelectItemSegment((StarSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof FunctionSelectItemSegment) {
            fillFunctionSelectItemSegment((FunctionSelectItemSegment) sqlSegment, selectStatement, sql);
            return;
        }
        if (sqlSegment instanceof SubquerySegment) {
            SubquerySegment subquerySegment = (SubquerySegment) sqlSegment;
            new SubqueryFiller().fill(subquerySegment, sqlStatement, sql, shardingRule, shardingTableMetaData);
        }
    }
    
    private void fillStarSelectItemSegment(final StarSelectItemSegment starSelectItemSegment, final SelectStatement selectStatement) {
        selectStatement.setContainStar(true);
        Optional<String> owner = starSelectItemSegment.getOwner();
        selectStatement.getItems().add(new StarSelectItem(owner.orNull()));
        if (!owner.isPresent()) {
            return;
        }
        Optional<Table> table = selectStatement.getTables().find(owner.get());
        if (table.isPresent() && !table.get().getAlias().isPresent()) {
            selectStatement.addSQLToken(new TableToken(starSelectItemSegment.getStartIndex(), 0, owner.get()));
        }
    }
    
    private void fillColumn(final ColumnSelectItemSegment columnSelectItemSegment, final SelectStatement selectStatement) {
        Optional<String> owner = columnSelectItemSegment.getOwner();
        if (owner.isPresent() && selectStatement.getTables().getTableNames().contains(owner.get())) {
            selectStatement.addSQLToken(new TableToken(columnSelectItemSegment.getStartIndex(), 0, owner.get()));
        }
        selectStatement.getItems().add(new CommonSelectItem(columnSelectItemSegment.getQualifiedName(), columnSelectItemSegment.getAlias()));
    }
    
    private void fillFunctionSelectItemSegment(final FunctionSelectItemSegment functionSegment, final SelectStatement selectStatement, final String sql) {
        AggregationType aggregationType = null;
        for (AggregationType eachType : AggregationType.values()) {
            if (eachType.name().equalsIgnoreCase(functionSegment.getFunctionName())) {
                aggregationType = eachType;
                break;
            }
        }
        String innerExpression = sql.substring(functionSegment.getInnerExpressionStartIndex(), functionSegment.getInnerExpressionStopIndex() + 1);
        String functionExpression = sql.substring(functionSegment.getFunctionStartIndex(), functionSegment.getInnerExpressionStopIndex() + 1);
        if (null != aggregationType) {
            if (functionSegment.hasDistinct()) {
                String columnName = sql.substring(functionSegment.getDistinctExpressionStartIndex(), functionSegment.getInnerExpressionStopIndex());
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
