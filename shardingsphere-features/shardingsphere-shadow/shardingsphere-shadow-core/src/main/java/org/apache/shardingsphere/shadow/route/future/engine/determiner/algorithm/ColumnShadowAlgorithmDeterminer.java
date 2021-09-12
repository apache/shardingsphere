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

package org.apache.shardingsphere.shadow.route.future.engine.determiner.algorithm;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Column shadow algorithm determiner.
 */
@RequiredArgsConstructor
public final class ColumnShadowAlgorithmDeterminer implements ShadowAlgorithmDeterminer {
    
    private final ColumnShadowAlgorithm<Comparable<?>> columnShadowAlgorithm;
    
    @Override
    public boolean isShadow(final ShadowDetermineCondition shadowDetermineCondition, final ShadowRule shadowRule, final String tableName) {
        Optional<Map<String, Collection<Comparable<?>>>> columnValuesMappings = shadowDetermineCondition.getColumnValuesMappings();
        if (columnValuesMappings.isPresent()) {
            for (Map.Entry<String, Collection<Comparable<?>>> entry : columnValuesMappings.get().entrySet()) {
                if (isShadowColumn(createColumnShadowValues(entry.getKey(), entry.getValue(), tableName, shadowDetermineCondition.getShadowOperationType()), shadowRule.getAllShadowTableNames())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isShadowColumn(final Collection<PreciseColumnShadowValue<Comparable<?>>> preciseColumnShadowValues, final Collection<String> relatedShadowTables) {
        for (PreciseColumnShadowValue<Comparable<?>> each : preciseColumnShadowValues) {
            if (!columnShadowAlgorithm.isShadow(relatedShadowTables, each)) {
                return false;
            }
        }
        return true;
    }
    
    private Collection<PreciseColumnShadowValue<Comparable<?>>> createColumnShadowValues(final String columnName, final Collection<Comparable<?>> columnValues, final String tableName,
                                                                                         final ShadowOperationType operationType) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        for (Comparable<?> each : columnValues) {
            result.add(new PreciseColumnShadowValue<>(tableName, operationType, columnName, each));
        }
        return result;
    }
}
