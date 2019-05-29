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
import lombok.Setter;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.SelectItemPrefixToken;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Select items filler.
 *
 * @author duhongjun
 */
@Setter
public final class SelectItemsFiller implements SQLSegmentFiller<SelectItemsSegment> {
    
    private SelectItemFiller selectItemFiller;
    
    @Override
    public void fill(final SelectItemsSegment sqlSegment, final SQLStatement sqlStatement) {
        selectItemFiller = new SelectItemFiller();
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.getSQLTokens().add(new SelectItemPrefixToken(sqlSegment.getStartIndex()));
        selectStatement.setSelectListStopIndex(sqlSegment.getStopIndex());
        if (sqlSegment.isHasDistinct()) {
            fillDistinct(sqlSegment, selectStatement);
            return;
        }
        int offset = 0;
        for (SelectItemSegment each : sqlSegment.getSelectItems()) {
            offset = setDistinctFunctionAlias(each, offset);
            selectItemFiller.fill(each, sqlStatement);
        }
    }
    
    private void fillDistinct(final SelectItemsSegment selectItemsSegment, final SelectStatement selectStatement) {
        Iterator<SelectItemSegment> selectItemSegmentIterator = selectItemsSegment.getSelectItems().iterator();
        SelectItemSegment firstSelectItemSegment = selectItemSegmentIterator.next();
        Set<String> distinctColumnNames = new LinkedHashSet<>();
        DistinctSelectItem distinctSelectItem = null;
        int offset = 0;
        if (firstSelectItemSegment instanceof ShorthandSelectItemSegment) {
            selectItemFiller.fill(firstSelectItemSegment, selectStatement);
            selectStatement.getItems().add(new DistinctSelectItem(distinctColumnNames, Optional.<String>absent()));
        } else if (firstSelectItemSegment instanceof ColumnSelectItemSegment) {
            ColumnSelectItemSegment columnSelectItemSegment = (ColumnSelectItemSegment) firstSelectItemSegment;
            distinctSelectItem = new DistinctSelectItem(distinctColumnNames, columnSelectItemSegment.getAlias());
            selectStatement.getItems().add(distinctSelectItem);
            distinctColumnNames.add(columnSelectItemSegment.getName());
        } else if (firstSelectItemSegment instanceof ExpressionSelectItemSegment) {
            distinctSelectItem = createDistinctExpressionItem(selectStatement, distinctColumnNames, (ExpressionSelectItemSegment) firstSelectItemSegment);
        } else {
            offset = setDistinctFunctionAlias(firstSelectItemSegment, offset);
            selectItemFiller.fill(firstSelectItemSegment, selectStatement);
        }
        while (selectItemSegmentIterator.hasNext()) {
            SelectItemSegment nextSelectItemSegment = selectItemSegmentIterator.next();
            selectItemFiller.fill(nextSelectItemSegment, selectStatement);
            if (nextSelectItemSegment instanceof ColumnSelectItemSegment) {
                offset = setDistinctFunctionAlias(nextSelectItemSegment, offset);
                distinctColumnNames.add(((ColumnSelectItemSegment) nextSelectItemSegment).getName());
            }
        }
        if (null != distinctSelectItem) {
            distinctSelectItem.getDistinctColumnNames().addAll(distinctColumnNames);
        }
    }
    
    private int setDistinctFunctionAlias(final SelectItemSegment selectItemSegment, final int offset) {
        if (selectItemSegment instanceof AggregationSelectItemSegment) {
            AggregationSelectItemSegment aggregationSelectItemSegment = (AggregationSelectItemSegment) selectItemSegment;
            Optional<String> alias = aggregationSelectItemSegment.getAlias();
            if (aggregationSelectItemSegment instanceof AggregationDistinctSelectItemSegment && !alias.isPresent()) {
                ((AggregationSelectItemSegment) selectItemSegment).setAlias(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(offset));
                return offset + 1;
            }
        }
        return offset;
    }
    
    private DistinctSelectItem createDistinctExpressionItem(final SelectStatement selectStatement, final Set<String> distinctColumnNames,
                                                            final ExpressionSelectItemSegment expressionSelectItemSegment) {
        DistinctSelectItem distinctSelectItem = new DistinctSelectItem(distinctColumnNames, expressionSelectItemSegment.getAlias());
        String commonExpression = selectStatement.getLogicSQL().substring(expressionSelectItemSegment.getStartIndex(), expressionSelectItemSegment.getStopIndex() + 1);
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
