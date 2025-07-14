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

package org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

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
    
    private SQLStatement sqlStatement;
    
    /**
     * Get create table statement.
     *
     * @return create table statement
     */
    public Optional<CreateTableStatement> getCreateTable() {
        return sqlStatement instanceof CreateTableStatement ? Optional.of((CreateTableStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get alter table statement.
     *
     * @return alter table statement
     */
    public Optional<AlterTableStatement> getAlterTable() {
        return sqlStatement instanceof AlterTableStatement ? Optional.of((AlterTableStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get drop table statement.
     *
     * @return drop table statement
     */
    public Optional<DropTableStatement> getDropTable() {
        return sqlStatement instanceof DropTableStatement ? Optional.of((DropTableStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get truncate statement.
     *
     * @return truncate statement
     */
    public Optional<TruncateStatement> getTruncate() {
        return sqlStatement instanceof TruncateStatement ? Optional.of((TruncateStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get insert statement.
     *
     * @return insert statement
     */
    public Optional<InsertStatement> getInsert() {
        return sqlStatement instanceof InsertStatement ? Optional.of((InsertStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get replace statement.
     *
     * @return replace statement
     */
    public Optional<InsertStatement> getReplace() {
        return sqlStatement instanceof InsertStatement ? Optional.of((InsertStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public Optional<UpdateStatement> getUpdate() {
        return sqlStatement instanceof UpdateStatement ? Optional.of((UpdateStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get delete statement.
     *
     * @return delete statement
     */
    public Optional<DeleteStatement> getDelete() {
        return sqlStatement instanceof DeleteStatement ? Optional.of((DeleteStatement) sqlStatement) : Optional.empty();
    }
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return sqlStatement instanceof SelectStatement ? Optional.of((SelectStatement) sqlStatement) : Optional.empty();
    }
}
