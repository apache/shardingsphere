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
        if (sqlSegment instanceof StarSelectItemSegment) {
            fillStarSelectItemSegment((StarSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof ColumnSelectItemSegment) {
            fillColumnSelectItemSegment((ColumnSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof ExpressionSelectItemSegment) {
            fillExpressionSelectItemSegment((ExpressionSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof FunctionSelectItemSegment) {
            fillFunctionSelectItemSegment((FunctionSelectItemSegment) sqlSegment, selectStatement, sql);
            return;
        }
        if (sqlSegment instanceof SubquerySegment) {
            fillSubquerySegment((SubquerySegment) sqlSegment, sqlStatement, sql, shardingRule, shardingTableMetaData);
        }
    }
    
    private void fillStarSelectItemSegment(final StarSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.setContainStar(true);
        Optional<String> owner = selectItemSegment.getOwner();
        selectStatement.getItems().add(new StarSelectItem(owner.orNull()));
        if (!owner.isPresent()) {
            return;
        }
        Optional<Table> table = selectStatement.getTables().find(owner.get());
        if (table.isPresent() && !table.get().getAlias().isPresent()) {
            selectStatement.addSQLToken(new TableToken(selectItemSegment.getStartIndex(), 0, owner.get()));
        }
    }
    
    private void fillColumnSelectItemSegment(final ColumnSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        Optional<String> owner = selectItemSegment.getOwner();
        if (owner.isPresent() && selectStatement.getTables().getTableNames().contains(owner.get())) {
            selectStatement.addSQLToken(new TableToken(selectItemSegment.getStartIndex(), 0, owner.get()));
        }
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getQualifiedName(), selectItemSegment.getAlias()));
    }
    
    private void fillExpressionSelectItemSegment(final ExpressionSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getExpression(), selectItemSegment.getAlias()));
    }
    
    private void fillFunctionSelectItemSegment(final FunctionSelectItemSegment selectItemSegment, final SelectStatement selectStatement, final String sql) {
        Optional<AggregationType> aggregationType = findAggregationType(selectItemSegment);
        String innerExpression = sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getInnerExpressionStopIndex() + 1);
        String functionExpression = sql.substring(selectItemSegment.getFunctionStartIndex(), selectItemSegment.getInnerExpressionStopIndex() + 1);
        if (aggregationType.isPresent()) {
            if (selectItemSegment.hasDistinct()) {
                String columnName = sql.substring(selectItemSegment.getDistinctExpressionStartIndex(), selectItemSegment.getInnerExpressionStopIndex());
                selectStatement.getItems().add(new AggregationDistinctSelectItem(aggregationType.get(), innerExpression, selectItemSegment.getAlias(), columnName));
                Optional<String> autoAlias = Optional.absent();
                if (DerivedAlias.isDerivedAlias(selectItemSegment.getAlias().get())) {
                    autoAlias = Optional.of(selectItemSegment.getAlias().get());
                }
                selectStatement.getSQLTokens().add(new AggregationDistinctToken(selectItemSegment.getFunctionStartIndex(), functionExpression, columnName, autoAlias));
            } else {
                selectStatement.getItems().add(new AggregationSelectItem(aggregationType.get(), innerExpression, selectItemSegment.getAlias()));
            }
        } else {
            selectStatement.getItems().add(new CommonSelectItem(functionExpression, selectItemSegment.getAlias()));
        }
    }
    
    private Optional<AggregationType> findAggregationType(final FunctionSelectItemSegment selectItemSegment) {
        for (AggregationType each : AggregationType.values()) {
            if (each.name().equalsIgnoreCase(selectItemSegment.getFunctionName())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private void fillSubquerySegment(
            final SubquerySegment subquerySegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        new SubqueryFiller().fill(subquerySegment, sqlStatement, sql, shardingRule, shardingTableMetaData);
    }
}
