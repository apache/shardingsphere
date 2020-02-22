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

package org.apache.shardingsphere.sql.parser.core.constant;

import lombok.RequiredArgsConstructor;

/**
 * Rule name.
 */
@RequiredArgsConstructor
public enum RuleName {
    
    SELECT("Select"),
    
    INSERT("Insert"),
    
    UPDATE("Update"),
    
    DELETE("Delete"),
    
    REPLACE("Replace"),
    
    CREATE_TABLE("CreateTable"),
    
    ALTER_TABLE("AlterTable"),
    
    DROP_TABLE("DropTable"),
    
    TRUNCATE_TABLE("TruncateTable"),
    
    CREATE_INDEX("CreateIndex"),
    
    ALTER_INDEX("AlterIndex"),
    
    DROP_INDEX("DropIndex"),
    
    SET_TRANSACTION("SetTransaction"),
    
    SET_IMPLICIT_TRANSACTIONS("SetImplicitTransactions"),
    
    BEGIN_TRANSACTION("BeginTransaction"),
    
    SET_AUTOCOMMIT("SetAutoCommit"),
    
    COMMIT("Commit"),
    
    ROLLBACK("Rollback"),
    
    SAVE_POINT("Savepoint"),
    
    GRANT("Grant"),
    
    REVOKE("Revoke"),
    
    CREATE_USER("CreateUser"),
    
    ALTER_USER("AlterUser"),
    
    DROP_USER("DropUser"),
    
    DENY_USER("Deny"),
    
    RENAME_USER("RenameUser"),
    
    CREATE_ROLE("CreateRole"),
    
    ALTER_ROLE("AlterRole"),
    
    DROP_ROLE("DropRole"),
    
    CREATE_LOGIN("CreateLogin"),
    
    ALTER_LOGIN("AlterLogin"),
    
    DROP_LOGIN("DropLogin"),
    
    SET_DEFAULT_ROLE("SetDefaultRole"),
    
    SET_ROLE("SetRole"),
    
    SET_PASSWORD("SetPassword"),
    
    USE("Use"),
    
    DESC("Desc"),
    
    SHOW_DATABASES("ShowDatabases"),
    
    SHOW_TABLES("ShowTables"),
    
    SHOW_TABLE_STATUS("ShowTableStatus"),
    
    SHOW_COLUMNS("ShowColumns"),
    
    SHOW_INDEX("ShowIndex"),
    
    SHOW_CREATE_TABLE("ShowCreateTable"),
    
    SHOW_OTHER("ShowOther"),
    
    SHOW("Show"),
    
    SET_VARIABLE("SetVariable"),
    
    SET("Set"),
    
    RESET_PARAMETER("ResetParameter"),
    
    CALL("Call");
    
    private final String name;
    
    /**
     * Get name.
     * 
     * @return name
     */
    public String getName() {
        return name + "Context";
    }
}
