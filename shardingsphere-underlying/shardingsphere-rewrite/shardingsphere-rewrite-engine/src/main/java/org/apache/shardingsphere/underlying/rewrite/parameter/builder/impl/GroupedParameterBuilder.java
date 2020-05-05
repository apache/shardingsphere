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

package org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Grouped parameter builder.
 */
public final class GroupedParameterBuilder implements ParameterBuilder {
    
    @Getter
    private final List<StandardParameterBuilder> parameterBuilders;

    @Getter
    private final List<Object> originalOnDuplicateKeyParameters = new LinkedList<>();

    @Getter
    private final Map<Integer, Collection<Object>> addedIndexAndOnDuplicateKeyParameters = new TreeMap<>();

    private final Map<Integer, Object> replacedIndexAndOnDuplicateKeyParameters = new LinkedHashMap<>();

    @Setter
    private String derivedColumnName;
    
    public GroupedParameterBuilder(final List<List<Object>> groupedParameters, final List<Object> onDuplicateKeyUpdateParameters) {
        parameterBuilders = new ArrayList<>(groupedParameters.size());
        for (List<Object> each : groupedParameters) {
            parameterBuilders.add(new StandardParameterBuilder(each));
        }

        originalOnDuplicateKeyParameters.addAll(onDuplicateKeyUpdateParameters);
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>();
        for (int i = 0; i < parameterBuilders.size(); i++) {
            result.addAll(getParameters(i));
        }

        result.addAll(getOnDuplicateKeyParameters());

        return result;
    }

    /**
     * Get parameters.
     * 
     * @param count parameters group count
     * @return parameters
     */
    public List<Object> getParameters(final int count) {
        return parameterBuilders.get(count).getParameters();
    }

    private List<Object> getOnDuplicateKeyParameters() {
        List<Object> result = new LinkedList<>(originalOnDuplicateKeyParameters);
        for (Map.Entry<Integer, Object> entry : replacedIndexAndOnDuplicateKeyParameters.entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Integer, Collection<Object>> entry : ((TreeMap<Integer, Collection<Object>>) addedIndexAndOnDuplicateKeyParameters).descendingMap().entrySet()) {
            if (entry.getKey() > result.size()) {
                result.addAll(entry.getValue());
            } else {
                result.addAll(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get derived column name.
     * 
     * @return derived column name
     */
    public Optional<String> getDerivedColumnName() {
        return Optional.ofNullable(derivedColumnName);
    }

    /**
     * Add replaced OnDuplicateKeyUpdateParameter.
     *
     * @param index parameter index to be replaced
     * @param parameter parameter to be replaced
     */
    public void addReplacedIndexAndOnDuplicateKeyUpdateParameters(final int index, final Object parameter) {
        replacedIndexAndOnDuplicateKeyParameters.put(index, parameter);
    }
}
