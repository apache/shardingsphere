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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.Pagination;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Select optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingSelectOptimizeEngine implements OptimizeEngine {
    
    private final SelectStatement selectStatement;
    
    private final List<Object> parameters;
    
    private final WhereClauseShardingConditionEngine shardingConditionEngine;
    
    private final WhereClauseEncryptConditionEngine encryptConditionEngine;
    
    public ShardingSelectOptimizeEngine(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final SelectStatement selectStatement, final List<Object> parameters) {
        this.selectStatement = selectStatement;
        this.parameters = parameters;
        shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, shardingTableMetaData);
        encryptConditionEngine = new WhereClauseEncryptConditionEngine(shardingRule.getEncryptRule(), shardingTableMetaData);
    }
    
    @Override
    public ShardingSelectOptimizedStatement optimize() {
        ShardingSelectOptimizedStatement result = new ShardingSelectOptimizedStatement(selectStatement, 
                new ArrayList<>(shardingConditionEngine.createShardingConditions(selectStatement, parameters)), 
                encryptConditionEngine.createEncryptConditions(selectStatement), appendAvgDerivedColumns(selectStatement.getItems()));
        setPagination(result);
        return result;
    }
    
    private Collection<SelectItem> appendAvgDerivedColumns(final Collection<SelectItem> items) {
        Collection<SelectItem> result = new LinkedList<>(items);
        int derivedColumnOffset = 0;
        for (SelectItem each : items) {
            if (isAverageSelectItem(each)) {
                appendAvgDerivedColumns(derivedColumnOffset, each);
                // TODO replace avg to constant, avoid calculate useless avg
                derivedColumnOffset++;
            }
        }
        return result;
    }
    
    private boolean isAverageSelectItem(final SelectItem each) {
        return each instanceof AggregationSelectItem && AggregationType.AVG == ((AggregationSelectItem) each).getType();
    }
    
    private void appendAvgDerivedColumns(final int derivedColumnOffset, final SelectItem selectItem) {
        if (selectItem instanceof AggregationDistinctSelectItem) {
            appendDerivedAggregationDistinctSelectItems((AggregationDistinctSelectItem) selectItem, derivedColumnOffset);
        } else {
            appendDerivedAggregationSelectItems((AggregationSelectItem) selectItem, derivedColumnOffset);
        }
    }
    
    private void appendDerivedAggregationDistinctSelectItems(final AggregationDistinctSelectItem averageDistinctSelectItem, final int derivedColumnOffset) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem countDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.COUNT, averageDistinctSelectItem.getInnerExpression(), Optional.of(countAlias), averageDistinctSelectItem.getDistinctColumnName());
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationDistinctSelectItem sumDistinctSelectItem = new AggregationDistinctSelectItem(
                AggregationType.SUM, averageDistinctSelectItem.getInnerExpression(), Optional.of(sumAlias), averageDistinctSelectItem.getDistinctColumnName());
        averageDistinctSelectItem.getDerivedAggregationSelectItems().clear();
        averageDistinctSelectItem.getDerivedAggregationSelectItems().add(countDistinctSelectItem);
        averageDistinctSelectItem.getDerivedAggregationSelectItems().add(sumDistinctSelectItem);
    }
    
    private void appendDerivedAggregationSelectItems(final AggregationSelectItem averageSelectItem, final int derivedColumnOffset) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, averageSelectItem.getInnerExpression(), Optional.of(countAlias));
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(derivedColumnOffset);
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, averageSelectItem.getInnerExpression(), Optional.of(sumAlias));
        averageSelectItem.getDerivedAggregationSelectItems().clear();
        averageSelectItem.getDerivedAggregationSelectItems().add(countSelectItem);
        averageSelectItem.getDerivedAggregationSelectItems().add(sumSelectItem);
    }
    
    private void setPagination(final ShardingSelectOptimizedStatement optimizedStatement) {
        if (null != selectStatement.getOffset() || null != selectStatement.getRowCount()) {
            optimizedStatement.setPagination(new Pagination(selectStatement.getOffset(), selectStatement.getRowCount(), parameters));
        }
    }
}
