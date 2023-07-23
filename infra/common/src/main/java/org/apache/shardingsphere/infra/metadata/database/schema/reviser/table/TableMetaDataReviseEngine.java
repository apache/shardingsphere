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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.column.ColumnReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.constraint.ConstraintReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Table meta data revise engine.
 *
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class TableMetaDataReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final DatabaseType databaseType;
    
    private final DataSource dataSource;
    
    private final MetaDataReviseEntry<T> reviseEntry;
    
    /**
     * Revise table meta data.
     *
     * @param originalMetaData original table meta data
     * @return revised table meta data
     */
    public TableMetaData revise(final TableMetaData originalMetaData) {
        Optional<? extends TableNameReviser<T>> tableNameReviser = reviseEntry.getTableNameReviser();
        String revisedTableName = tableNameReviser.map(optional -> optional.revise(originalMetaData.getName(), rule)).orElse(originalMetaData.getName());
        return new TableMetaData(revisedTableName, new ColumnReviseEngine<>(rule, databaseType, dataSource, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getColumns()),
                new IndexReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getIndexes()),
                new ConstraintReviseEngine<>(rule, reviseEntry).revise(originalMetaData.getName(), originalMetaData.getConstraints()));
    }
}
