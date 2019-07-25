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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.item.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.CommonSelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.DerivedColumn;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.ShorthandSelectItem;
import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

/**
 * Select item engine.
 *
 * @author zhangliang
 */
public final class SelectItemEngine {
    
    private int aggregationAverageDerivedColumnCount;
    
    private int aggregationDistinctDerivedColumnCount;
    
    /**
     * Create select item.
     * 
     * @param sql SQL
     * @param selectItemSegment select item segment
     * @return select item
     */
    public Optional<SelectItem> createSelectItem(final String sql, final SelectItemSegment selectItemSegment) {
        if (selectItemSegment instanceof ShorthandSelectItemSegment) {
            return Optional.<SelectItem>of(createSelectItem((ShorthandSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ColumnSelectItemSegment) {
            return Optional.<SelectItem>of(createSelectItem((ColumnSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ExpressionSelectItemSegment) {
            return Optional.<SelectItem>of(createSelectItem((ExpressionSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof AggregationDistinctSelectItemSegment) {
            return Optional.<SelectItem>of(createSelectItem(sql, (AggregationDistinctSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof AggregationSelectItemSegment) {
            return Optional.<SelectItem>of(createSelectItem(sql, (AggregationSelectItemSegment) selectItemSegment));
        }
        // TODO subquery
        return Optional.absent();
    }
    
    private ShorthandSelectItem createSelectItem(final ShorthandSelectItemSegment selectItemSegment) {
        Optional<TableSegment> owner = selectItemSegment.getOwner();
        return new ShorthandSelectItem(owner.isPresent() ? owner.get().getTableName() : null);
    }
    
    private CommonSelectItem createSelectItem(final ColumnSelectItemSegment selectItemSegment) {
        return new CommonSelectItem(selectItemSegment.getQualifiedName(), selectItemSegment.getAlias().orNull());
    }
    
    private CommonSelectItem createSelectItem(final ExpressionSelectItemSegment selectItemSegment) {
        return new CommonSelectItem(selectItemSegment.getText(), selectItemSegment.getAlias().orNull());
    }
    
    private AggregationDistinctSelectItem createSelectItem(final String sql, final AggregationDistinctSelectItemSegment selectItemSegment) {
        String innerExpression = sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1);
        String alias = selectItemSegment.getAlias().or(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++));
        AggregationDistinctSelectItem result = new AggregationDistinctSelectItem(selectItemSegment.getType(), innerExpression, alias, selectItemSegment.getDistinctExpression());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedItem(result);
        }
        return result;
    }
    
    private AggregationSelectItem createSelectItem(final String sql, final AggregationSelectItemSegment selectItemSegment) {
        String innerExpression = sql.substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1);
        AggregationSelectItem result = new AggregationSelectItem(selectItemSegment.getType(), innerExpression, selectItemSegment.getAlias().orNull());
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedItem(result);
            // TODO replace avg to constant, avoid calculate useless avg
        }
        return result;
    }
    
    private void appendAverageDistinctDerivedItem(final AggregationDistinctSelectItem averageDistinctSelectItem) {
        String innerExpression = averageDistinctSelectItem.getInnerExpression();
        String distinctInnerExpression = averageDistinctSelectItem.getDistinctInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctSelectItem countDistinctSelectItem = new AggregationDistinctSelectItem(AggregationType.COUNT, innerExpression, countAlias, distinctInnerExpression);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctSelectItem sumDistinctSelectItem = new AggregationDistinctSelectItem(AggregationType.SUM, innerExpression, sumAlias, distinctInnerExpression);
        averageDistinctSelectItem.getDerivedAggregationItems().add(countDistinctSelectItem);
        averageDistinctSelectItem.getDerivedAggregationItems().add(sumDistinctSelectItem);
        aggregationAverageDerivedColumnCount++;
    }
    
    private void appendAverageDerivedItem(final AggregationSelectItem averageSelectItem) {
        String innerExpression = averageSelectItem.getInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationSelectItem countSelectItem = new AggregationSelectItem(AggregationType.COUNT, innerExpression, countAlias);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationSelectItem sumSelectItem = new AggregationSelectItem(AggregationType.SUM, innerExpression, sumAlias);
        averageSelectItem.getDerivedAggregationItems().add(countSelectItem);
        averageSelectItem.getDerivedAggregationItems().add(sumSelectItem);
        aggregationAverageDerivedColumnCount++;
    }
}
