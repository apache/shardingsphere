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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.rollup.RollupSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create table statement.
 */
@Getter
public final class CreateTableStatement extends DDLStatement {
    
    private final SimpleTableSegment table;
    
    private final SelectStatement selectStatement;
    
    private final boolean ifNotExists;
    
    private final SimpleTableSegment likeTable;
    
    private final CreateTableOptionSegment createTableOption;
    
    private final Collection<ColumnDefinitionSegment> columnDefinitions;
    
    private final Collection<ConstraintDefinitionSegment> constraintDefinitions;
    
    private final List<ColumnSegment> columns;
    
    private final Collection<RollupSegment> rollups;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private CreateTableStatement(final DatabaseType databaseType, final SimpleTableSegment table, final SelectStatement selectStatement,
                                 final boolean ifNotExists, final SimpleTableSegment likeTable, final CreateTableOptionSegment createTableOption,
                                 final Collection<ColumnDefinitionSegment> columnDefinitions, final Collection<ConstraintDefinitionSegment> constraintDefinitions,
                                 final List<ColumnSegment> columns, final Collection<RollupSegment> rollups) {
        super(databaseType);
        this.table = table;
        this.selectStatement = selectStatement;
        this.ifNotExists = ifNotExists;
        this.likeTable = likeTable;
        this.createTableOption = createTableOption;
        this.columnDefinitions = null == columnDefinitions ? Collections.emptyList() : columnDefinitions;
        this.constraintDefinitions = null == constraintDefinitions ? Collections.emptyList() : constraintDefinitions;
        this.columns = null == columns ? Collections.emptyList() : columns;
        this.rollups = null == rollups ? Collections.emptyList() : rollups;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(getTables()), new CreateTableConstraintSQLStatementAttribute(), new CreateTableIndexSQLStatementAttribute());
    }
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelectStatement() {
        return Optional.ofNullable(selectStatement);
    }
    
    /**
     * Get like table.
     *
     * @return like table
     */
    public Optional<SimpleTableSegment> getLikeTable() {
        return Optional.ofNullable(likeTable);
    }
    
    /**
     * Get create table option.
     *
     * @return create table option
     */
    public Optional<CreateTableOptionSegment> getCreateTableOption() {
        return Optional.ofNullable(createTableOption);
    }
    
    private Collection<SimpleTableSegment> getTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null != table) {
            result.add(table);
        }
        for (ColumnDefinitionSegment each : columnDefinitions) {
            result.addAll(each.getReferencedTables());
        }
        for (ConstraintDefinitionSegment each : constraintDefinitions) {
            if (each.getReferencedTable().isPresent()) {
                result.add(each.getReferencedTable().get());
            }
        }
        return result;
    }
    
    private class CreateTableConstraintSQLStatementAttribute implements ConstraintSQLStatementAttribute {
        
        @Override
        public Collection<ConstraintSegment> getConstraints() {
            Collection<ConstraintSegment> result = new LinkedList<>();
            for (ConstraintDefinitionSegment each : constraintDefinitions) {
                each.getConstraintName().ifPresent(result::add);
            }
            return result;
        }
    }
    
    private class CreateTableIndexSQLStatementAttribute implements IndexSQLStatementAttribute {
        
        @Override
        public Collection<IndexSegment> getIndexes() {
            Collection<IndexSegment> result = new LinkedList<>();
            for (ConstraintDefinitionSegment each : constraintDefinitions) {
                each.getIndexName().ifPresent(result::add);
            }
            return result;
        }
        
        @Override
        public Collection<ColumnSegment> getIndexColumns() {
            return constraintDefinitions.stream().flatMap(each -> each.getIndexColumns().stream()).collect(Collectors.toList());
        }
    }
}
