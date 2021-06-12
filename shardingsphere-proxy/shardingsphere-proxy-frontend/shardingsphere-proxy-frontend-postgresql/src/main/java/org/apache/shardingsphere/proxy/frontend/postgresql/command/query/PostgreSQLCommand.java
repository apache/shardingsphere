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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query;

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * PostgreSQL command.
 */
public enum PostgreSQLCommand {
    
    SELECT(SelectStatement.class),
    INSERT(InsertStatement.class),
    UPDATE(UpdateStatement.class),
    DELETE(DeleteStatement.class),
    CREATE(CreateDatabaseStatement.class, AddResourceStatement.class, CreateShardingTableRuleStatement.class),
    DROP(DropDatabaseStatement.class),
    BEGIN(BeginTransactionStatement.class),
    COMMIT(CommitStatement.class),
    ROLLBACK(RollbackStatement.class),
    SET(SetStatement.class);
    
    private final Collection<Class<? extends SQLStatement>> sqlStatementClasses;
    
    @SafeVarargs
    PostgreSQLCommand(final Class<? extends SQLStatement>... sqlStatementClasses) {
        this.sqlStatementClasses = Arrays.asList(sqlStatementClasses);
    }
    
    /**
     * Value of PostgreSQL command via SQL statement class.
     * 
     * @param sqlStatementClass SQL statement class
     * @return PostgreSQL command
     */
    public static Optional<PostgreSQLCommand> valueOf(final Class<? extends SQLStatement> sqlStatementClass) {
        return Arrays.stream(PostgreSQLCommand.values()).filter(each -> matches(sqlStatementClass, each)).findAny();
    }
    
    private static boolean matches(final Class<? extends SQLStatement> sqlStatementClass, final PostgreSQLCommand postgreSQLCommand) {
        return postgreSQLCommand.sqlStatementClasses.stream().anyMatch(each -> each.isAssignableFrom(sqlStatementClass));
    }
}
