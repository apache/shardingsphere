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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.column;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Column revise engine.
 * 
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class ColumnReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final MetaDataReviseEntry<T> reviseEntry;
    
    /**
     * Revise column meta data.
     *
     * @param tableName table name
     * @param originalMetaDataList original column meta data list
     * @return revised column meta data
     */
    public Collection<ColumnMetaData> revise(final String tableName, final Collection<ColumnMetaData> originalMetaDataList) {
        Optional<? extends ColumnExistedReviser> existedReviser = reviseEntry.getColumnExistedReviser(rule, tableName);
        Optional<? extends ColumnNameReviser> nameReviser = reviseEntry.getColumnNameReviser(rule, tableName);
        Optional<? extends ColumnGeneratedReviser> generatedReviser = reviseEntry.getColumnGeneratedReviser(rule, tableName);
        Collection<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : originalMetaDataList) {
            if (existedReviser.isPresent() && !existedReviser.get().isExisted(each.getName())) {
                continue;
            }
            String name = nameReviser.isPresent() ? nameReviser.get().revise(each.getName()) : each.getName();
            boolean generated = generatedReviser.map(optional -> optional.revise(each)).orElseGet(each::isGenerated);
            result.add(
                    new ColumnMetaData(name, each.getDataType(), each.isPrimaryKey(), generated, each.getTypeName(), each.isCaseSensitive(), each.isVisible(), each.isUnsigned(), each.isNullable()));
        }
        return result;
    }
}
