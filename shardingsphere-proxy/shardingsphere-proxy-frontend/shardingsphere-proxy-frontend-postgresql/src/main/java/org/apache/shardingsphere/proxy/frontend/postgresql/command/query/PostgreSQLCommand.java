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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

/**
 * PostgreSQL command.
 */
@RequiredArgsConstructor
public final class PostgreSQLCommand {
    
    private final SQLStatement sqlStatement;
    
    /**
     * Get SQL command.
     * 
     * @return SQL command
     */
    public String getSQLCommand() {
        if (sqlStatement instanceof InsertStatement) {
            return "INSERT";
        }
        if (sqlStatement instanceof DeleteStatement) {
            return "DELETE";
        }
        if (sqlStatement instanceof UpdateStatement) {
            return "UPDATE";
        }
        if (sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof CreateDataSourcesStatement || sqlStatement instanceof CreateShardingRuleStatement) {
            return "CREATE";
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return "DROP";
        }
        return "";
    }
}
