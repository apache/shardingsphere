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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ChangeColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyCollectionRetrievalSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.ReplaceColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.AddConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.DropConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ModifyConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.alter.ValidateConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.RenamePartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.primary.DropPrimaryKeyDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RenameRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter table statement.
 */
@Getter
@Setter
public final class AlterTableStatement extends DDLStatement {
    
    private SimpleTableSegment table;
    
    private SimpleTableSegment renameTable;
    
    private ConvertTableDefinitionSegment convertTableDefinition;
    
    private ModifyCollectionRetrievalSegment modifyCollectionRetrieval;
    
    private AlgorithmTypeSegment algorithmSegment;
    
    private LockTableSegment lockTableSegment;
    
    private DropPrimaryKeyDefinitionSegment dropPrimaryKeyDefinition;
    
    private final Collection<AddColumnDefinitionSegment> addColumnDefinitions = new LinkedList<>();
    
    private final Collection<ModifyColumnDefinitionSegment> modifyColumnDefinitions = new LinkedList<>();
    
    private final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions = new LinkedList<>();
    
    private final Collection<DropColumnDefinitionSegment> dropColumnDefinitions = new LinkedList<>();
    
    private final Collection<AddConstraintDefinitionSegment> addConstraintDefinitions = new LinkedList<>();
    
    private final Collection<ValidateConstraintDefinitionSegment> validateConstraintDefinitions = new LinkedList<>();
    
    private final Collection<ModifyConstraintDefinitionSegment> modifyConstraintDefinitions = new LinkedList<>();
    
    private final Collection<DropConstraintDefinitionSegment> dropConstraintDefinitions = new LinkedList<>();
    
    private final Collection<DropIndexDefinitionSegment> dropIndexDefinitions = new LinkedList<>();
    
    private final Collection<RenameColumnSegment> renameColumnDefinitions = new LinkedList<>();
    
    private final Collection<RenameIndexDefinitionSegment> renameIndexDefinitions = new LinkedList<>();
    
    private final Collection<ReplaceColumnDefinitionSegment> replaceColumnDefinitions = new LinkedList<>();
    
    private final Collection<RenameRollupDefinitionSegment> renameRollupDefinitions = new LinkedList<>();
    
    private final Collection<RenamePartitionDefinitionSegment> renamePartitionDefinitions = new LinkedList<>();
    
    private SQLStatementAttributes attributes;
    
    public AlterTableStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get rename table.
     *
     * @return rename table
     */
    public Optional<SimpleTableSegment> getRenameTable() {
        return Optional.ofNullable(renameTable);
    }
    
    /**
     * Get convert table definition.
     *
     * @return convert table definition
     */
    public Optional<ConvertTableDefinitionSegment> getConvertTableDefinition() {
        return Optional.ofNullable(convertTableDefinition);
    }
    
    /**
     * Get modify collection retrieval.
     *
     * @return modify collection retrieval
     */
    public Optional<ModifyCollectionRetrievalSegment> getModifyCollectionRetrieval() {
        return Optional.ofNullable(modifyCollectionRetrieval);
    }
    
    /**
     * Get algorithm segment.
     *
     * @return algorithm segment
     */
    public Optional<AlgorithmTypeSegment> getGetAlgorithmSegment() {
        return Optional.ofNullable(algorithmSegment);
    }
    
    /**
     * Get lock table Segment.
     *
     * @return lock table segment
     */
    public Optional<LockTableSegment> getLockTableSegment() {
        return Optional.ofNullable(lockTableSegment);
    }
    
    /**
     * Get drop primary key.
     *
     * @return drop primary key
     */
    public Optional<DropPrimaryKeyDefinitionSegment> getDropPrimaryKeyDefinition() {
        return Optional.ofNullable(dropPrimaryKeyDefinition);
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(getTables()), new AlterTableConstraintSQLStatementAttribute(), new AlterTableIndexSQLStatementAttribute());
    }
    
    private Collection<SimpleTableSegment> getTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(table);
        if (getRenameTable().isPresent()) {
            result.add(getRenameTable().get());
        }
        for (AddColumnDefinitionSegment each : addColumnDefinitions) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                result.addAll(columnDefinition.getReferencedTables());
            }
        }
        for (ModifyColumnDefinitionSegment each : modifyColumnDefinitions) {
            result.addAll(each.getColumnDefinition().getReferencedTables());
        }
        for (AddConstraintDefinitionSegment each : addConstraintDefinitions) {
            each.getConstraintDefinition().getReferencedTable().ifPresent(result::add);
        }
        for (ReplaceColumnDefinitionSegment each : replaceColumnDefinitions) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                result.addAll(columnDefinition.getReferencedTables());
            }
        }
        return result;
    }
    
    private class AlterTableConstraintSQLStatementAttribute implements ConstraintSQLStatementAttribute {
        
        @Override
        public Collection<ConstraintSegment> getConstraints() {
            Collection<ConstraintSegment> result = new LinkedList<>();
            for (AddConstraintDefinitionSegment each : addConstraintDefinitions) {
                each.getConstraintDefinition().getConstraintName().ifPresent(result::add);
            }
            validateConstraintDefinitions.stream().map(ValidateConstraintDefinitionSegment::getConstraintName).forEach(result::add);
            dropConstraintDefinitions.stream().map(DropConstraintDefinitionSegment::getConstraintName).forEach(result::add);
            return result;
        }
    }
    
    private class AlterTableIndexSQLStatementAttribute implements IndexSQLStatementAttribute {
        
        @Override
        public Collection<IndexSegment> getIndexes() {
            Collection<IndexSegment> result = new LinkedList<>();
            for (AddConstraintDefinitionSegment each : addConstraintDefinitions) {
                each.getConstraintDefinition().getIndexName().ifPresent(result::add);
            }
            dropIndexDefinitions.stream().map(DropIndexDefinitionSegment::getIndexSegment).forEach(result::add);
            for (RenameIndexDefinitionSegment each : getRenameIndexDefinitions()) {
                result.add(each.getIndexSegment());
                result.add(each.getRenameIndexSegment());
            }
            return result;
        }
        
        @Override
        public Collection<ColumnSegment> getIndexColumns() {
            return addConstraintDefinitions.stream().flatMap(each -> each.getConstraintDefinition().getIndexColumns().stream()).collect(Collectors.toList());
        }
    }
}
