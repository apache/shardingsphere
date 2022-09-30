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

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shadow table rule.
 */
@Getter
public final class ShadowTableRule {
    
    private final String tableName;
    
    private final Collection<String> shadowDataSources;
    
    private final Collection<String> hintShadowAlgorithmNames;
    
    private final Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> columnShadowAlgorithmNames;
    
    public ShadowTableRule(final String tableName, final Collection<String> shadowDataSources, final Collection<String> shadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        this.tableName = tableName;
        this.shadowDataSources = shadowDataSources;
        this.hintShadowAlgorithmNames = initHintShadowAlgorithmNames(shadowAlgorithmNames, shadowAlgorithms);
        this.columnShadowAlgorithmNames = initColumnShadowAlgorithmNames(shadowAlgorithmNames, shadowAlgorithms);
    }
    
    private Collection<String> initHintShadowAlgorithmNames(final Collection<String> shadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        return shadowAlgorithmNames.stream().filter(each -> shadowAlgorithms.get(each) instanceof HintShadowAlgorithm).collect(Collectors.toList());
    }
    
    private Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> initColumnShadowAlgorithmNames(final Collection<String> shadowAlgorithmNames,
                                                                                                         final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> result = new EnumMap<>(ShadowOperationType.class);
        shadowAlgorithmNames.forEach(each -> {
            ShadowAlgorithm shadowAlgorithm = shadowAlgorithms.get(each);
            if (shadowAlgorithm instanceof ColumnShadowAlgorithm) {
                String operation = shadowAlgorithm.getProps().getProperty("operation");
                String shadowColumnName = shadowAlgorithm.getProps().getProperty("column");
                ShadowOperationType.contains(operation).ifPresent(optional -> initShadowAlgorithmNames(optional, each, shadowColumnName, result));
            }
        });
        return result;
    }
    
    private void initShadowAlgorithmNames(final ShadowOperationType operationType, final String algorithmName, final String shadowColumnName,
                                          final Map<ShadowOperationType, Collection<ShadowAlgorithmNameRule>> columnShadowAlgorithmNames) {
        Collection<ShadowAlgorithmNameRule> shadowAlgorithmNameRules = columnShadowAlgorithmNames.get(operationType);
        Preconditions.checkState(null == shadowAlgorithmNameRules, "Column shadow algorithm `%s` operation only supports one column mapping in shadow table `%s`", operationType.name(), tableName);
        columnShadowAlgorithmNames.put(operationType, Collections.singletonList(new ShadowAlgorithmNameRule(shadowColumnName, algorithmName)));
    }
}
