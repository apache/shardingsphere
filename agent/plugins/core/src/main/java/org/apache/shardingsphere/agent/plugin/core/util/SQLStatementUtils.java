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

package org.apache.shardingsphere.agent.plugin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.plugin.core.enums.SQLStatementType;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

/**
 * SQL statement utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementUtils {
    
    /**
     * Get SQL type.
     *
     * @param sqlStatement SQL statement
     * @return SQL statement type
     */
    public static SQLStatementType getType(final SQLStatement sqlStatement) {
        if (null == sqlStatement) {
            return SQLStatementType.OTHER;
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLType(sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return SQLStatementType.DDL;
        }
        if (sqlStatement instanceof DCLStatement) {
            return SQLStatementType.DCL;
        }
        if (sqlStatement instanceof DALStatement) {
            return SQLStatementType.DAL;
        }
        if (sqlStatement instanceof TCLStatement) {
            return SQLStatementType.TCL;
        }
        if (sqlStatement instanceof DistSQLStatement) {
            return getDistSQLType(sqlStatement);
        }
        return SQLStatementType.OTHER;
    }
    
    private static SQLStatementType getDMLType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return SQLStatementType.SELECT;
        }
        if (sqlStatement instanceof InsertStatement) {
            return SQLStatementType.INSERT;
        }
        if (sqlStatement instanceof UpdateStatement) {
            return SQLStatementType.UPDATE;
        }
        if (sqlStatement instanceof DeleteStatement) {
            return SQLStatementType.DELETE;
        }
        return SQLStatementType.DML;
    }
    
    private static SQLStatementType getDistSQLType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof RQLStatement) {
            return SQLStatementType.RQL;
        }
        if (sqlStatement instanceof RDLStatement) {
            return SQLStatementType.RDL;
        }
        if (sqlStatement instanceof RALStatement) {
            return SQLStatementType.RAL;
        }
        if (sqlStatement instanceof RULStatement) {
            return SQLStatementType.RUL;
        }
        return SQLStatementType.OTHER;
    }
}
