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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Standard parameter builder.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@AllArgsConstructor
public final class StandardParameterBuilder implements ParameterBuilder {
    
    private final List<Object> originalParameters;
    
    private Integer whereClauseParameterStartIndex;
    
    @Getter
    private final Collection<Object> addedParameters = new LinkedList<>();
    
    @Getter
    private final Map<Integer, Object> replacedIndexAndParameters = new LinkedHashMap<>();
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>(originalParameters);
        for (Entry<Integer, Object> entry : replacedIndexAndParameters.entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        if (null == whereClauseParameterStartIndex) {
            result.addAll(addedParameters);
        } else {
            result.addAll(whereClauseParameterStartIndex, addedParameters);
        }
        return result;
    }
}
