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

import org.apache.shardingsphere.infra.exception.kernel.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateColumnException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.DuplicateIndexException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.IndexNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterTableMetadataCheckUtilsTest {
    
    private ShardingSphereTable shardingSphereTable;
    
    private AlterTableStatement alterTableStatement;
    
    @BeforeEach
    void setUp() {
        shardingSphereTable = mock(ShardingSphereTable.class);
        alterTableStatement = mock(AlterTableStatement.class);
    }
    
    @Test
    void assertValidateAddColumns() {
        Collection<AddColumnDefinitionSegment> addColumns = createAddColumnDefinitions("new_col");
        when(alterTableStatement.getAddColumnDefinitions()).thenReturn(addColumns);
        when(shardingSphereTable.containsColumn("new_col")).thenReturn(false);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertAddColumnsFailed() {
        Collection<AddColumnDefinitionSegment> addColumns = createAddColumnDefinitions("new_col");
        when(alterTableStatement.getAddColumnDefinitions()).thenReturn(addColumns);
        when(shardingSphereTable.containsColumn("new_col")).thenReturn(true);
        assertThrows(DuplicateColumnException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateModifyColumnsFailed() {
        Collection<ModifyColumnDefinitionSegment> modifyColumns = createModifyColumnDefinitions("col1");
        when(alterTableStatement.getModifyColumnDefinitions()).thenReturn(modifyColumns);
        when(shardingSphereTable.containsColumn("col1")).thenReturn(false);
        assertThrows(ColumnNotFoundException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateModifyColumns() {
        Collection<ModifyColumnDefinitionSegment> modifyColumns = createModifyColumnDefinitions("col1");
        when(alterTableStatement.getModifyColumnDefinitions()).thenReturn(modifyColumns);
        when(shardingSphereTable.containsColumn("col1")).thenReturn(true);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateRenameColumnsFailed() {
        Collection<RenameColumnSegment> renameColumns = createRenameColumnSegments("old_col", "new_col");
        when(alterTableStatement.getRenameColumnDefinitions()).thenReturn(renameColumns);
        when(shardingSphereTable.containsColumn("new_col")).thenReturn(true);
        when(shardingSphereTable.containsColumn("old_col")).thenReturn(true);
        assertThrows(DuplicateColumnException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateRenameColumns() {
        Collection<RenameColumnSegment> renameColumns = createRenameColumnSegments("old_col", "new_col");
        when(alterTableStatement.getRenameColumnDefinitions()).thenReturn(renameColumns);
        when(shardingSphereTable.containsColumn("new_col")).thenReturn(false);
        when(shardingSphereTable.containsColumn("old_col")).thenReturn(true);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateDropColumnsFailed() {
        Collection<DropColumnDefinitionSegment> dropColumns = createDropColumnDefinitions("col_to_drop");
        when(alterTableStatement.getDropColumnDefinitions()).thenReturn(dropColumns);
        when(shardingSphereTable.containsColumn("col_to_drop")).thenReturn(false);
        assertThrows(ColumnNotFoundException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateDropColumns() {
        Collection<DropColumnDefinitionSegment> dropColumns = createDropColumnDefinitions("col_to_drop");
        when(alterTableStatement.getDropColumnDefinitions()).thenReturn(dropColumns);
        when(shardingSphereTable.containsColumn("col_to_drop")).thenReturn(true);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateAddIndexesFailed() {
        Collection<AddConstraintDefinitionSegment> addConstraints = createAddConstraintDefinitions("idx_new");
        when(alterTableStatement.getAddConstraintDefinitions()).thenReturn(addConstraints);
        when(shardingSphereTable.containsIndex("idx_new")).thenReturn(true);
        assertThrows(DuplicateIndexException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateAddIndexes() {
        Collection<AddConstraintDefinitionSegment> addConstraints = createAddConstraintDefinitions("idx_new");
        when(alterTableStatement.getAddConstraintDefinitions()).thenReturn(addConstraints);
        when(shardingSphereTable.containsIndex("idx_new")).thenReturn(false);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateDropIndexesFailed() {
        Collection<DropIndexDefinitionSegment> dropIndexes = createDropIndexDefinitions("idx_old");
        when(alterTableStatement.getDropIndexDefinitions()).thenReturn(dropIndexes);
        when(shardingSphereTable.containsIndex("idx_old")).thenReturn(false);
        assertThrows(IndexNotFoundException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateDropIndexes() {
        Collection<DropIndexDefinitionSegment> dropIndexes = createDropIndexDefinitions("idx_old");
        when(alterTableStatement.getDropIndexDefinitions()).thenReturn(dropIndexes);
        when(shardingSphereTable.containsIndex("idx_old")).thenReturn(true);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateRenameIndexesFailed() {
        Collection<RenameIndexDefinitionSegment> renameIndexes = createRenameIndexDefinitions("old_idx", "new_idx");
        when(alterTableStatement.getRenameIndexDefinitions()).thenReturn(renameIndexes);
        when(shardingSphereTable.containsIndex("old_idx")).thenReturn(false);
        when(shardingSphereTable.containsIndex("new_idx")).thenReturn(false);
        assertThrows(IndexNotFoundException.class, () -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    @Test
    void assertValidateRenameIndexes() {
        Collection<RenameIndexDefinitionSegment> renameIndexes = createRenameIndexDefinitions("old_idx", "new_idx");
        when(alterTableStatement.getRenameIndexDefinitions()).thenReturn(renameIndexes);
        when(shardingSphereTable.containsIndex("old_idx")).thenReturn(true);
        when(shardingSphereTable.containsIndex("new_idx")).thenReturn(false);
        assertDoesNotThrow(() -> AlterTableMetadataCheckUtils.checkAlterTable(alterTableStatement, shardingSphereTable));
    }
    
    private Collection<AddColumnDefinitionSegment> createAddColumnDefinitions(final String... columnNames) {
        Collection<AddColumnDefinitionSegment> result = new ArrayList<>(columnNames.length);
        for (String each : columnNames) {
            ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
            when(columnDefinition.getColumnName()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue(each)));
            AddColumnDefinitionSegment segment = mock(AddColumnDefinitionSegment.class);
            when(segment.getColumnDefinitions()).thenReturn(Collections.singletonList(columnDefinition));
            result.add(segment);
        }
        return result;
    }
    
    private Collection<ModifyColumnDefinitionSegment> createModifyColumnDefinitions(final String columnName) {
        ColumnDefinitionSegment columnDefinition = mock(ColumnDefinitionSegment.class);
        IdentifierValue identifier = new IdentifierValue(columnName);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, identifier);
        when(columnDefinition.getColumnName()).thenReturn(columnSegment);
        ModifyColumnDefinitionSegment segment = mock(ModifyColumnDefinitionSegment.class);
        when(segment.getColumnDefinition()).thenReturn(columnDefinition);
        return Collections.singletonList(segment);
    }
    
    private Collection<RenameColumnSegment> createRenameColumnSegments(final String oldColumnName, final String newColumnName) {
        RenameColumnSegment segment = mock(RenameColumnSegment.class);
        when(segment.getColumnName()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue(newColumnName)));
        when(segment.getOldColumnName()).thenReturn(new ColumnSegment(0, 0, new IdentifierValue(oldColumnName)));
        return Collections.singleton(segment);
    }
    
    private Collection<DropColumnDefinitionSegment> createDropColumnDefinitions(final String columnName) {
        ColumnSegment column = new ColumnSegment(0, 0, new IdentifierValue(columnName));
        DropColumnDefinitionSegment segment = mock(DropColumnDefinitionSegment.class);
        when(segment.getColumns()).thenReturn(Collections.singletonList(column));
        return Collections.singletonList(segment);
    }
    
    private Collection<AddConstraintDefinitionSegment> createAddConstraintDefinitions(final String indexName) {
        AddConstraintDefinitionSegment segment = mock(AddConstraintDefinitionSegment.class);
        when(segment.getConstraintDefinition()).thenReturn(mock(ConstraintDefinitionSegment.class));
        when(segment.getConstraintDefinition().getIndexName()).thenReturn(Optional.of(mock(IndexSegment.class)));
        when(segment.getConstraintDefinition().getIndexName().get().getIndexName()).thenReturn(new IndexNameSegment(0, 0, new IdentifierValue(indexName)));
        return Collections.singletonList(segment);
    }
    
    private Collection<DropIndexDefinitionSegment> createDropIndexDefinitions(final String indexName) {
        DropIndexDefinitionSegment segment = mock(DropIndexDefinitionSegment.class);
        when(segment.getIndexSegment()).thenReturn(mock(IndexSegment.class));
        when(segment.getIndexSegment().getIndexName()).thenReturn(new IndexNameSegment(0, 0, new IdentifierValue(indexName)));
        return Collections.singletonList(segment);
    }
    
    private Collection<RenameIndexDefinitionSegment> createRenameIndexDefinitions(final String oldIndexName, final String newIndexName) {
        RenameIndexDefinitionSegment segment = mock(RenameIndexDefinitionSegment.class);
        when(segment.getIndexSegment()).thenReturn(mock(IndexSegment.class));
        when(segment.getIndexSegment().getIndexName()).thenReturn(new IndexNameSegment(0, 0, new IdentifierValue(oldIndexName)));
        when(segment.getRenameIndexSegment()).thenReturn(mock(IndexSegment.class));
        when(segment.getRenameIndexSegment().getIndexName()).thenReturn(new IndexNameSegment(0, 0, new IdentifierValue(newIndexName)));
        return Collections.singletonList(segment);
    }
}
