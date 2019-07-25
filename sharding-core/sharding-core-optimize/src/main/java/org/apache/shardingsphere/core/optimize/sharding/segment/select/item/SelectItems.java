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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.item;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Select items.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class SelectItems {
    
    private final Collection<SelectItem> items;
    
    private final boolean distinctRow;
    
    private final int selectListStopIndex;
    
    /**
     * Judge is unqualified shorthand item or not.
     * 
     * @return is unqualified shorthand item or not
     */
    public boolean isUnqualifiedShorthandItem() {
        if (1 != items.size()) {
            return false;
        }
        SelectItem item = items.iterator().next();
        return item instanceof ShorthandSelectItem && !((ShorthandSelectItem) item).getOwner().isPresent();
    }
    
    /**
     * Find alias.
     * 
     * @param itemName item name
     * @return item alias
     */
    public Optional<String> findAlias(final String itemName) {
        for (SelectItem each : items) {
            if (itemName.equalsIgnoreCase(each.getExpression())) {
                return each.getAlias();
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get aggregation select items.
     *
     * @return aggregation select items
     */
    public List<AggregationSelectItem> getAggregationSelectItems() {
        List<AggregationSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationSelectItem) {
                AggregationSelectItem aggregationSelectItem = (AggregationSelectItem) each;
                result.add(aggregationSelectItem);
                result.addAll(aggregationSelectItem.getDerivedAggregationItems());
            }
        }
        return result;
    }
    
    /**
     * Get aggregation distinct select items.
     *
     * @return aggregation distinct select items
     */
    public List<AggregationDistinctSelectItem> getAggregationDistinctSelectItems() {
        List<AggregationDistinctSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof AggregationDistinctSelectItem) {
                result.add((AggregationDistinctSelectItem) each);
            }
        }
        return result;
    }
    
    /**
     * Get column labels.
     * 
     * @return column labels
     */
    public List<String> getColumnLabels() {
        List<String> result = new ArrayList<>(items.size());
        for (SelectItem each : items) {
            // TODO read * from metadata
            if (!(each instanceof ShorthandSelectItem)) {
                result.add(each.getAlias().or(each.getExpression()));
            }
        }
        return result;
    }
}
