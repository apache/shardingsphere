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

package org.apache.shardingsphere.sql.parser.core.database.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatementType;

/**
 * SQL Visitor rule.
 */
@RequiredArgsConstructor
public enum SQLVisitorRule {
    
    SELECT("Select", SQLStatementType.DML),
    
    TABLE("Table", SQLStatementType.DML),
    
    INSERT("Insert", SQLStatementType.DML),
    
    UPDATE("Update", SQLStatementType.DML),
    
    DELETE("Delete", SQLStatementType.DML),
    
    MERGE("Merge", SQLStatementType.DML),
    
    REPLACE("Replace", SQLStatementType.DML),
    
    COPY("Copy", SQLStatementType.DML),
    
    CREATE_TABLE("CreateTable", SQLStatementType.DDL),
    
    RENAME_TABLE("RenameTable", SQLStatementType.DDL),
    
    ALTER_TABLE("AlterTable", SQLStatementType.DDL),
    
    ALTER_AGGREGATE("AlterAggregate", SQLStatementType.DDL),
    
    ALTER_COLLATION("AlterCollation", SQLStatementType.DDL),
    
    ALTER_DEFAULT_PRIVILEGES("AlterDefaultPrivileges", SQLStatementType.DDL),
    
    ALTER_FOREIGN_DATA_WRAPPER("AlterForeignDataWrapper", SQLStatementType.DDL),
    
    ALTER_FOREIGN_TABLE("AlterForeignTable", SQLStatementType.DDL),

    DROP_FOREIGN_TABLE("DropForeignTable", SQLStatementType.DDL),

    ALTER_GROUP("AlterGroup", SQLStatementType.DDL),
    
    ALTER_MATERIALIZED_VIEW("AlterMaterializedView", SQLStatementType.DDL),
    
    DROP_TABLE("DropTable", SQLStatementType.DDL),
    
    TRUNCATE_TABLE("TruncateTable", SQLStatementType.DDL),
    
    CREATE_INDEX("CreateIndex", SQLStatementType.DDL),
    
    ALTER_INDEX("AlterIndex", SQLStatementType.DDL),
    
    DROP_INDEX("DropIndex", SQLStatementType.DDL),
    
    CREATE_PROCEDURE("CreateProcedure", SQLStatementType.DDL),
    
    ALTER_PROCEDURE("AlterProcedure", SQLStatementType.DDL),
    
    ALTER_STATEMENT("AlterStatement", SQLStatementType.DDL),
    
    DROP_PROCEDURE("DropProcedure", SQLStatementType.DDL),
    
    CREATE_FUNCTION("CreateFunction", SQLStatementType.DDL),
    
    ALTER_FUNCTION("AlterFunction", SQLStatementType.DDL),

    DROP_CAST("DropCast", SQLStatementType.DDL),
    
    DROP_FUNCTION("DropFunction", SQLStatementType.DDL),
    
    DROP_GROUP("DropGroup", SQLStatementType.DDL),
    
    CREATE_DATABASE("CreateDatabase", SQLStatementType.DDL),
    
    CREATE_DATABASE_LINK("CreateDatabaseLink", SQLStatementType.DDL),
    
    ALTER_DATABASE("AlterDatabase", SQLStatementType.DDL),
    
    DROP_DATABASE("DropDatabase", SQLStatementType.DDL),
    
    DROP_DATABASE_LINK("DropDatabaseLink", SQLStatementType.DDL),
    
    CREATE_DIMENSION("CreateDimension", SQLStatementType.DDL),
    
    ALTER_DIMENSION("AlterDimension", SQLStatementType.DDL),
    
    DROP_DIMENSION("DropDimension", SQLStatementType.DDL),
    
    CREATE_EVENT("CreateEvent", SQLStatementType.DDL),
    
    ALTER_EVENT("AlterEvent", SQLStatementType.DDL),
    
    DROP_EVENT("DropEvent", SQLStatementType.DDL),
    
    ALTER_INSTANCE("AlterInstance", SQLStatementType.DDL),
    
    CREATE_LOGFILE_GROUP("CreateLogfileGroup", SQLStatementType.DDL),
    
    ALTER_LOGFILE_GROUP("AlterLogfileGroup", SQLStatementType.DDL),
    
    DROP_LOGFILE_GROUP("DropLogfileGroup", SQLStatementType.DDL),
    
    CREATE_SERVER("CreateServer", SQLStatementType.DDL),
    
    ALTER_SERVER("AlterServer", SQLStatementType.DDL),
    
    ALTER_SESSION("AlterSession", SQLStatementType.DDL),
    
    ALTER_SYSTEM("AlterSystem", SQLStatementType.DDL),
    
    DROP_SERVER("DropServer", SQLStatementType.DDL),
    
    CREATE_TRIGGER("CreateTrigger", SQLStatementType.DDL),
    
    ALTER_TRIGGER("AlterTrigger", SQLStatementType.DDL),
    
    DROP_TRIGGER("DropTrigger", SQLStatementType.DDL),

    DROP_EVENT_TRIGGER("DropEventTrigger", SQLStatementType.DDL),
    
    CREATE_VIEW("CreateView", SQLStatementType.DDL),
    
    ALTER_VIEW("AlterView", SQLStatementType.DDL),
    
    DROP_VIEW("DropView", SQLStatementType.DDL),
    
    ANALYZE("Analyze", SQLStatementType.DDL),
    
    CREATE_SEQUENCE("CreateSequence", SQLStatementType.DDL),
    
    ALTER_SEQUENCE("AlterSequence", SQLStatementType.DDL),
    
    DROP_SEQUENCE("DropSequence", SQLStatementType.DDL),
    
    ALTER_SYNONYM("AlterSynonym", SQLStatementType.DDL),
    
    PREPARE("Prepare", SQLStatementType.DDL),
    
    EXECUTE_STMT("ExecuteStmt", SQLStatementType.DDL),
    
    DEALLOCATE("Deallocate", SQLStatementType.DDL),
    
    CREATE_TABLESPACE("CreateTablespace", SQLStatementType.DDL),
    
    ALTER_TABLESPACE("AlterTablespace", SQLStatementType.DDL),
    
    DROP_TABLESPACE("DropTablespace", SQLStatementType.DDL),
    
    ASSOCIATE_STATISTICS("AssociateStatistics", SQLStatementType.DDL),
    
    DISASSOCIATE_STATISTICS("DisassociateStatistics", SQLStatementType.DDL),
    
    AUDIT("Audit", SQLStatementType.DDL),
    
    NOAUDIT("NoAudit", SQLStatementType.DDL),
    
    COMMENT("Comment", SQLStatementType.DDL),
    
    FLASHBACK_DATABASE("FlashbackDatabase", SQLStatementType.DDL),
    
    FLASHBACK_TABLE("FlashbackTable", SQLStatementType.DDL),
    
    PURGE("Purge", SQLStatementType.DDL),
    
    RENAME("Rename", SQLStatementType.DDL),
    
    CREATE_EXTENSION("CreateExtension", SQLStatementType.DDL),
    
    ALTER_EXTENSION("AlterExtension", SQLStatementType.DDL),
    
    DROP_EXTENSION("DropExtension", SQLStatementType.DDL),
    
    DECLARE("Declare", SQLStatementType.DDL),
    
    DISCARD("Discard", SQLStatementType.DDL),
    
    SET_CONSTRAINTS("SetConstraints", SQLStatementType.TCL),
    
    SET_TRANSACTION("SetTransaction", SQLStatementType.TCL),
    
    SET_IMPLICIT_TRANSACTIONS("SetImplicitTransactions", SQLStatementType.TCL),
    
    BEGIN_TRANSACTION("BeginTransaction", SQLStatementType.TCL),
    
    BEGIN_DISTRIBUTED_TRANSACTION("BeginDistributedTransaction", SQLStatementType.TCL),
    
    START_TRANSACTION("StartTransaction", SQLStatementType.TCL),
    
    END("End", SQLStatementType.TCL),
    
    SET_AUTOCOMMIT("SetAutoCommit", SQLStatementType.TCL),
    
    COMMIT("Commit", SQLStatementType.TCL),
    
    COMMIT_WORK("CommitWork", SQLStatementType.TCL),
    
    ROLLBACK("Rollback", SQLStatementType.TCL),
    
    ROLLBACK_WORK("RollbackWork", SQLStatementType.TCL),
    
    SAVEPOINT("Savepoint", SQLStatementType.TCL),
    
    RELEASE_SAVEPOINT("ReleaseSavepoint", SQLStatementType.TCL),
    
    ROLLBACK_TO_SAVEPOINT("RollbackToSavepoint", SQLStatementType.TCL),
    
    LOCK("Lock", SQLStatementType.TCL),
    
    UNLOCK("Unlock", SQLStatementType.TCL),
    
    GRANT("Grant", SQLStatementType.DCL),
    
    GRANT_ROLE_OR_PRIVILEGE_TO("GrantRoleOrPrivilegeTo", SQLStatementType.DCL),
    
    GRANT_ROLE_OR_PRIVILEGE_ON_TO("GrantRoleOrPrivilegeOnTo", SQLStatementType.DCL),
    
    GRANT_PROXY("GrantPROXY", SQLStatementType.DCL),
    
    REVOKE("Revoke", SQLStatementType.DCL),
    
    REVOKE_FROM("RevokeFrom", SQLStatementType.DCL),
    
    REVOKE_ON_FROM("RevokeOnFrom", SQLStatementType.DCL),
    
    CREATE_USER("CreateUser", SQLStatementType.DCL),
    
    ALTER_USER("AlterUser", SQLStatementType.DCL),
    
    DROP_USER("DropUser", SQLStatementType.DCL),
    
    DENY_USER("Deny", SQLStatementType.DCL),
    
    RENAME_USER("RenameUser", SQLStatementType.DCL),
    
    SET_USER("SetUser", SQLStatementType.DCL),
    
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
    
    HELP("Help", SQLStatementType.DAL),
    
    EXPLAIN("Explain", SQLStatementType.DAL),
    
    SHOW_DATABASES("ShowDatabases", SQLStatementType.DAL),
    
    SHOW_TABLES("ShowTables", SQLStatementType.DAL),
    
    SHOW_EVENTS("ShowEvents", SQLStatementType.DAL),
    
    SHOW_CHARACTER_SET("ShowCharacterSet", SQLStatementType.DAL),
    
    SHOW_COLLATION("ShowCollation", SQLStatementType.DAL),
    
    SHOW_VARIABLES("ShowVariables", SQLStatementType.DAL),
    
    SHOW_TABLE_STATUS("ShowTableStatus", SQLStatementType.DAL),
    
    SHOW_COLUMNS("ShowColumns", SQLStatementType.DAL),
    
    SHOW_INDEX("ShowIndex", SQLStatementType.DAL),
    
    SHOW_CREATE_TABLE("ShowCreateTable", SQLStatementType.DAL),
    
    SHOW_OTHER("ShowOther", SQLStatementType.DAL),
    
    SHOW_REPLICAS("ShowReplicas", SQLStatementType.DAL),
    
    SHOW_REPLICA_STATUS("ShowReplicaStatus", SQLStatementType.DAL),
    
    SHOW_SLAVE_HOSTS("ShowSlaveHosts", SQLStatementType.DAL),
    
    SHOW_SLAVE_STATUS("ShowSlaveStatus", SQLStatementType.DAL),
    
    SHOW_STATUS("ShowStatus", SQLStatementType.DAL),
    
    SHOW("Show", SQLStatementType.DAL),
    
    SHOW_RELAYLOG_EVENTS("ShowRelaylogEventsStatement", SQLStatementType.DAL),
    
    SHOW_PROCEDURE_CODE("ShowProcedureCodeStatement", SQLStatementType.DAL),
    
    SHOW_OPEN_TABLES("ShowOpenTables", SQLStatementType.DAL),
    
    SHOW_TRIGGERS("ShowTriggers", SQLStatementType.DAL),
    
    SET_VARIABLE("SetVariable", SQLStatementType.DAL),
    
    SET("Set", SQLStatementType.DAL),
    
    SET_NAME("SetName", SQLStatementType.DAL),
    
    SET_CHARACTER("SetCharacter", SQLStatementType.DAL),
    
    RESET_PARAMETER("ResetParameter", SQLStatementType.DAL),
    
    VACUUM("Vacuum", SQLStatementType.DAL),
    
    CREATE_LOADABLE_FUNCTION("CreateLoadableFunction", SQLStatementType.DAL),
    
    ANALYZE_TABLE("AnalyzeTable", SQLStatementType.DAL),
    
    LOAD("Load", SQLStatementType.DAL),
    
    INSTALL("Install", SQLStatementType.DAL),
    
    UNINSTALL("Uninstall", SQLStatementType.DAL),
    
    FLUSH("Flush", SQLStatementType.DAL),
    
    RESTART("Restart", SQLStatementType.DAL),
    
    SHUTDOWN("Shutdown", SQLStatementType.DAL),
    
    CREATE_RESOURCE_GROUP("CreateResourceGroup", SQLStatementType.DAL),
    
    SET_RESOURCE_GROUP("SetResourceGroup", SQLStatementType.DAL),
    
    BINLOG("Binlog", SQLStatementType.DAL),
    
    OPTIMIZE_TABLE("OptimizeTable", SQLStatementType.DAL),
    
    CLONE("Clone", SQLStatementType.DAL),
    
    REPAIR_TABLE("RepairTable", SQLStatementType.DAL),
    
    KILL("Kill", SQLStatementType.DAL),
    
    RESET("ResetStatement", SQLStatementType.DAL),
    
    RESET_PERSIST("ResetPersistStatement", SQLStatementType.DAL),
    
    CACHE_INDEX("CacheIndex", SQLStatementType.DAL),
    
    LOAD_INDEX_INFO("LoadIndexInfo", SQLStatementType.DAL),
    
    CHECK_TABLE("CheckTable", SQLStatementType.DAL),
    
    CHECKSUM_TABLE("ChecksumTable", SQLStatementType.DAL),
    
    DROP_RESOURCE_GROUP("DropResourceGroup", SQLStatementType.DAL),
    
    ALTER_RESOURCE_GROUP("AlterResourceGroup", SQLStatementType.DAL),
    
    DELIMITER("Delimiter", SQLStatementType.DAL),
    
    CALL("Call", SQLStatementType.DML),
    
    CHANGE_MASTER("ChangeMaster", SQLStatementType.RL), 
    
    START_SLAVE("StartSlave", SQLStatementType.RL),
    
    STOP_SLAVE("StopSlave", SQLStatementType.RL),
    
    XA("Xa", SQLStatementType.TCL),
    
    ABORT("Abort", SQLStatementType.TCL),
    
    CREATE_SCHEMA("CreateSchema", SQLStatementType.DDL),
    
    ALTER_SCHEMA("AlterSchema", SQLStatementType.DDL),
    
    DROP_SCHEMA("DropSchema", SQLStatementType.DDL),
    
    CREATE_SERVICE("CreateService", SQLStatementType.DDL),
    
    ALTER_SERVICE("AlterService", SQLStatementType.DDL),
    
    DROP_SERVICE("DropService", SQLStatementType.DDL),
    
    DROP_DOMAIN("DropDomain", SQLStatementType.DDL),
    
    CREATE_DOMAIN("CreateDomain", SQLStatementType.DDL),
    
    CREATE_RULE("CreateRule", SQLStatementType.DDL),
    
    CREATE_LANGUAGE("CreateLanguage", SQLStatementType.DDL),
    
    ALTER_LANGUAGE("AlterLanguage", SQLStatementType.DDL),
    
    DROP_LANGUAGE("DropLanguage", SQLStatementType.DDL),
    
    CREATE_CONVERSION("CreateConversion", SQLStatementType.DDL),
    
    CREATE_TYPE("CreateType", SQLStatementType.DDL),
    
    DROP_CONVERSION("DropConversion", SQLStatementType.DDL),
    
    ALTER_DOMAIN("AlterDomain", SQLStatementType.DDL),
    
    ALTER_CONVERSION("AlterConversion", SQLStatementType.DDL),
    
    CREATE_TEXT_SEARCH("CreateTextSearch", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_DICTIONARY("AlterTextSearchDictionary", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_TEMPLATE("AlterTextSearchTemplate", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_PARSER("AlterTextSearchParser", SQLStatementType.DDL),
    
    DROP_POLICY("DropPolicy", SQLStatementType.DDL),
    
    DROP_OWNED("DropOwned", SQLStatementType.DDL),
    
    DROP_OPERATOR("DropOperator", SQLStatementType.DDL),
    
    DROP_MATERIALIZED_VIEW("DropMaterializedView", SQLStatementType.DDL),

    DROP_AGGREGATE("DropAggregate", SQLStatementType.DDL),
    
    DROP_COLLATION("DropCollation", SQLStatementType.DDL);
    
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
    public static SQLVisitorRule valueOf(final Class<? extends ParseTree> parseTreeClass) {
        String parseTreeClassName = parseTreeClass.getSimpleName();
        for (SQLVisitorRule each : values()) {
            if (each.getContextName().equals(parseTreeClassName)) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Can not find visitor rule: `%s`", parseTreeClassName));
    }
}
