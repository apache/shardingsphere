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

package org.apache.shardingsphere.core.parse.antlr.filler.common.segment.impl.dql;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.expr.SubquerySegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.select.StarSelectItemSegment;
import org.apache.shardingsphere.core.parse.parser.constant.DerivedAlias;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.CommonSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parse.parser.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;

/**
 * Select item filler.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class SelectItemFiller implements SQLSegmentCommonFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (sqlSegment instanceof StarSelectItemSegment) {
            fillStarSelectItemSegment(shardingTableMetaData, (StarSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof ColumnSelectItemSegment) {
            fillColumnSelectItemSegment(shardingTableMetaData, (ColumnSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof ExpressionSelectItemSegment) {
            fillExpressionSelectItemSegment((ExpressionSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof AggregationSelectItemSegment) {
            fillAggregationSelectItemSegment((AggregationSelectItemSegment) sqlSegment, selectStatement, sql);
            return;
        }
        if (sqlSegment instanceof SubquerySegment) {
            fillSubquerySegment((SubquerySegment) sqlSegment, sqlStatement, sql, shardingTableMetaData);
        }
    }
    
    private void fillStarSelectItemSegment(final ShardingTableMetaData shardingTableMetaData, final StarSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.setContainStar(true);
        Optional<String> owner = selectItemSegment.getOwner();
        selectStatement.getItems().add(new StarSelectItem(owner.orNull()));
        if (owner.isPresent()) {
            Optional<Table> table = selectStatement.getTables().find(owner.get());
            if (table.isPresent() && !table.get().getAlias().isPresent() && shardingTableMetaData.containsTable(table.get().getName())) {
                selectStatement.addSQLToken(new TableToken(selectItemSegment.getStartIndex(), 
                        0, SQLUtil.getExactlyValue(owner.get()), SQLUtil.getLeftDelimiter(owner.get()), SQLUtil.getRightDelimiter(owner.get())));
            }
        }
    }
    
    private void fillColumnSelectItemSegment(final ShardingTableMetaData shardingTableMetaData, final ColumnSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        Optional<String> owner = selectItemSegment.getOwner();
        if (owner.isPresent()) {
            Optional<Table> table = selectStatement.getTables().find(owner.get());
            if (table.isPresent() && !table.get().getAlias().isPresent() && shardingTableMetaData.containsTable(table.get().getName())) {
                selectStatement.addSQLToken(new TableToken(selectItemSegment.getStartIndex(), 
                        0, SQLUtil.getExactlyValue(owner.get()), SQLUtil.getLeftDelimiter(owner.get()), SQLUtil.getRightDelimiter(owner.get())));
            }
        }
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getQualifiedName(), selectItemSegment.getAlias()));
    }
    
    private void fillExpressionSelectItemSegment(final ExpressionSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getExpression(), selectItemSegment.getAlias()));
    }
    
    private void fillAggregationSelectItemSegment(final AggregationSelectItemSegment selectItemSegment, final SelectStatement selectStatement, final String sql) {
        if (selectItemSegment instanceof AggregationDistinctSelectItemSegment) {
            fillAggregationDistinctSelectItemSegment((AggregationDistinctSelectItemSegment) selectItemSegment, selectStatement, sql);
        } else {
            selectStatement.getItems().add(new AggregationSelectItem(selectItemSegment.getType(), sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1), selectItemSegment.getAlias()));
        }
    }
    
    private void fillAggregationDistinctSelectItemSegment(final AggregationDistinctSelectItemSegment selectItemSegment, final SelectStatement selectStatement, final String sql) {
        selectStatement.getItems().add(
                new AggregationDistinctSelectItem(selectItemSegment.getType(), sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1), selectItemSegment.getAlias(), selectItemSegment.getDistinctExpression()));
        Optional<String> derivedAlias = Optional.absent();
        if (DerivedAlias.isDerivedAlias(selectItemSegment.getAlias().get())) {
            derivedAlias = Optional.of(selectItemSegment.getAlias().get());
        }
        selectStatement.getSQLTokens().add(new AggregationDistinctToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), selectItemSegment.getDistinctExpression(), derivedAlias));
    }
    
    private void fillSubquerySegment(final SubquerySegment subquerySegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        new SubqueryFiller().fill(subquerySegment, sqlStatement, sql, shardingTableMetaData);
    }
}
