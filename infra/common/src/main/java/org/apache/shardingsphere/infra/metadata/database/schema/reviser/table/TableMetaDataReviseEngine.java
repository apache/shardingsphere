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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.column.ColumnReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint.ConstraintReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Table meta data revise engine.
 *
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class TableMetaDataReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final MetaDataReviseEntry<T> reviseEntry;
    
    /**
     * Revise table meta data.
     *
     * @param originalMetaData original table meta data
     * @return revised table meta data
     */
    public TableMetaData revise(final TableMetaData originalMetaData) {
        return revise(originalMetaData, Collections.singleton(originalMetaData));
    }
    
    /**
     * Revise table meta data.
     *
     * @param originalMetaData original table meta data
     * @param originalMetaDataList original table meta data list
     * @return revised table meta data
     */
    public TableMetaData revise(final TableMetaData originalMetaData, final Collection<TableMetaData> originalMetaDataList) {
        return revise(originalMetaData, originalMetaDataList, Collections.emptyList());
    }
    
    /**
     * Revise table meta data.
     *
     * @param originalMetaData original table meta data
     * @param originalMetaDataList original table meta data list
     * @param indexNameRecoveryCandidateTables index name recovery candidate tables
     * @return revised table meta data
     */
    public TableMetaData revise(final TableMetaData originalMetaData, final Collection<TableMetaData> originalMetaDataList,
                                final Collection<TableMetaData> indexNameRecoveryCandidateTables) {
        String storageUnitName = originalMetaData.getStorageUnitName();
        Optional<? extends TableMetaDataRevisionContext<T>> revisionContext =
                reviseEntry.createTableMetaDataRevisionContext(rule, originalMetaData.getName(), storageUnitName);
        return revisionContext.isPresent()
                ? revise(originalMetaData, originalMetaDataList, indexNameRecoveryCandidateTables, revisionContext.get())
                : reviseWithoutRevisionContext(originalMetaData, originalMetaDataList, indexNameRecoveryCandidateTables, storageUnitName);
    }
    
    private TableMetaData revise(final TableMetaData originalMetaData, final Collection<TableMetaData> originalMetaDataList,
                                 final Collection<TableMetaData> indexNameRecoveryCandidateTables, final TableMetaDataRevisionContext<T> revisionContext) {
        TableMetaData result = new TableMetaData(revisionContext.reviseTableName(originalMetaData.getName()),
                new ColumnReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getColumns(), revisionContext.getColumnGeneratedReviser()),
                new IndexReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getIndexes(), originalMetaDataList,
                        indexNameRecoveryCandidateTables, revisionContext.getIndexReviser()),
                new ConstraintReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getConstraints(), revisionContext.getConstraintReviser()),
                originalMetaData.getType());
        result.setStorageUnitName(originalMetaData.getStorageUnitName());
        return result;
    }
    
    private TableMetaData reviseWithoutRevisionContext(final TableMetaData originalMetaData, final Collection<TableMetaData> originalMetaDataList,
                                                       final Collection<TableMetaData> indexNameRecoveryCandidateTables, final String storageUnitName) {
        Optional<? extends TableNameReviser<T>> tableNameReviser = reviseEntry.getTableNameReviser();
        String revisedTableName = tableNameReviser.map(optional -> optional.revise(originalMetaData.getName(), rule, storageUnitName)).orElse(originalMetaData.getName());
        TableMetaData result = new TableMetaData(revisedTableName,
                new ColumnReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), storageUnitName, originalMetaData.getColumns()),
                new IndexReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), storageUnitName, originalMetaData.getIndexes(), originalMetaDataList,
                        indexNameRecoveryCandidateTables),
                new ConstraintReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), storageUnitName, originalMetaData.getConstraints()), originalMetaData.getType());
        result.setStorageUnitName(storageUnitName);
        return result;
    }
}
