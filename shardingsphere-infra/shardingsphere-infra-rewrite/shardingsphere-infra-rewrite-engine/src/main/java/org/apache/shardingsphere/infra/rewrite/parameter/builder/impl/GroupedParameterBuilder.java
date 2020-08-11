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

package org.apache.shardingsphere.infra.rewrite.parameter.builder.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Grouped parameter builder.
 */
public final class GroupedParameterBuilder implements ParameterBuilder {
    
    @Getter
    private final List<StandardParameterBuilder> parameterBuilders;
    
    @Getter
    private final StandardParameterBuilder genericParameterBuilder;
    
    @Setter
    private String derivedColumnName;
    
    public GroupedParameterBuilder(final List<List<Object>> groupedParameters, final List<Object> genericParameters) {
        parameterBuilders = new ArrayList<>(groupedParameters.size());
        for (List<Object> each : groupedParameters) {
            parameterBuilders.add(new StandardParameterBuilder(each));
        }
        genericParameterBuilder = new StandardParameterBuilder(genericParameters);
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>();
        for (int i = 0; i < parameterBuilders.size(); i++) {
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
        return parameterBuilders.get(count).getParameters();
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
     * Build broadcast parameters.
     * 
     * @return parameters
     */
    public List<Object> buildBroadcastParameters() {
        List<Object> genericParameters = genericParameterBuilder.getParameters();
        if (genericParameters.isEmpty()) {
            return getParameters();
        }
        List<Object> result = new LinkedList<>();
        result.addAll(getParameters());
        result.addAll(genericParameters);
        return result;
    }
}
