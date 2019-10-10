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

package org.apache.shardingsphere.core.rewrite.parameter.builder.impl;

import lombok.Getter;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Grouped Parameter builder.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class GroupedParameterBuilder implements ParameterBuilder {
    
    private final List<List<Object>> groupedParameters;
    
    @Getter
    private final List<Map<Integer, Object>> addedIndexAndParameterGroups;
    
    @Getter
    private final List<Map<Integer, Object>> replacedIndexAndParameterGroups;
    
    public GroupedParameterBuilder(final List<List<Object>> groupedParameters) {
        this.groupedParameters = groupedParameters;
        addedIndexAndParameterGroups = createAdditionalParameterGroups();
        replacedIndexAndParameterGroups = createAdditionalParameterGroups();
    }
    
    private List<Map<Integer, Object>> createAdditionalParameterGroups() {
        List<Map<Integer, Object>> result = new ArrayList<>(groupedParameters.size());
        for (int i = 0; i < groupedParameters.size(); i++) {
            result.add(new HashMap<Integer, Object>());
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>();
        for (int i = 0; i < groupedParameters.size(); i++) {
            result.addAll(getParameters(i));
        }
        return result;
    }
    
    /**
     * Get parameters.
     * 
     * @param count parameters group count
     * @return parameters
     */
    public List<Object> getParameters(final int count) {
        List<Object> result = new LinkedList<>();
        result.addAll(groupedParameters.get(count));
        for (Entry<Integer, Object> entry : replacedIndexAndParameterGroups.get(count).entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        for (Entry<Integer, Object> entry : addedIndexAndParameterGroups.get(count).entrySet()) {
            int index = entry.getKey();
            if (index < result.size()) {
                result.add(index, entry.getValue());
            } else {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
