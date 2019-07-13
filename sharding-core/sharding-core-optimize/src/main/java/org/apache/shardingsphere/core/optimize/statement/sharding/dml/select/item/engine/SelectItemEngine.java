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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.AggregationSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.CommonSelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.ShorthandSelectItem;
import org.apache.shardingsphere.core.parse.constant.DerivedColumn;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationDistinctSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.AggregationSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ExpressionSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

/**
 * Select items engine.
 *
 * @author zhangliang
 */
public final class SelectItemEngine {
    
    private int derivedColumnCount;
    
    /**
     * Create select item.
     * 
     * @param selectItemSegment select item segment
     * @param sqlStatement SQL statement
     * @return select item
     */
    public Optional<SelectItem> createSelectItem(final SelectItemSegment selectItemSegment, final SQLStatement sqlStatement) {
        if (selectItemSegment instanceof ShorthandSelectItemSegment) {
            return Optional.<SelectItem>of(createShorthandSelectItemSegment((ShorthandSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ColumnSelectItemSegment) {
            return Optional.<SelectItem>of(createColumnSelectItemSegment((ColumnSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof ExpressionSelectItemSegment) {
            return Optional.<SelectItem>of(createExpressionSelectItemSegment((ExpressionSelectItemSegment) selectItemSegment));
        }
        if (selectItemSegment instanceof AggregationSelectItemSegment) {
            return Optional.<SelectItem>of(createAggregationSelectItemSegment((AggregationSelectItemSegment) selectItemSegment, sqlStatement));
        }
        // TODO subquery
        return Optional.absent();
    }
    
    private ShorthandSelectItem createShorthandSelectItemSegment(final ShorthandSelectItemSegment selectItemSegment) {
        Optional<TableSegment> owner = selectItemSegment.getOwner();
        return new ShorthandSelectItem(owner.isPresent() ? owner.get().getName() : null);
    }
    
    private CommonSelectItem createColumnSelectItemSegment(final ColumnSelectItemSegment selectItemSegment) {
        return new CommonSelectItem(selectItemSegment.getQualifiedName(), selectItemSegment.getAlias().orNull());
    }
    
    private CommonSelectItem createExpressionSelectItemSegment(final ExpressionSelectItemSegment selectItemSegment) {
        return new CommonSelectItem(selectItemSegment.getText(), selectItemSegment.getAlias().orNull());
    }
    
    private AggregationSelectItem createAggregationSelectItemSegment(final AggregationSelectItemSegment selectItemSegment, final SQLStatement sqlStatement) {
        String innerExpression = sqlStatement.getLogicSQL().substring(selectItemSegment.getInnerExpressionStartIndex(), selectItemSegment.getStopIndex() + 1);
        if (selectItemSegment instanceof AggregationDistinctSelectItemSegment) {
            String alias = selectItemSegment.getAlias().or(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(derivedColumnCount++));
            return new AggregationDistinctSelectItem(selectItemSegment.getType(), innerExpression, alias, ((AggregationDistinctSelectItemSegment) selectItemSegment).getDistinctExpression());
        }
        return new AggregationSelectItem(selectItemSegment.getType(), innerExpression, selectItemSegment.getAlias().orNull());
    }
}
