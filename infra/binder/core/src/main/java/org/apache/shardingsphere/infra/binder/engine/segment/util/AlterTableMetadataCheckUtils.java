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

package org.apache.shardingsphere.infra.binder.engine.segment.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateColumnException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.IndexNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;

import java.util.Collection;

/**
 * Alter table metadata checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AlterTableMetadataCheckUtils {
    
    /**
     * Check alter table.
     *
     * @param alterTableStatement alter table statement
     * @param table ShardingSphere table
     */
    public static void checkAlterTable(final AlterTableStatement alterTableStatement, final ShardingSphereTable table) {
        validateAddColumns(table, alterTableStatement.getAddColumnDefinitions());
        validateModifyColumns(table, alterTableStatement.getModifyColumnDefinitions());
        validateChangeColumns(table, alterTableStatement.getChangeColumnDefinitions());
        validateRenameColumns(table, alterTableStatement.getRenameColumnDefinitions());
        validateDropColumns(table, alterTableStatement.getDropColumnDefinitions());
        validateAddIndexes(table, alterTableStatement.getAddConstraintDefinitions());
        validateDropIndexes(table, alterTableStatement.getDropIndexDefinitions());
        validateRenameIndexes(table, alterTableStatement.getRenameIndexDefinitions());
    }
    
    private static void validateAddColumns(final ShardingSphereTable table, final Collection<AddColumnDefinitionSegment> addColumns) {
        for (AddColumnDefinitionSegment each : addColumns) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                String addColumnName = columnDefinition.getColumnName().getIdentifier().getValue();
                ShardingSpherePreconditions.checkState(!containsColumn(table, addColumnName), () -> new DuplicateColumnException(addColumnName));
            }
        }
    }
    
    private static void validateModifyColumns(final ShardingSphereTable table, final Collection<ModifyColumnDefinitionSegment> modifyColumns) {
        for (ModifyColumnDefinitionSegment each : modifyColumns) {
            String columnName = each.getColumnDefinition().getColumnName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(containsColumn(table, columnName), () -> new ColumnNotFoundException(columnName, table.getName()));
        }
    }
    
    private static void validateChangeColumns(final ShardingSphereTable table, final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions) {
        for (ChangeColumnDefinitionSegment each : changeColumnDefinitions) {
            String newColumnName = each.getColumnDefinition().getColumnName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!containsColumn(table, newColumnName), () -> new DuplicateColumnException(newColumnName));
            String oldColumnName = each.getPreviousColumn().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(containsColumn(table, oldColumnName), () -> new ColumnNotFoundException(oldColumnName, table.getName()));
        }
    }
    
    private static void validateRenameColumns(final ShardingSphereTable table, final Collection<RenameColumnSegment> renameColumns) {
        for (RenameColumnSegment each : renameColumns) {
            String newColumnName = each.getColumnName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!containsColumn(table, newColumnName), () -> new DuplicateColumnException(newColumnName));
            String oldColumnName = each.getOldColumnName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(containsColumn(table, oldColumnName), () -> new ColumnNotFoundException(oldColumnName, table.getName()));
        }
    }
    
    private static void validateDropColumns(final ShardingSphereTable table, final Collection<DropColumnDefinitionSegment> dropColumns) {
        for (DropColumnDefinitionSegment each : dropColumns) {
            for (ColumnSegment column : each.getColumns()) {
                String columnName = column.getIdentifier().getValue();
                ShardingSpherePreconditions.checkState(containsColumn(table, columnName), () -> new ColumnNotFoundException(columnName, table.getName()));
            }
        }
    }
    
    private static void validateAddIndexes(final ShardingSphereTable table, final Collection<AddConstraintDefinitionSegment> addConstraints) {
        for (AddConstraintDefinitionSegment each : addConstraints) {
            String indexName = each.getConstraintDefinition().getIndexName().map(optional -> optional.getIndexName().getIdentifier().getValue()).orElse("");
            if (indexName.isEmpty()) {
                continue;
            }
            ShardingSpherePreconditions.checkState(!containsIndex(table, indexName), () -> new DuplicateIndexException(indexName));
        }
    }
    
    private static void validateDropIndexes(final ShardingSphereTable table, final Collection<DropIndexDefinitionSegment> dropIndexes) {
        for (DropIndexDefinitionSegment each : dropIndexes) {
            String indexName = each.getIndexSegment().getIndexName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(containsIndex(table, indexName), () -> new IndexNotFoundException(indexName));
        }
    }
    
    private static void validateRenameIndexes(final ShardingSphereTable table, final Collection<RenameIndexDefinitionSegment> renameIndexes) {
        for (RenameIndexDefinitionSegment each : renameIndexes) {
            String oldIndexName = each.getIndexSegment().getIndexName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(containsIndex(table, oldIndexName), () -> new IndexNotFoundException(oldIndexName));
            String newIndexName = each.getRenameIndexSegment().getIndexName().getIdentifier().getValue();
            ShardingSpherePreconditions.checkState(!containsIndex(table, newIndexName), () -> new DuplicateIndexException(newIndexName));
        }
    }
    
    private static boolean containsColumn(final ShardingSphereTable table, final String columnName) {
        return table.containsColumn(columnName);
    }
    
    private static boolean containsIndex(final ShardingSphereTable table, final String indexName) {
        return table.containsIndex(indexName);
    }
}
