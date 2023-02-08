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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Column revise engine.
 * 
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class ColumnReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final DatabaseType databaseType;
    
    private final DataSource dataSource;
    
    /**
     * Revise column meta data.
     * 
     * @param tableName table name
     * @param originalMetaDataList original column meta data list
     * @return revised column meta data
     */
    public Collection<ColumnMetaData> revise(final String tableName, final Collection<ColumnMetaData> originalMetaDataList) {
        String type = rule.getClass().getSimpleName();
        @SuppressWarnings("rawtypes")
        Optional<ColumnNameReviser> nameReviser = TypedSPILoader.findService(ColumnNameReviser.class, type);
        @SuppressWarnings("rawtypes")
        Optional<ColumnDataTypeReviser> dataTypeReviser = TypedSPILoader.findService(ColumnDataTypeReviser.class, type);
        @SuppressWarnings("rawtypes")
        Optional<ColumnGeneratedReviser> generatedReviser = TypedSPILoader.findService(ColumnGeneratedReviser.class, type);
        Collection<ColumnMetaData> result = new LinkedHashSet<>();
        for (ColumnMetaData each : originalMetaDataList) {
            @SuppressWarnings("unchecked")
            Optional<String> name = nameReviser.isPresent() ? nameReviser.get().revise(each.getName(), tableName, rule) : Optional.of(each.getName());
            if (!name.isPresent()) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Optional<Integer> dataType = dataTypeReviser.isPresent() ? dataTypeReviser.get().revise(each.getName(), tableName, rule, databaseType, dataSource) : Optional.empty();
            @SuppressWarnings("unchecked")
            Optional<Boolean> generated = generatedReviser.map(optional -> optional.revise(each, rule));
            result.add(new ColumnMetaData(name.get(),
                    dataType.orElseGet(each::getDataType), each.isPrimaryKey(), generated.orElse(each.isGenerated()), each.isCaseSensitive(), each.isVisible(), each.isUnsigned()));
        }
        return result;
    }
}
