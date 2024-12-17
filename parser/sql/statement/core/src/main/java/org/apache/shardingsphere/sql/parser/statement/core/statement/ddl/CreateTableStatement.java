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

package org.apache.shardingsphere.sql.parser.statement.core.statement.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Create table statement.
 */
@Getter
@Setter
public abstract class CreateTableStatement extends AbstractSQLStatement implements DDLStatement {
    
    private SimpleTableSegment table;
    
    private SelectStatement selectStatement;
    
    private final Collection<ColumnDefinitionSegment> columnDefinitions = new LinkedList<>();
    
    private final Collection<ConstraintDefinitionSegment> constraintDefinitions = new LinkedList<>();
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelectStatement() {
        return Optional.ofNullable(selectStatement);
    }
    
    /**
     * Judge whether contains if not exists or not.
     *
     * @return whether contains if not exists or not
     */
    public boolean isIfNotExists() {
        return false;
    }
    
    /**
     * Set if not exists.
     *
     * @param ifNotExists if not exists
     */
    public void setIfNotExists(final boolean ifNotExists) {
    }
    
    /**
     * Get list of columns.
     *
     * @return list of columns
     */
    public List<ColumnSegment> getColumns() {
        return Collections.emptyList();
    }
    
    /**
     * Get like table.
     *
     * @return like table
     */
    public Optional<SimpleTableSegment> getLikeTable() {
        return Optional.empty();
    }
    
    /**
     * Set like table.
     *
     * @param likeTable like table
     */
    public void setLikeTable(final SimpleTableSegment likeTable) {
    }
    
    /**
     * Get create table option.
     *
     * @return create table option
     */
    public Optional<CreateTableOptionSegment> getCreateTableOption() {
        return Optional.empty();
    }
    
    /**
     * Set create table option.
     *
     * @param createTableOption create table option
     */
    public void setCreateTableOption(final CreateTableOptionSegment createTableOption) {
    }
}
