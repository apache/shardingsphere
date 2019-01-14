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

package io.shardingsphere.core.executor.sql.execute.result;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AggregationDistinctQueryMetaDataTest {

    private AggregationDistinctQueryMetaData distinctQueryMetaData; 
    
    @Before
    public void setUp() {
        Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems = new LinkedList<>();
        AggregationDistinctSelectItem distinctCountSelectItem = new AggregationDistinctSelectItem(AggregationType.COUNT, "(DISTINCT order_id)", Optional.of("c"), "order_id");
        AggregationDistinctSelectItem distinctAvgSelectItem = new AggregationDistinctSelectItem(AggregationType.AVG, "(DISTINCT order_id)", Optional.of("a"), "order_id");
        distinctAvgSelectItem.getDerivedAggregationSelectItems().add(new AggregationSelectItem(AggregationType.COUNT, "(DISTINCT order_id)", Optional.of("AVG_DERIVED_COUNT_0")));
        distinctAvgSelectItem.getDerivedAggregationSelectItems().add(new AggregationSelectItem(AggregationType.SUM, "(DISTINCT order_id)", Optional.of("AVG_DERIVED_SUM_0")));
        aggregationDistinctSelectItems.add(distinctCountSelectItem);
        aggregationDistinctSelectItems.add(distinctAvgSelectItem);
        Multimap<String, Integer> columnLabelAndIndexMap = HashMultimap.create();
        columnLabelAndIndexMap.put("c", 1);
        columnLabelAndIndexMap.put("a", 2);
        distinctQueryMetaData = new AggregationDistinctQueryMetaData(aggregationDistinctSelectItems, columnLabelAndIndexMap);
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndexes() {
        Collection<Integer> actual = distinctQueryMetaData.getAggregationDistinctColumnIndexes();
        Collection<Integer> expected = Arrays.asList(1, 2);
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next(), is(expected.iterator().next()));
    }
    
    @Test
    public void assertGetAggregationDistinctColumnLabels() {
        Collection<String> actual = distinctQueryMetaData.getAggregationDistinctColumnLabels();
        Collection<String> expected = Arrays.asList("c", "a");
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next(), is(expected.iterator().next()));
    }
    
    @Test
    public void assertGetAggregationType() {
        AggregationType actual = distinctQueryMetaData.getAggregationType(2);
        assertThat(actual, is(AggregationType.AVG));
    }
    
    @Test
    public void assertGetDerivedCountColumnIndexes() {
        Collection<Integer> actual = distinctQueryMetaData.getDerivedCountColumnIndexes();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(3));
    }
    
    @Test
    public void assertGetDerivedSumColumnIndexes() {
        Collection<Integer> actual = distinctQueryMetaData.getDerivedSumColumnIndexes();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(4));
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndexByColumnLabel() {
        int actual = distinctQueryMetaData.getAggregationDistinctColumnIndex("a");
        assertThat(actual, is(2));
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndexBySumIndex() {
        int actual = distinctQueryMetaData.getAggregationDistinctColumnIndex(4);
        assertThat(actual, is(2));
    }
    
    @Test
    public void assertGetAggregationDistinctColumnLabel() { 
        String actual = distinctQueryMetaData.getAggregationDistinctColumnLabel(1);
        assertThat(actual, is("c"));
    }
}
