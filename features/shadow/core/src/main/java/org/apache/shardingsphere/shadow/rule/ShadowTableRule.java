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

package org.apache.shardingsphere.shadow.rule;

import lombok.Getter;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shadow table rule.
 */
@Getter
public final class ShadowTableRule {
    
    private final String name;
    
    private final Collection<String> logicDataSourceNames;
    
    private final Collection<String> hintShadowAlgorithmNames;
    
    private final Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> columnShadowAlgorithmNames;
    
    public ShadowTableRule(final String tableName, final Collection<String> logicDataSourceNames, final Collection<String> shadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        name = tableName;
        this.logicDataSourceNames = logicDataSourceNames;
        hintShadowAlgorithmNames = getHintShadowAlgorithmNames(shadowAlgorithmNames, shadowAlgorithms);
        columnShadowAlgorithmNames = getColumnShadowAlgorithmRules(shadowAlgorithmNames, shadowAlgorithms);
    }
    
    private Collection<String> getHintShadowAlgorithmNames(final Collection<String> shadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        return shadowAlgorithmNames.stream().filter(each -> shadowAlgorithms.get(each) instanceof HintShadowAlgorithm).collect(Collectors.toList());
    }
    
    private Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> getColumnShadowAlgorithmRules(final Collection<String> shadowAlgorithmNames,
                                                                                                        final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> result = new EnumMap<>(ShadowOperationType.class);
        for (String each : shadowAlgorithmNames) {
            ShadowAlgorithm shadowAlgorithm = shadowAlgorithms.get(each);
            if (shadowAlgorithm instanceof ColumnShadowAlgorithm) {
                ShadowOperationType operationType = ((ColumnShadowAlgorithm<?>) shadowAlgorithm).getShadowOperationType();
                result.computeIfAbsent(operationType, unused -> new LinkedList<>()).add(new ShadowAlgorithmNameRule(((ColumnShadowAlgorithm<?>) shadowAlgorithm).getShadowColumn(), each));
            }
        }
        return result;
    }
}
