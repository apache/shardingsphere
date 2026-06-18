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

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.distribution.ModifyDistributionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.engine.ModifyEngineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.feature.EnableFeatureSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.DropIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.RenameIndexDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.AddPartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.AddPartitionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.ModifyPartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.partition.RenamePartitionDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.primary.DropPrimaryKeyDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.AddRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.DropRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.alter.OrderByColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RenameRollupDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.AlgorithmTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ConvertTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.LockTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ModifyTableCommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.ReplaceTableDefinitionSegment;
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
public final class AlterTableStatement extends DDLStatement {
    
    private final SimpleTableSegment table;
    
    private final SimpleTableSegment renameTable;
    
    private final ReplaceTableDefinitionSegment replaceTable;
    
    private final ConvertTableDefinitionSegment convertTableDefinition;
    
    private final ModifyCollectionRetrievalSegment modifyCollectionRetrieval;
    
    private final AlgorithmTypeSegment algorithmSegment;
    
    private final LockTableSegment lockTableSegment;
    
    private final DropPrimaryKeyDefinitionSegment dropPrimaryKeyDefinition;
    
    private final Collection<PropertiesSegment> setPropertiesDefinitions;
    
    private final Collection<EnableFeatureSegment> enableFeatureDefinitions;
    
    private final Collection<ModifyTableCommentSegment> modifyTableCommentDefinitions;
    
    private final Collection<ModifyEngineSegment> modifyEngineDefinitions;
    
    private final Collection<ModifyDistributionSegment> modifyDistributionDefinitions;
    
    private final Collection<AddColumnDefinitionSegment> addColumnDefinitions;
    
    private final Collection<ModifyColumnDefinitionSegment> modifyColumnDefinitions;
    
    private final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions;
    
    private final Collection<DropColumnDefinitionSegment> dropColumnDefinitions;
    
    private final Collection<AddConstraintDefinitionSegment> addConstraintDefinitions;
    
    private final Collection<ValidateConstraintDefinitionSegment> validateConstraintDefinitions;
    
    private final Collection<ModifyConstraintDefinitionSegment> modifyConstraintDefinitions;
    
    private final Collection<DropConstraintDefinitionSegment> dropConstraintDefinitions;
    
    private final Collection<DropIndexDefinitionSegment> dropIndexDefinitions;
    
    private final Collection<RenameColumnSegment> renameColumnDefinitions;
    
    private final Collection<RenameIndexDefinitionSegment> renameIndexDefinitions;
    
    private final Collection<ReplaceColumnDefinitionSegment> replaceColumnDefinitions;
    
    private final Collection<AddRollupDefinitionSegment> addRollupDefinitions;
    
    private final Collection<DropRollupDefinitionSegment> dropRollupDefinitions;
    
    private final Collection<RenameRollupDefinitionSegment> renameRollupDefinitions;
    
    private final Collection<OrderByColumnDefinitionSegment> orderByColumnDefinitions;
    
    private final Collection<RenamePartitionDefinitionSegment> renamePartitionDefinitions;
    
    private final Collection<AddPartitionDefinitionSegment> addPartitionDefinitions;
    
    private final Collection<AddPartitionsSegment> addPartitionsSegments;
    
    private final Collection<ModifyPartitionDefinitionSegment> modifyPartitionDefinitions;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private AlterTableStatement(final DatabaseType databaseType, final SimpleTableSegment table, final SimpleTableSegment renameTable, final ReplaceTableDefinitionSegment replaceTable,
                                final ConvertTableDefinitionSegment convertTableDefinition, final ModifyCollectionRetrievalSegment modifyCollectionRetrieval,
                                final AlgorithmTypeSegment algorithmSegment, final LockTableSegment lockTableSegment, final DropPrimaryKeyDefinitionSegment dropPrimaryKeyDefinition,
                                @Singular("setPropertiesDefinition") final Collection<PropertiesSegment> setPropertiesDefinitions,
                                @Singular("enableFeatureDefinition") final Collection<EnableFeatureSegment> enableFeatureDefinitions,
                                @Singular("modifyTableCommentDefinition") final Collection<ModifyTableCommentSegment> modifyTableCommentDefinitions,
                                @Singular("modifyEngineDefinition") final Collection<ModifyEngineSegment> modifyEngineDefinitions,
                                @Singular("modifyDistributionDefinition") final Collection<ModifyDistributionSegment> modifyDistributionDefinitions,
                                @Singular("addColumnDefinition") final Collection<AddColumnDefinitionSegment> addColumnDefinitions,
                                @Singular("modifyColumnDefinition") final Collection<ModifyColumnDefinitionSegment> modifyColumnDefinitions,
                                @Singular("changeColumnDefinition") final Collection<ChangeColumnDefinitionSegment> changeColumnDefinitions,
                                @Singular("dropColumnDefinition") final Collection<DropColumnDefinitionSegment> dropColumnDefinitions,
                                @Singular("addConstraintDefinition") final Collection<AddConstraintDefinitionSegment> addConstraintDefinitions,
                                @Singular("validateConstraintDefinition") final Collection<ValidateConstraintDefinitionSegment> validateConstraintDefinitions,
                                @Singular("modifyConstraintDefinition") final Collection<ModifyConstraintDefinitionSegment> modifyConstraintDefinitions,
                                @Singular("dropConstraintDefinition") final Collection<DropConstraintDefinitionSegment> dropConstraintDefinitions,
                                @Singular("dropIndexDefinition") final Collection<DropIndexDefinitionSegment> dropIndexDefinitions,
                                @Singular("renameColumnDefinition") final Collection<RenameColumnSegment> renameColumnDefinitions,
                                @Singular("renameIndexDefinition") final Collection<RenameIndexDefinitionSegment> renameIndexDefinitions,
                                @Singular("replaceColumnDefinition") final Collection<ReplaceColumnDefinitionSegment> replaceColumnDefinitions,
                                @Singular("addRollupDefinition") final Collection<AddRollupDefinitionSegment> addRollupDefinitions,
                                @Singular("dropRollupDefinition") final Collection<DropRollupDefinitionSegment> dropRollupDefinitions,
                                @Singular("renameRollupDefinition") final Collection<RenameRollupDefinitionSegment> renameRollupDefinitions,
                                @Singular("orderByColumnDefinition") final Collection<OrderByColumnDefinitionSegment> orderByColumnDefinitions,
                                @Singular("renamePartitionDefinition") final Collection<RenamePartitionDefinitionSegment> renamePartitionDefinitions,
                                @Singular("addPartitionDefinition") final Collection<AddPartitionDefinitionSegment> addPartitionDefinitions,
                                @Singular("addPartitionsSegment") final Collection<AddPartitionsSegment> addPartitionsSegments,
                                @Singular("modifyPartitionDefinition") final Collection<ModifyPartitionDefinitionSegment> modifyPartitionDefinitions) {
        super(databaseType);
        this.table = table;
        this.renameTable = renameTable;
        this.replaceTable = replaceTable;
        this.convertTableDefinition = convertTableDefinition;
        this.modifyCollectionRetrieval = modifyCollectionRetrieval;
        this.algorithmSegment = algorithmSegment;
        this.lockTableSegment = lockTableSegment;
        this.dropPrimaryKeyDefinition = dropPrimaryKeyDefinition;
        this.setPropertiesDefinitions = setPropertiesDefinitions;
        this.enableFeatureDefinitions = enableFeatureDefinitions;
        this.modifyTableCommentDefinitions = modifyTableCommentDefinitions;
        this.modifyEngineDefinitions = modifyEngineDefinitions;
        this.modifyDistributionDefinitions = modifyDistributionDefinitions;
        this.addColumnDefinitions = addColumnDefinitions;
        this.modifyColumnDefinitions = modifyColumnDefinitions;
        this.changeColumnDefinitions = changeColumnDefinitions;
        this.dropColumnDefinitions = dropColumnDefinitions;
        this.addConstraintDefinitions = addConstraintDefinitions;
        this.validateConstraintDefinitions = validateConstraintDefinitions;
        this.modifyConstraintDefinitions = modifyConstraintDefinitions;
        this.dropConstraintDefinitions = dropConstraintDefinitions;
        this.dropIndexDefinitions = dropIndexDefinitions;
        this.renameColumnDefinitions = renameColumnDefinitions;
        this.renameIndexDefinitions = renameIndexDefinitions;
        this.replaceColumnDefinitions = replaceColumnDefinitions;
        this.addRollupDefinitions = addRollupDefinitions;
        this.dropRollupDefinitions = dropRollupDefinitions;
        this.renameRollupDefinitions = renameRollupDefinitions;
        this.orderByColumnDefinitions = orderByColumnDefinitions;
        this.renamePartitionDefinitions = renamePartitionDefinitions;
        this.addPartitionDefinitions = addPartitionDefinitions;
        this.addPartitionsSegments = addPartitionsSegments;
        this.modifyPartitionDefinitions = modifyPartitionDefinitions;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(getTables()), new AlterTableConstraintSQLStatementAttribute(), new AlterTableIndexSQLStatementAttribute());
    }
    
    private Collection<SimpleTableSegment> getTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(table);
        if (getRenameTable().isPresent()) {
            result.add(getRenameTable().get());
        }
        if (getReplaceTable().isPresent() && null != getReplaceTable().get().getReplaceTable()) {
            result.add(getReplaceTable().get().getReplaceTable());
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
    
    /**
     * Get rename table.
     *
     * @return rename table
     */
    public Optional<SimpleTableSegment> getRenameTable() {
        return Optional.ofNullable(renameTable);
    }
    
    /**
     * Get replace table definition.
     *
     * @return replace table definition
     */
    public Optional<ReplaceTableDefinitionSegment> getReplaceTable() {
        return Optional.ofNullable(replaceTable);
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
