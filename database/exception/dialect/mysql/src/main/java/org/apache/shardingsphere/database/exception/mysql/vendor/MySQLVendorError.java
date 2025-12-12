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

package org.apache.shardingsphere.database.exception.mysql.vendor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;

/**
 * MySQL vendor error.
 * 
 * @see <a href="https://dev.mysql.com/doc/mysql-errors/8.0/en/server-error-reference.html">Server Error Message Reference</a>
 */
@RequiredArgsConstructor
@Getter
public enum MySQLVendorError implements VendorError {
    
    ER_DB_CREATE_EXISTS_ERROR(XOpenSQLState.GENERAL_ERROR, 1007, "Can't create database '%s'; database exists"),
    
    ER_DB_DROP_NOT_EXISTS_ERROR(XOpenSQLState.GENERAL_ERROR, 1008, "Can't drop database '%s'; database doesn't exist"),
    
    ER_CON_COUNT_ERROR(XOpenSQLState.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT, 1040, "Too many connections"),
    
    ER_HANDSHAKE_ERROR(XOpenSQLState.COMMUNICATION_LINK_FAILURE, 1043, "Bad handshake"),
    
    ER_DBACCESS_DENIED_ERROR(XOpenSQLState.SYNTAX_ERROR, 1044, "Access denied for user '%s'@'%s' to database '%s'"),
    
    ER_ACCESS_DENIED_ERROR(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, 1045, "Access denied for user '%s'@'%s' (using password: %s)"),
    
    ER_NO_DB_ERROR(XOpenSQLState.INVALID_CATALOG_NAME, 1046, "No database selected"),
    
    ER_BAD_DB_ERROR(XOpenSQLState.SYNTAX_ERROR, 1049, "Unknown database '%s'"),
    
    ER_TABLE_EXISTS_ERROR(XOpenSQLState.DUPLICATE, 1050, "Table '%s' already exists"),
    
    ER_DUP_ENTRY(XOpenSQLState.INTEGRITY_CONSTRAINT_VIOLATION, 1062, "Duplicate entry '%s' for key %d"),
    
    ER_PARSE_ERROR(XOpenSQLState.SYNTAX_ERROR, 1064, "%s near '%s' at line %d"),
    
    ER_UNKNOWN_CHARACTER_SET(XOpenSQLState.SYNTAX_ERROR, 1115, "Unknown character set: '%s'"),
    
    ER_WRONG_VALUE_COUNT_ON_ROW(XOpenSQLState.MISMATCH_INSERT_VALUES_AND_COLUMNS, 1136, "Column count doesn't match value count at row %d"),
    
    ER_NO_SUCH_TABLE(XOpenSQLState.NOT_FOUND, 1146, "Table '%s' doesn't exist"),
    
    ER_UNKNOWN_SYSTEM_VARIABLE(XOpenSQLState.GENERAL_ERROR, 1193, "Unknown system variable '%s'"),
    
    ER_LOCAL_VARIABLE(XOpenSQLState.GENERAL_ERROR, 1228, "Variable '%s' is a SESSION variable and can't be used with SET GLOBAL"),
    
    ER_GLOBAL_VARIABLE(XOpenSQLState.GENERAL_ERROR, 1229, "Variable '%s' is a GLOBAL variable and should be set with SET GLOBAL"),
    
    ER_INCORRECT_GLOBAL_LOCAL_VAR(XOpenSQLState.GENERAL_ERROR, 1238, "Variable '%s' is a %s variable"),
    
    ER_UNKNOWN_COLLATION(XOpenSQLState.GENERAL_ERROR, 1273, "Unknown collation: '%s'"),
    
    ER_UNSUPPORTED_PS(XOpenSQLState.GENERAL_ERROR, 1295, "This command is not supported in the prepared statement protocol yet"),
    
    ER_PS_MANY_PARAM(XOpenSQLState.GENERAL_ERROR, 1390, "Prepared statement contains too many placeholders"),
    
    ER_INTERNAL_ERROR(XOpenSQLState.GENERAL_ERROR, 1815, "Internal error: %s"),
    
    ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE(XOpenSQLState.GENERAL_ERROR, 3176,
            "Please do not modify the %s table with an XA transaction. This is an internal system table used to store GTIDs for committed transactions. "
                    + "Although modifying it can lead to an inconsistent GTID state, if necessary you can modify it with a non-XA transaction.");
    
    private final SQLState sqlState;
    
    private final int vendorCode;
    
    private final String reason;
}
