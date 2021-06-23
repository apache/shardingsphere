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

package org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;

import java.util.Optional;

/**
 * PostgreSQL prepare statement.
 */
@ToString
@Getter
@Setter
public final class PostgreSQLPrepareStatement extends AbstractSQLStatement implements DDLStatement, PostgreSQLStatement {
    
    private SelectStatement select;
    
    private InsertStatement insert;
    
    private UpdateStatement update;
    
    private DeleteStatement delete;
    
    /**
     * Get select statement.
     *
     * @return select statement
     */
    public Optional<SelectStatement> getSelect() {
        return Optional.ofNullable(select);
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
}
