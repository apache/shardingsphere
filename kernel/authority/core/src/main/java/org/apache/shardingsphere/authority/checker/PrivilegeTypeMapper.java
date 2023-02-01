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

package org.apache.shardingsphere.authority.checker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

/**
 * Privilege type mapper.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeTypeMapper {
    
    /**
     * Get privilege type.
     * 
     * @param sqlStatement SQL statement
     * @return privilege type
     */
    public static PrivilegeType getPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLPrivilegeType(sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLPrivilegeType(sqlStatement);
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }
    
    private static PrivilegeType getDMLPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return PrivilegeType.SELECT;
        }
        if (sqlStatement instanceof InsertStatement) {
            return PrivilegeType.INSERT;
        }
        if (sqlStatement instanceof UpdateStatement) {
            return PrivilegeType.UPDATE;
        }
        if (sqlStatement instanceof DeleteStatement) {
            return PrivilegeType.DELETE;
        }
        return null;
    }
    
    private static PrivilegeType getDDLPrivilegeType(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof AlterDatabaseStatement) {
            return PrivilegeType.ALTER_ANY_DATABASE;
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return PrivilegeType.ALTER;
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return PrivilegeType.CREATE_DATABASE;
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return PrivilegeType.CREATE_TABLE;
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return PrivilegeType.CREATE_FUNCTION;
        }
        if (sqlStatement instanceof DropTableStatement || sqlStatement instanceof DropDatabaseStatement) {
            return PrivilegeType.DROP;
        }
        if (sqlStatement instanceof TruncateStatement) {
            return PrivilegeType.TRUNCATE;
        }
        return null;
    }
}
