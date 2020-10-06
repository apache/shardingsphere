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

package org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.util.Optional;

/**
 * Valid statement segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public class ValidStatementSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private CreateTableStatement createTable;
    
    private AlterTableStatement alterTable;
    
    private DropTableStatement dropTable;
    
    private TruncateStatement truncate;
    
    private InsertStatement insert;
    
    private InsertStatement replace;
    
    private UpdateStatement update;
    
    private DeleteStatement delete;
    
    private SelectStatement select;
    
    /**
     * Get create table statement.
     *
     * @return create table statement
     */
    public Optional<CreateTableStatement> getCreateTable() {
        return Optional.ofNullable(createTable);
    }
    
    /**
     * Get alter table statement.
     *
     * @return alter table statement
     */
    public Optional<AlterTableStatement> getAlterTable() {
        return Optional.ofNullable(alterTable);
    }
    
    /**
     * Get drop table statement.
     *
     * @return drop table statement
     */
    public Optional<DropTableStatement> getDropTable() {
        return Optional.ofNullable(dropTable);
    }
    
    /**
     * Get truncate statement.
     *
     * @return truncate statement
     */
    public Optional<TruncateStatement> getTruncate() {
        return Optional.ofNullable(truncate);
    }
    
    /**
     * Get insert statement.
     *
     * @return insert statement
     */
    public Optional<InsertStatement> getInsert() {
        return Optional.ofNullable(insert);
    }
    
    /**
     * Get replace statement.
     *
     * @return replace statement
     */
    public Optional<InsertStatement> getReplace() {
        return Optional.ofNullable(replace);
    }
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public Optional<UpdateStatement> getUpdate() {
        return Optional.ofNullable(update);
    }
    
    /**
     * Get delete statement.
     *
     * @return delete statement
     */
    public Optional<DeleteStatement> getDelete() {
        return Optional.ofNullable(delete);
    }
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return Optional.ofNullable(select);
    }
}
