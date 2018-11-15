/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.sql.dql.select;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.parsing.parser.context.OrderItem;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import io.shardingsphere.core.parsing.parser.token.OffsetToken;
import io.shardingsphere.core.parsing.parser.token.RowCountToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.util.SQLUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Select statement.
 *
 * @author zhangliang
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class SelectStatement extends DQLStatement {
    
    private boolean containStar;
    
    private int selectListLastPosition;
    
    private int groupByLastPosition;
    
    private final Set<SelectItem> items = new HashSet<>();
    
    private final List<OrderItem> groupByItems = new LinkedList<>();
    
    private final List<OrderItem> orderByItems = new LinkedList<>();
    
    private Limit limit;
    
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private SelectStatement subQueryStatement;
    
    /**
     * Get alias.
     * 
     * @param name name or alias
     * @return alias
     */
    public Optional<String> getAlias(final String name) {
        if (containStar) {
            return Optional.absent();
        }
        String rawName = SQLUtil.getExactlyValue(name);
        for (SelectItem each : items) {
            if (rawName.equalsIgnoreCase(SQLUtil.getExactlyValue(each.getExpression()))) {
                return each.getAlias();
            }
            if (rawName.equalsIgnoreCase(each.getAlias().orNull())) {
                return Optional.of(rawName);
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
                result.addAll(aggregationSelectItem.getDerivedAggregationSelectItems());
            }
        }
        return result;
    }
    
    /**
     * Judge has unqualified star select item.
     * 
     * @return star select item without owner
     */
    public boolean hasUnqualifiedStarSelectItem() {
        for (SelectItem each : items) {
            if (each instanceof StarSelectItem && !((StarSelectItem) each).getOwner().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get qualified star select items.
     *
     * @return qualified star select items
     */
    public Collection<StarSelectItem> getQualifiedStarSelectItems() {
        Collection<StarSelectItem> result = new LinkedList<>();
        for (SelectItem each : items) {
            if (each instanceof StarSelectItem && ((StarSelectItem) each).getOwner().isPresent()) {
                result.add((StarSelectItem) each);
            }
        }
        return result;
    }
    
    /**
     * Find star select item via table name or alias.
     *
     * @param tableNameOrAlias table name or alias
     * @return star select item via table name or alias
     */
    public Optional<StarSelectItem> findStarSelectItem(final String tableNameOrAlias) {
        Optional<Table> table = getTables().find(tableNameOrAlias);
        if (!table.isPresent()) {
            return Optional.absent();
        }
        for (SelectItem each : items) {
            if (!(each instanceof StarSelectItem)) {
                continue;
            }
            StarSelectItem starSelectItem = (StarSelectItem) each;
            if (starSelectItem.getOwner().isPresent() && getTables().find(starSelectItem.getOwner().get()).equals(table)) {
                return Optional.of(starSelectItem);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Adjust group by and order by sequence is same or not.
     *
     * @return group by and order by sequence is same or not
     */
    public boolean isSameGroupByAndOrderByItems() {
        return !getGroupByItems().isEmpty() && getGroupByItems().equals(getOrderByItems());
    }
    
    /**
     * Set index for select items.
     * 
     * @param columnLabelIndexMap map for column label and index
     */
    public void setIndexForItems(final Map<String, Integer> columnLabelIndexMap) {
        setIndexForAggregationItem(columnLabelIndexMap);
        setIndexForOrderItem(columnLabelIndexMap, orderByItems);
        setIndexForOrderItem(columnLabelIndexMap, groupByItems);
    }
    
    private void setIndexForAggregationItem(final Map<String, Integer> columnLabelIndexMap) {
        for (AggregationSelectItem each : getAggregationSelectItems()) {
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s, please add alias for aggregate selections", each));
            each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            for (AggregationSelectItem derived : each.getDerivedAggregationSelectItems()) {
                Preconditions.checkState(columnLabelIndexMap.containsKey(derived.getColumnLabel()), String.format("Can't find index: %s", derived));
                derived.setIndex(columnLabelIndexMap.get(derived.getColumnLabel()));
            }
        }
    }
    
    private void setIndexForOrderItem(final Map<String, Integer> columnLabelIndexMap, final List<OrderItem> orderItems) {
        for (OrderItem each : orderItems) {
            if (-1 != each.getIndex()) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel()), String.format("Can't find index: %s", each));
            if (columnLabelIndexMap.containsKey(each.getColumnLabel())) {
                each.setIndex(columnLabelIndexMap.get(each.getColumnLabel()));
            }
        }
    }
    
    /**
     * Set sub query statement.
     * 
     * @param subQueryStatement sub query statement
     */
    public void setSubQueryStatement(final SelectStatement subQueryStatement) {
        this.subQueryStatement = subQueryStatement;
        setParametersIndex(subQueryStatement.getParametersIndex());
    }
    
    /**
     * Adjust contains sub query statement or not.
     * 
     * @return contains sub query statement or not
     */
    public boolean containsSubQuery() {
        return null != subQueryStatement;
    }
    
    /**
     * Merge sub query statement if contains.
     * 
     * @return Select select statement
     */
    public SelectStatement mergeSubQueryStatement() {
        SelectStatement result = processLimitForSubQuery();
        processItems(result);
        processOrderByItems(result);
        result.setParametersIndex(getParametersIndex());
        return result;
    }
    
    private SelectStatement processLimitForSubQuery() {
        SelectStatement result = this;
        List<SQLToken> limitSQLTokens = getLimitTokens(result);
        Limit limit = result.getLimit();
        while (result.containsSubQuery()) {
            result = result.subQueryStatement;
            limitSQLTokens.addAll(getLimitTokens(result));
            if (null == result.getLimit()) {
                continue;
            }
            if (null == limit) {
                limit = result.getLimit();
            }
            if (null != result.getLimit().getRowCount()) {
                limit.setRowCount(result.getLimit().getRowCount());
            }
            if (null != result.getLimit().getOffset()) {
                limit.setOffset(result.getLimit().getOffset());
            }
        }
        resetLimitTokens(result, limitSQLTokens);
        result.setLimit(limit);
        return result;
    }
    
    private List<SQLToken> getLimitTokens(final SelectStatement selectStatement) {
        List<SQLToken> result = new LinkedList<>();
        for (SQLToken each : selectStatement.getSQLTokens()) {
            if (each instanceof RowCountToken || each instanceof OffsetToken) {
                result.add(each);
            }
        }
        return result;
    }
    
    private void resetLimitTokens(final SelectStatement selectStatement, final List<SQLToken> limitSQLTokens) {
        int count = 0;
        List<Integer> toBeRemovedIndexes = new LinkedList<>();
        List<SQLToken> sqlTokens = selectStatement.getSQLTokens();
        for (SQLToken each : sqlTokens) {
            if (each instanceof RowCountToken || each instanceof OffsetToken) {
                toBeRemovedIndexes.add(count);
            }
            count++;
        }
        for (int each : toBeRemovedIndexes) {
            sqlTokens.remove(each);
        }
        sqlTokens.addAll(limitSQLTokens);
    }
    
    private void processItems(final SelectStatement subQueryStatement) {
        if (!containStar) {
            subQueryStatement.getItems().clear();
            subQueryStatement.getItems().addAll(getItems());
        }
    }
    
    private void processOrderByItems(final SelectStatement subQueryStatement) {
        if (!containStar) {
            subQueryStatement.getOrderByItems().clear();
            subQueryStatement.getGroupByItems().clear();
        }
    }
}
