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

import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class DistinctGroupConcatAggregationUnit implements AggregationUnit {
    
    private final Collection<String> values = new HashSet<>();
    
    private final static String DEFAULT_SEPARATOR = ",";
    
    private final String separator;
    
    public DistinctGroupConcatAggregationUnit(String separator) {
        this.separator = null == separator ? DEFAULT_SEPARATOR : separator;
    }
    
    @Override
    public void merge(List<Comparable<?>> values) {
        if (null == values || null == values.get(0)) {
            return;
        }
        this.values.addAll(Arrays.asList(values.get(0).toString().split(separator)));
    }
    
    @Override
    public Comparable<?> getResult() {
        return String.join(separator, values);
    }
}
