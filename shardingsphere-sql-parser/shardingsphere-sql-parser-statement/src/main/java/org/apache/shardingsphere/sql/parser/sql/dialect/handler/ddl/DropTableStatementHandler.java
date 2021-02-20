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
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLDropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropTableStatement;

/**
 * Drop table statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DropTableStatementHandler implements SQLStatementHandler {
    
    /**
     * Judge whether contains if exist clause.
     *
     * @param dropTableStatement drop table statement
     * @return contains if exist clause or not
     */
    public static boolean containsIfExistClause(final DropTableStatement dropTableStatement) {
        if (dropTableStatement instanceof MySQLStatement) {
            return ((MySQLDropTableStatement) dropTableStatement).isContainsIfExistClause();
        }
        if (dropTableStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLDropTableStatement) dropTableStatement).isContainsIfExistClause();
        }
        if (dropTableStatement instanceof SQLServerStatement) {
            return ((SQLServerDropTableStatement) dropTableStatement).isContainsIfExistClause();
        }
        return false;
    }
}
