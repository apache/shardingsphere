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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Select items.
 *
 * @author zhangliang
 * @author sunbufu
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString(exclude = "tableMetas")
public final class SelectItems {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final boolean distinctRow;
    
    private final Collection<SelectItem> items;
    
    private final Collection<TableSegment> tables;
    
    private final TableMetas tableMetas;
    
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
            if (each instanceof ShorthandSelectItem) {
                result.addAll(getShorthandColumnLabels((ShorthandSelectItem) each));
            } else {
                result.add(each.getColumnLabel());
            }
        }
        return result;
    }
    
    private Collection<String> getShorthandColumnLabels(final ShorthandSelectItem shorthandSelectItem) {
        return shorthandSelectItem.getOwner().isPresent() ? getQualifiedShorthandColumnLabels(shorthandSelectItem.getOwner().get()) : getUnqualifiedShorthandColumnLabels();
    }
    
    private Collection<String> getQualifiedShorthandColumnLabels(final String owner) {
        for (TableSegment each : tables) {
            if (owner.equalsIgnoreCase(each.getAlias().or(each.getTableName()))) {
                return tableMetas.get(each.getTableName()).getColumns().keySet();
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<String> getUnqualifiedShorthandColumnLabels() {
        Collection<String> result = new LinkedList<>();
        for (TableSegment each : tables) {
            result.addAll(tableMetas.get(each.getTableName()).getColumns().keySet());
        }
        return result;
    }
}
