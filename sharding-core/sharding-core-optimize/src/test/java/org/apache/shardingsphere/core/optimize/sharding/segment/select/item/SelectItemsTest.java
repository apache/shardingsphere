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

import org.apache.shardingsphere.core.parse.core.constant.AggregationType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectItemsTest {

    @Test
    public void assertUnqualifiedShorthandItemWithEmptyItems() {
        SelectItems selectItems = new SelectItems(Collections.<SelectItem>emptySet(), true, 1);
        assertFalse(selectItems.isUnqualifiedShorthandItem());
    }

    @Test
    public void assertUnqualifiedShorthandItemWithWrongSelectItem() {
        SelectItems selectItems = new SelectItems(Collections.singleton((SelectItem) getCommonSelectItem()), true, 1);
        assertFalse(selectItems.isUnqualifiedShorthandItem());
    }

    @Test
    public void assertUnqualifiedShorthandItemWithWrongShortSelectItem() {
        SelectItems selectItems = new SelectItems(Collections.singleton((SelectItem) getShorthandSelectItem()), true, 1);
        assertFalse(selectItems.isUnqualifiedShorthandItem());
    }

    @Test
    public void assertUnqualifiedShorthandItem() {
        SelectItem selectItem = new ShorthandSelectItem(null);
        SelectItems selectItems = new SelectItems(Collections.singleton(selectItem), true, 1);
        assertTrue(selectItems.isUnqualifiedShorthandItem());
    }

    @Test
    public void assertFindAliasWithOutAlias() {
        SelectItems selectItems = new SelectItems(Collections.<SelectItem>emptyList(), true, 1);
        assertFalse(selectItems.findAlias("").isPresent());
    }

    @Test
    public void assertFindAlias() {
        SelectItem selectItem = getCommonSelectItemWithAlias();
        SelectItems selectItems = new SelectItems(Collections.singleton(selectItem), true, 1);
        assertTrue(selectItems.findAlias(selectItem.getExpression()).isPresent());
    }

    @Test
    public void assertGetAggregationSelectItems() {
        SelectItem aggregationSelectItem = getAggregationSelectItem();
        List<AggregationSelectItem> items = new SelectItems(Arrays.asList(aggregationSelectItem, getCommonSelectItem()), true, 1).getAggregationSelectItems();
        assertTrue(items.contains(aggregationSelectItem));
        assertEquals(items.size(), 1);
    }

    @Test
    public void assertGetAggregationDistinctSelectItems() {
        SelectItem aggregationDistinctSelectItem = getAggregationDistinctSelectItem();
        List<AggregationDistinctSelectItem> items = new SelectItems(Arrays.asList(aggregationDistinctSelectItem, getCommonSelectItem()), true, 1).getAggregationDistinctSelectItems();
        assertTrue(items.contains(aggregationDistinctSelectItem));
        assertEquals(items.size(), 1);
    }

    @Test
    public void assertGetColumnLabelWithShorthandSelectItem() {
        SelectItem selectItem = getShorthandSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.isEmpty());
    }

    @Test
    public void assertGetColumnLabelsWithCommonSelectItem() {
        SelectItem selectItem = getCommonSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getColumnLabel()));
    }

    @Test
    public void assertGetColumnLabelsWithCommonSelectItemAlias() {
        SelectItem selectItem = getCommonSelectItemWithAlias();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getAlias().or("")));
    }

    @Test
    public void assertGetColumnLabelsWithExpressionSelectItem() {
        SelectItem selectItem = getExpressionSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getColumnLabel()));
    }

    @Test
    public void assertGetColumnLabelsWithExpressionSelectItemAlias() {
        SelectItem selectItem = getExpressionSelectItemWithAlias();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getAlias().or("")));
    }

    @Test
    public void assertGetColumnLabelsWithDerivedSelectItem() {
        SelectItem selectItem = getDerivedSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getColumnLabel()));
    }

    @Test
    public void assertGetColumnLabelsWithDerivedSelectItemAlias() {
        SelectItem selectItem = getDerivedSelectItemWithAlias();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getAlias().or("")));
    }

    @Test
    public void assertGetColumnLabelsWithAggregationSelectItem() {
        SelectItem selectItem = getAggregationSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getColumnLabel()));
    }

    @Test
    public void assertGetColumnLabelsWithAggregationDistinctSelectItem() {
        SelectItem selectItem = getAggregationDistinctSelectItem();
        List<String> columnLabels = new SelectItems(Collections.singletonList(selectItem), true, 1).getColumnLabels();
        assertTrue(columnLabels.contains(selectItem.getColumnLabel()));
    }

    private ShorthandSelectItem getShorthandSelectItem() {
        return new ShorthandSelectItem("table");
    }

    private CommonSelectItem getCommonSelectItem() {
        return new CommonSelectItem("table", "name", null);
    }

    private CommonSelectItem getCommonSelectItemWithAlias() {
        return new CommonSelectItem("table", "name", "n");
    }

    private ExpressionSelectItem getExpressionSelectItem() {
        return new ExpressionSelectItem("table.name", null);
    }

    private ExpressionSelectItem getExpressionSelectItemWithAlias() {
        return new ExpressionSelectItem("table.name", "n");
    }

    private DerivedSelectItem getDerivedSelectItem() {
        return new DerivedSelectItem("table.name", null);
    }

    private DerivedSelectItem getDerivedSelectItemWithAlias() {
        return new DerivedSelectItem("table.name", "n");
    }

    private AggregationSelectItem getAggregationSelectItem() {
        return new AggregationSelectItem(AggregationType.COUNT, "(column)", "c");
    }

    private AggregationDistinctSelectItem getAggregationDistinctSelectItem() {
        return new AggregationDistinctSelectItem(AggregationType.COUNT, "(DISTINCT column)", "c", "column");
    }

}
