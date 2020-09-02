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

package org.apache.shardingsphere.sql.parser.core.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatementType;

/**
 * Visitor rule.
 */
@RequiredArgsConstructor
public enum VisitorRule {
    
    SELECT("Select", SQLStatementType.DML),
    
    INSERT("Insert", SQLStatementType.DML),
    
    UPDATE("Update", SQLStatementType.DML),
    
    DELETE("Delete", SQLStatementType.DML),
    
    REPLACE("Replace", SQLStatementType.DML),
    
    CREATE_TABLE("CreateTable", SQLStatementType.DDL),
    
    ALTER_TABLE("AlterTable", SQLStatementType.DDL),
    
    DROP_TABLE("DropTable", SQLStatementType.DDL),
    
    TRUNCATE_TABLE("TruncateTable", SQLStatementType.DDL),
    
    CREATE_INDEX("CreateIndex", SQLStatementType.DDL),
    
    ALTER_INDEX("AlterIndex", SQLStatementType.DDL),
    
    DROP_INDEX("DropIndex", SQLStatementType.DDL),

    CREATE_PROCEDURE("CreateProcedure", SQLStatementType.DDL),

    ALTER_PROCEDURE("AlterProcedure", SQLStatementType.DDL),

    DROP_PROCEDURE("DropProcedure", SQLStatementType.DDL),

    CREATE_FUNCTION("CreateFunction", SQLStatementType.DDL),

    ALTER_FUNCTION("AlterFunction", SQLStatementType.DDL),

    DROP_FUNCTION("DropFunction", SQLStatementType.DDL),

    CREATE_DATABASE("CreateDatabase", SQLStatementType.DDL),
    
    ALTER_DATABASE("AlterDatabase", SQLStatementType.DDL),

    DROP_DATABASE("DropDatabase", SQLStatementType.DDL),

    CREATE_EVENT("CreateEvent", SQLStatementType.DDL),

    ALTER_EVENT("AlterEvent", SQLStatementType.DDL),
    
    DROP_EVENT("DropEvent", SQLStatementType.DDL),

    ALTER_INSTANCE("AlterInstance", SQLStatementType.DDL),

    CREATE_LOGFILE_GROUP("CreateLogfileGroup", SQLStatementType.DDL),

    ALTER_LOGFILE_GROUP("AlterLogfileGroup", SQLStatementType.DDL),

    DROP_LOGFILE_GROUP("DropLogfileGroup", SQLStatementType.DDL),

    CREATE_SERVER("CreateServer", SQLStatementType.DDL),
    
    ALTER_SERVER("AlterServer", SQLStatementType.DDL),

    DROP_SERVER("DropServer", SQLStatementType.DDL),
    
    SET_TRANSACTION("SetTransaction", SQLStatementType.TCL),
    
    SET_IMPLICIT_TRANSACTIONS("SetImplicitTransactions", SQLStatementType.TCL),
    
    BEGIN_TRANSACTION("BeginTransaction", SQLStatementType.TCL),
    
    SET_AUTOCOMMIT("SetAutoCommit", SQLStatementType.TCL),
    
    COMMIT("Commit", SQLStatementType.TCL),
    
    ROLLBACK("Rollback", SQLStatementType.TCL),
    
    SAVE_POINT("Savepoint", SQLStatementType.TCL),
    
    GRANT("Grant", SQLStatementType.DCL),
    
    REVOKE("Revoke", SQLStatementType.DCL),
    
    CREATE_USER("CreateUser", SQLStatementType.DCL),
    
    ALTER_USER("AlterUser", SQLStatementType.DCL),
    
    DROP_USER("DropUser", SQLStatementType.DCL),
    
    DENY_USER("Deny", SQLStatementType.DCL),
    
    RENAME_USER("RenameUser", SQLStatementType.DCL),
    
    CREATE_ROLE("CreateRole", SQLStatementType.DCL),
    
    ALTER_ROLE("AlterRole", SQLStatementType.DCL),
    
    DROP_ROLE("DropRole", SQLStatementType.DCL),
    
    CREATE_LOGIN("CreateLogin", SQLStatementType.DCL),
    
    ALTER_LOGIN("AlterLogin", SQLStatementType.DCL),
    
    DROP_LOGIN("DropLogin", SQLStatementType.DCL),
    
    SET_DEFAULT_ROLE("SetDefaultRole", SQLStatementType.DCL),
    
    SET_ROLE("SetRole", SQLStatementType.DCL),
    
    SET_PASSWORD("SetPassword", SQLStatementType.DCL),
    
    USE("Use", SQLStatementType.DAL),
    
    DESC("Desc", SQLStatementType.DAL),
    
    EXPLAIN("Explain", SQLStatementType.DAL),
    
    SHOW_DATABASES("ShowDatabases", SQLStatementType.DAL),
    
    SHOW_TABLES("ShowTables", SQLStatementType.DAL),
    
    SHOW_TABLE_STATUS("ShowTableStatus", SQLStatementType.DAL),
    
    SHOW_COLUMNS("ShowColumns", SQLStatementType.DAL),
    
    SHOW_INDEX("ShowIndex", SQLStatementType.DAL),
    
    SHOW_CREATE_TABLE("ShowCreateTable", SQLStatementType.DAL),
    
    SHOW_OTHER("ShowOther", SQLStatementType.DAL),
    
    SHOW("Show", SQLStatementType.DAL),
    
    SET_VARIABLE("SetVariable", SQLStatementType.DAL),
    
    SET("Set", SQLStatementType.DAL),
    
    SET_NAME("SetName", SQLStatementType.DAL),
    
    SET_CHARACTER("SetCharacter", SQLStatementType.DAL),
    
    RESET_PARAMETER("ResetParameter", SQLStatementType.DAL),
    
    CALL("Call", SQLStatementType.DML),
    
    CHANGE_MASTER("ChangeMaster", SQLStatementType.RL), 
    
    START_SLAVE("StartSlave", SQLStatementType.RL),
    
    STOP_SLAVE("StopSlave", SQLStatementType.RL);
    
    private final String name;
    
    @Getter
    private final SQLStatementType type;
    
    private String getContextName() {
        return name + "Context";
    }
    
    /**
     * Value of visitor rule.
     * 
     * @param parseTreeClass parse tree class
     * @return visitor rule
     */
    public static VisitorRule valueOf(final Class<? extends ParseTree> parseTreeClass) {
        String parseTreeClassName = parseTreeClass.getSimpleName();
        for (VisitorRule each : values()) {
            if (each.getContextName().equals(parseTreeClassName)) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Can not find visitor rule: `%s`", parseTreeClassName));
    }
}
