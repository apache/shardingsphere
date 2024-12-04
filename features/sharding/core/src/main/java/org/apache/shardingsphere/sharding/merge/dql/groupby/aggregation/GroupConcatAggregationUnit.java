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

package org.apache.shardingsphere.sharding.merge.dql.groupby.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Group concat aggregation unit.
 */
public final class GroupConcatAggregationUnit implements AggregationUnit {
    
    private static final String DEFAULT_SEPARATOR = ",";
    
    private final Collection<String> values = new ArrayList<>();
    
    private final String separator;
    
    public GroupConcatAggregationUnit(final String separator) {
        this.separator = null == separator ? DEFAULT_SEPARATOR : separator;
    }
    
    @Override
    public void merge(final List<Comparable<?>> values) {
        if (null == values || null == values.get(0)) {
            return;
        }
        this.values.add(String.valueOf(values.get(0)));
    }
    
    @Override
    public Comparable<?> getResult() {
        return String.join(separator, values);
    }
}
