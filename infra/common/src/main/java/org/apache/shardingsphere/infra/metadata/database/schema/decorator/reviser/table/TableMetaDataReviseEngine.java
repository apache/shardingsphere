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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.column.ColumnReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.constraint.ConstraintReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.constraint.ConstraintReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index.IndexReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Optional;

/**
 * Table meta data revise engine.
 *
 * @param <R> type of rule
 */
@RequiredArgsConstructor
public final class TableMetaDataReviseEngine<R extends ShardingSphereRule> {
    
    private final R rule;
    
    /**
     * Revise table meta data.
     * 
     * @param originalMetaData original table meta data
     * @param columnRevisers column revisers
     * @param indexRevisers index revisers
     * @param constraintRevisers constraint revisers
     * @param <T> type of table rule
     * @return revised table meta data
     */
    public <T> TableMetaData revise(final TableMetaData originalMetaData,
                                    final Collection<ColumnReviser> columnRevisers, final Collection<IndexReviser> indexRevisers, final Collection<ConstraintReviser> constraintRevisers) {
        @SuppressWarnings("unchecked") TableNameReviser<R, T> tableNameReviser = TypedSPILoader.getService(TableNameReviser.class, rule.getClass().getSimpleName());
        Optional<T> tableRule = tableNameReviser.findTableRule(originalMetaData.getName(), rule);
        String revisedTableName = tableRule.isPresent() ? tableNameReviser.revise(originalMetaData.getName(), tableRule.get()) : originalMetaData.getName();
        return new TableMetaData(revisedTableName, new ColumnReviseEngine().revise(originalMetaData.getColumns(), columnRevisers),
                new IndexReviseEngine().revise(revisedTableName, originalMetaData.getIndexes(), indexRevisers),
                new ConstraintReviseEngine().revise(revisedTableName, originalMetaData.getConstrains(), constraintRevisers));
    }
}
