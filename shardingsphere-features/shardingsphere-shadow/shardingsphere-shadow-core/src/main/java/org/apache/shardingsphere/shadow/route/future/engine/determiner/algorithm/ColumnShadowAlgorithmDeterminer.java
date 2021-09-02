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
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.PreciseColumnShadowValue;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowAlgorithmDeterminer;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Column shadow algorithm determiner.
 */
@RequiredArgsConstructor
public final class ColumnShadowAlgorithmDeterminer implements ShadowAlgorithmDeterminer {
    
    private final ColumnShadowAlgorithm<Comparable<?>> columnShadowAlgorithm;
    
    @Override
    public boolean isShadow(final InsertStatementContext insertStatementContext, final Collection<String> relatedShadowTables, final String tableName) {
        Collection<String> columnNames = insertStatementContext.getInsertColumnNames();
        List<List<Object>> groupedParameters = insertStatementContext.getGroupedParameters();
        Iterator<String> columnNamesIt = columnNames.iterator();
        Iterator<List<Object>> groupedParametersIt = groupedParameters.iterator();
        int count = Math.min(columnNames.size(), groupedParameters.size());
        for (int i = 0; i < count; i++) {
            Optional<Collection<PreciseColumnShadowValue<Comparable<?>>>> preciseColumnShadowValues = getPreciseColumnShadowValues(columnNamesIt.next(), groupedParametersIt.next(), tableName);
            if (preciseColumnShadowValues.isPresent()) {
                if (isPassColumn(preciseColumnShadowValues.get(), columnShadowAlgorithm, relatedShadowTables)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isPassColumn(final Collection<PreciseColumnShadowValue<Comparable<?>>> preciseColumnShadowValues, final ColumnShadowAlgorithm<Comparable<?>> columnShadowAlgorithm,
                                 final Collection<String> relatedShadowTables) {
        for (PreciseColumnShadowValue<Comparable<?>> each : preciseColumnShadowValues) {
            if (!columnShadowAlgorithm.isShadow(relatedShadowTables, each)) {
                return false;
            }
        }
        return true;
    }
    
    private Optional<Collection<PreciseColumnShadowValue<Comparable<?>>>> getPreciseColumnShadowValues(final String columnName, final List<Object> groupedParameter, final String tableName) {
        Collection<PreciseColumnShadowValue<Comparable<?>>> result = new LinkedList<>();
        for (Object each : groupedParameter) {
            if (!(each instanceof Comparable<?>)) {
                return Optional.empty();
            }
            result.add(new PreciseColumnShadowValue<>(tableName, ShadowOperationType.INSERT, columnName, (Comparable<?>) each));
        }
        return Optional.of(result);
    }
}
