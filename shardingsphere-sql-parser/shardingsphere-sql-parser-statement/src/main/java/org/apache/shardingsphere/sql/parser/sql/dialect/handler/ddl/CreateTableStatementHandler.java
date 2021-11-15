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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Create table statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateTableStatementHandler implements SQLStatementHandler {
    
    /**
     * Judge whether contains not exist clause or not.
     *
     * @param createTableStatement create table statement
     * @return whether contains not exist clause or not
     */
    public static boolean containsNotExistClause(final CreateTableStatement createTableStatement) {
        if (createTableStatement instanceof MySQLStatement) {
            return ((MySQLCreateTableStatement) createTableStatement).isContainsNotExistClause();
        }
        if (createTableStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLCreateTableStatement) createTableStatement).isContainsNotExistClause();
        }
        if (createTableStatement instanceof OpenGaussStatement) {
            return ((OpenGaussCreateTableStatement) createTableStatement).isContainsNotExistClause();
        }
        return false;
    }
    
    /**
     * Get select statement.
     *
     * @param createTableStatement create table statement
     * @return select statement
     */
    public static Optional<SelectStatement> getSelectStatement(final CreateTableStatement createTableStatement) {
        if (createTableStatement instanceof SQLServerStatement) {
            return ((SQLServerCreateTableStatement) createTableStatement).getSelectStatement();
        }
        return Optional.empty();
    }
    
    /**
     * Get list of columns.
     *
     * @param createTableStatement create table statement
     * @return list of columns
     */
    public static List<ColumnSegment> getColumns(final CreateTableStatement createTableStatement) {
        if (createTableStatement instanceof SQLServerStatement) {
            return ((SQLServerCreateTableStatement) createTableStatement).getColumns();
        }
        return Collections.emptyList();
    }
}
