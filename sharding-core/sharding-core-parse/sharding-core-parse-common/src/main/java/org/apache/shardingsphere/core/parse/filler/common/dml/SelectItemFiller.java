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

package org.apache.shardingsphere.core.parse.filler.common.dml;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.sql.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.CommonSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.TopSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.AggregationDistinctToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.RowCountToken;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemPrefixToken;

/**
 * Select item filler.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SelectItemFiller implements SQLSegmentFiller<SelectItemSegment> {
    
    @Override
    public void fill(final SelectItemSegment sqlSegment, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        if (sqlSegment instanceof ShorthandSelectItemSegment) {
            fillShorthandSelectItemSegment((ShorthandSelectItemSegment) sqlSegment, selectStatement);
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
        if (sqlSegment instanceof AggregationSelectItemSegment) {
            fillAggregationSelectItemSegment((AggregationSelectItemSegment) sqlSegment, selectStatement);
            return;
        }
        if (sqlSegment instanceof SubquerySegment) {
            fillSubquerySegment((SubquerySegment) sqlSegment, sqlStatement);
        }
        if (sqlSegment instanceof TopSegment) {
            fillTopSegment((TopSegment) sqlSegment, selectStatement);
        }
    }
    
    private void fillShorthandSelectItemSegment(final ShorthandSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.setContainStar(true);
        Optional<TableSegment> owner = selectItemSegment.getOwner();
        selectStatement.getItems().add(new StarSelectItem(owner.isPresent() ? owner.get().getName() : null));
    }
    
    private void fillColumnSelectItemSegment(final ColumnSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getQualifiedName(), selectItemSegment.getAlias()));
    }
    
    private void fillExpressionSelectItemSegment(final ExpressionSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.getItems().add(new CommonSelectItem(selectItemSegment.getText(), selectItemSegment.getAlias()));
    }
    
    private void fillAggregationSelectItemSegment(final AggregationSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        if (selectItemSegment instanceof AggregationDistinctSelectItemSegment) {
            fillAggregationDistinctSelectItemSegment((AggregationDistinctSelectItemSegment) selectItemSegment, selectStatement);
        } else {
            selectStatement.getItems().add(new AggregationSelectItem(selectItemSegment.getType(),
                    selectStatement.getLogicSQL().substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1), selectItemSegment.getAlias()));
        }
    }
    
    private void fillAggregationDistinctSelectItemSegment(final AggregationDistinctSelectItemSegment selectItemSegment, final SelectStatement selectStatement) {
        selectStatement.getItems().add(new AggregationDistinctSelectItem(selectItemSegment.getType(), selectStatement.getLogicSQL().substring(selectItemSegment.getInnerExpressionStartIndex(), 
                selectItemSegment.getStopIndex() + 1), selectItemSegment.getAlias(), selectItemSegment.getDistinctExpression()));
        Optional<String> derivedAlias = Optional.absent();
        if (DerivedColumn.isDerivedColumn(selectItemSegment.getAlias().get())) {
            derivedAlias = Optional.of(selectItemSegment.getAlias().get());
        }
        selectStatement.getSQLTokens().add(new AggregationDistinctToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), selectItemSegment.getDistinctExpression(), derivedAlias));
        setSelectItemPrefixToken(selectStatement);
    }
    
    private void fillSubquerySegment(final SubquerySegment subquerySegment, final SQLStatement sqlStatement) {
        new SubqueryFiller().fill(subquerySegment, sqlStatement);
    }
    
    private void fillTopSegment(final TopSegment topSegment, final SelectStatement selectStatement) {
        Limit limit = new Limit();
        limit.setRowCount(getTopValueSegment(topSegment.getTop()));
        selectStatement.setLimit(limit);
        if (-1 != limit.getRowCount().getValue()) {
            selectStatement.addSQLToken(new RowCountToken(topSegment.getTop().getStartIndex(), topSegment.getTop().getStopIndex(), limit.getRowCount().getValue()));
        }
        selectStatement.getItems().add(new CommonSelectItem("rownum", Optional.of(topSegment.getRowNumberAlias())));
    }
    
    private LimitValue getTopValueSegment(final LimitValueSegment topValueSegment) {
        return topValueSegment instanceof ParameterMarkerLimitValueSegment ? new LimitValue(-1, ((ParameterMarkerLimitValueSegment) topValueSegment).getParameterIndex(), false)
                : new LimitValue(((NumberLiteralLimitValueSegment) topValueSegment).getValue(), -1, false);
    }
    
    private void setSelectItemPrefixToken(final SelectStatement selectStatement) {
        Optional<SelectItemPrefixToken> selectItemPrefixToken = selectStatement.findSQLToken(SelectItemPrefixToken.class);
        if (selectItemPrefixToken.isPresent()) {
            selectItemPrefixToken.get().setToAppendDistinct(true);
        }
    }
}
