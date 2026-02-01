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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create table statement.
 */
@Getter
@Setter
public final class CreateTableStatement extends DDLStatement {
    
    private SimpleTableSegment table;
    
    private SelectStatement selectStatement;
    
    private boolean ifNotExists;
    
    private SimpleTableSegment likeTable;
    
    private CreateTableOptionSegment createTableOption;
    
    private final Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
    
    private final Collection<ConstraintDefinitionSegment> constraintDefinitions = new LinkedList<>();
    
    private final List<ColumnSegment> columns = new LinkedList<>();
    
    private SQLStatementAttributes attributes;
    
    public CreateTableStatement(final DatabaseType databaseType) {
        super(databaseType);
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
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(getTables()), new CreateTableConstraintSQLStatementAttribute(), new CreateTableIndexSQLStatementAttribute());
    }
    
    private Collection<SimpleTableSegment> getTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(table);
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
