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
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.SelectClauseSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.FunctionSelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.SelectItemSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.select.StarSelectItemSegment;
import org.apache.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import org.apache.shardingsphere.core.parsing.parser.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Select clause filler.
 *
 * @author duhongjun
 */
public final class SelectClauseFiller implements SQLStatementFiller<SelectClauseSegment> {
    
    private SelectItemFiller selectItemFiller = new SelectItemFiller();
    
    @Override
    public void fill(final SelectClauseSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setFirstSelectItemStartIndex(sqlSegment.getFirstSelectItemStartIndex());
        selectStatement.setSelectListStopIndex(sqlSegment.getSelectItemsStopIndex());
        if (sqlSegment.getSelectItems().isEmpty()) {
            return;
        }
        if (sqlSegment.isHasDistinct()) {
            fillDistinct(sqlSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
        } else {
            int offset = 0;
            for (SelectItemSegment each : sqlSegment.getSelectItems()) {
                offset = setDistinctFunctionAlias(each, offset);
                selectItemFiller.fill(each, sqlStatement, sql, shardingRule, shardingTableMetaData);
            }
        }
    }
    
    private void fillDistinct(final SelectClauseSegment selectClauseSegment, final SelectStatement selectStatement, final String sql, final ShardingRule shardingRule,
                              final ShardingTableMetaData shardingTableMetaData) {
        Iterator<SelectItemSegment> selectItemSegmentIterator = selectClauseSegment.getSelectItems().iterator();
        SelectItemSegment firstSelectItemSegment = selectItemSegmentIterator.next();
        Set<String> distinctColumnNames = new LinkedHashSet<>();
        DistinctSelectItem distinctSelectItem = null;
        int offset = 0;
        if (firstSelectItemSegment instanceof StarSelectItemSegment) {
            selectItemFiller.fill(firstSelectItemSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
            selectStatement.getItems().add(new DistinctSelectItem(distinctColumnNames, Optional.<String>absent()));
        } else if (firstSelectItemSegment instanceof ColumnSelectItemSegment) {
            ColumnSelectItemSegment columnSelectItemSegment = (ColumnSelectItemSegment) firstSelectItemSegment;
            distinctSelectItem = new DistinctSelectItem(distinctColumnNames, columnSelectItemSegment.getAlias());
            selectStatement.getItems().add(distinctSelectItem);
            distinctColumnNames.add(columnSelectItemSegment.getName());
        } else if (firstSelectItemSegment instanceof ExpressionSelectItemSegment) {
            distinctSelectItem = createDistinctExpressionItem(selectStatement, sql, distinctColumnNames, (ExpressionSelectItemSegment) firstSelectItemSegment);
        } else {
            offset = setDistinctFunctionAlias(firstSelectItemSegment, offset);
            selectItemFiller.fill(firstSelectItemSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
        }
        while (selectItemSegmentIterator.hasNext()) {
            SelectItemSegment nextSelectItemSegment = selectItemSegmentIterator.next();
            selectItemFiller.fill(nextSelectItemSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
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
        if (selectItemSegment instanceof FunctionSelectItemSegment) {
            FunctionSelectItemSegment functionSelectItemSegment = (FunctionSelectItemSegment) selectItemSegment;
            Optional<String> alias = functionSelectItemSegment.getAlias();
            if (functionSelectItemSegment.hasDistinct() && !alias.isPresent()) {
                ((FunctionSelectItemSegment) selectItemSegment).setAlias(DerivedAlias.AGGREGATION_DISTINCT_DERIVED.getDerivedAlias(offset));
                return offset + 1;
            }
        }
        return offset;
    }
    
    private DistinctSelectItem createDistinctExpressionItem(final SelectStatement selectStatement, final String sql, final Set<String> distinctColumnNames,
                                                            final ExpressionSelectItemSegment expressionSelectItemSegment) {
        DistinctSelectItem distinctSelectItem = new DistinctSelectItem(distinctColumnNames, expressionSelectItemSegment.getAlias());
        String commonExpression = sql.substring(expressionSelectItemSegment.getStartIndex(), expressionSelectItemSegment.getStopIndex() + 1);
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
