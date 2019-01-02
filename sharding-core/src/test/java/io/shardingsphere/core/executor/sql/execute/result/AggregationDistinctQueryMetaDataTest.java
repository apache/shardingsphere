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
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

public class AggregationDistinctQueryMetaDataTest {
    
    private final Collection<AggregationDistinctSelectItem> aggregationDistinctSelectItems = new LinkedList<>();
    
    private final Multimap<String, Integer> columnLabelAndIndexMap = HashMultimap.create();
    
    @Before
    public void setUp() {
        AggregationDistinctSelectItem distinctCountSelectItem = new AggregationDistinctSelectItem(AggregationType.COUNT, "(DISTINCT order_id)", Optional.of("c"), "order_id");
        AggregationDistinctSelectItem distinctAvgSelectItem = new AggregationDistinctSelectItem(AggregationType.AVG, "(DISTINCT order_id)", Optional.of("a"), "order_id");
        aggregationDistinctSelectItems.add(distinctCountSelectItem);
        aggregationDistinctSelectItems.add(distinctAvgSelectItem);
        columnLabelAndIndexMap.put("c", 1);
        columnLabelAndIndexMap.put("a", 2);
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndexes() {
    }
    
    @Test
    public void assertGetAggregationDistinctColumnLabels() {
    }
    
    @Test
    public void assertGetAggregationType() {
    }
    
    @Test
    public void assertGetDerivedCountColumnIndexes() {
    }
    
    @Test
    public void assertGetDerivedSumColumnIndexes() {
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndex() {
    }
    
    @Test
    public void assertGetAggregationDistinctColumnIndex1() {
    }
    
    @Test
    public void assertGetAggregationDistinctColumnLabel() {
    }
}
