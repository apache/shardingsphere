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

package org.apache.shardingsphere.sql.parser.engine.core.database.visitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLASTVisitorException;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.SQLStatementType;

/**
 * SQL visitor rule.
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
    
    HANDLER_STATEMENT("HandlerStatement", SQLStatementType.DML),
    
    CREATE_TABLE("CreateTable", SQLStatementType.DDL),
    
    CREATE_AGGREGATE("CreateAggregate", SQLStatementType.DDL),
    
    RENAME_TABLE("RenameTable", SQLStatementType.DDL),
    
    ALTER_TABLE("AlterTable", SQLStatementType.DDL),
    
    MSCK("MsckStatement", SQLStatementType.DDL),
    
    ALTER_TYPE("AlterType", SQLStatementType.DDL),
    
    ALTER_AGGREGATE("AlterAggregate", SQLStatementType.DDL),
    
    ALTER_COLLATION("AlterCollation", SQLStatementType.DDL),
    
    ALTER_DEFAULT_PRIVILEGES("AlterDefaultPrivileges", SQLStatementType.DDL),
    
    ALTER_FOREIGN_DATA_WRAPPER("AlterForeignDataWrapper", SQLStatementType.DDL),
    
    ALTER_FOREIGN_TABLE("AlterForeignTable", SQLStatementType.DDL),
    
    DROP_FOREIGN_TABLE("DropForeignTable", SQLStatementType.DDL),
    
    ALTER_GROUP("AlterGroup", SQLStatementType.DDL),
    
    ALTER_MATERIALIZED_VIEW("AlterMaterializedView", SQLStatementType.DDL),
    
    ALTER_MATERIALIZED_VIEW_LOG("AlterMaterializedViewLog", SQLStatementType.DDL),
    
    ALTER_PLUGGABLE_DATABASE("AlterPluggableDatabase", SQLStatementType.DDL),
    
    ALTER_OPERATOR("AlterOperator", SQLStatementType.DDL),
    
    ALTER_PROFILE("AlterProfile", SQLStatementType.DDL),
    
    ALTER_ROLLBACK_SEGMENT("AlterRollbackSegment", SQLStatementType.DDL),
    
    DROP_TABLE("DropTable", SQLStatementType.DDL),
    
    TRUNCATE_TABLE("TruncateTable", SQLStatementType.DDL),
    
    CREATE_INDEX("CreateIndex", SQLStatementType.DDL),
    
    ALTER_INDEX("AlterIndex", SQLStatementType.DDL),
    
    DROP_INDEX("DropIndex", SQLStatementType.DDL),
    
    CREATE_PROCEDURE("CreateProcedure", SQLStatementType.DDL),
    
    CREATE_PUBLICATION("CreatePublication", SQLStatementType.DDL),
    
    ALTER_PUBLICATION("AlterPublication", SQLStatementType.DDL),
    
    ALTER_SUBSCRIPTION("AlterSubscription", SQLStatementType.DDL),
    
    ALTER_PROCEDURE("AlterProcedure", SQLStatementType.DDL),
    
    ALTER_STATEMENT("AlterStatement", SQLStatementType.DDL),
    
    DROP_PROCEDURE("DropProcedure", SQLStatementType.DDL),
    
    DROP_ROUTINE("DropRoutine", SQLStatementType.DDL),
    
    DROP_RULE("DropRule", SQLStatementType.DDL),
    
    DROP_STATISTICS("DropStatistics", SQLStatementType.DDL),
    
    DROP_PUBLICATION("DropPublication", SQLStatementType.DDL),
    
    DROP_SUBSCRIPTION("DropSubscription", SQLStatementType.DDL),
    
    CREATE_FUNCTION("CreateFunction", SQLStatementType.DDL),
    
    ALTER_FUNCTION("AlterFunction", SQLStatementType.DDL),
    
    DROP_CAST("DropCast", SQLStatementType.DDL),
    
    DROP_FUNCTION("DropFunction", SQLStatementType.DDL),
    
    RELOAD_FUNCTION("ReloadFunction", SQLStatementType.DDL),
    
    DROP_GROUP("DropGroup", SQLStatementType.DDL),
    
    CREATE_DATABASE("CreateDatabase", SQLStatementType.DDL),
    
    CREATE_DATABASE_LINK("CreateDatabaseLink", SQLStatementType.DDL),
    
    ALTER_DATABASE("AlterDatabase", SQLStatementType.DDL),
    
    ALTER_DATABASE_LINK("AlterDatabaseLink", SQLStatementType.DDL),
    
    DROP_DATABASE("DropDatabase", SQLStatementType.DDL),
    
    DROP_DATABASE_LINK("DropDatabaseLink", SQLStatementType.DDL),
    
    ALTER_DATABASE_DICTIONARY("AlterDatabaseDictionary", SQLStatementType.DDL),
    
    CREATE_DIMENSION("CreateDimension", SQLStatementType.DDL),
    
    ALTER_DIMENSION("AlterDimension", SQLStatementType.DDL),
    
    DROP_DIMENSION("DropDimension", SQLStatementType.DDL),
    
    ALTER_DIRECTORY("AlterDirectory", SQLStatementType.DDL),
    
    DROP_DIRECTORY("DropDirectory", SQLStatementType.DDL),
    
    CREATE_EVENT("CreateEvent", SQLStatementType.DDL),
    
    CREATE_EDITION("CreateEdition", SQLStatementType.DDL),
    
    ALTER_EVENT("AlterEvent", SQLStatementType.DDL),
    
    DROP_EVENT("DropEvent", SQLStatementType.DDL),
    
    ALTER_INSTANCE("AlterInstance", SQLStatementType.DDL),
    
    CREATE_LOGFILE_GROUP("CreateLogfileGroup", SQLStatementType.DDL),
    
    ALTER_LOGFILE_GROUP("AlterLogfileGroup", SQLStatementType.DDL),
    
    DROP_LOGFILE_GROUP("DropLogfileGroup", SQLStatementType.DDL),
    
    CREATE_SERVER("CreateServer", SQLStatementType.DDL),
    
    CREATE_SYNONYM("CreateSynonym", SQLStatementType.DDL),
    
    DROP_SYNONYM("DropSynonym", SQLStatementType.DDL),
    
    CREATE_DIRECTORY("CreateDirectory", SQLStatementType.DDL),
    
    ALTER_SERVER("AlterServer", SQLStatementType.DDL),
    
    ALTER_STATISTICS("AlterStatistics", SQLStatementType.DDL),
    
    ALTER_SESSION("AlterSession", SQLStatementType.DDL),
    
    ALTER_SYSTEM("AlterSystem", SQLStatementType.DDL),
    
    DROP_SERVER("DropServer", SQLStatementType.DDL),
    
    CREATE_TRIGGER("CreateTrigger", SQLStatementType.DDL),
    
    ALTER_TRIGGER("AlterTrigger", SQLStatementType.DDL),
    
    DROP_TRIGGER("DropTrigger", SQLStatementType.DDL),
    
    DROP_EVENT_TRIGGER("DropEventTrigger", SQLStatementType.DDL),
    
    CREATE_VIEW("CreateView", SQLStatementType.DDL),
    
    ALTER_VIEW("AlterView", SQLStatementType.DDL),
    
    DROP_PACKAGE("DropPackage", SQLStatementType.DDL),
    
    ALTER_PACKAGE("AlterPackage", SQLStatementType.DDL),
    
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
    
    DROP_TEXT_SEARCH("DropTextSearch", SQLStatementType.DDL),
    
    ASSOCIATE_STATISTICS("AssociateStatistics", SQLStatementType.DDL),
    
    DISASSOCIATE_STATISTICS("DisassociateStatistics", SQLStatementType.DDL),
    
    AUDIT("Audit", SQLStatementType.DDL),
    
    NOAUDIT("NoAudit", SQLStatementType.DDL),
    
    COMMENT("Comment", SQLStatementType.DDL),
    
    FLASHBACK_DATABASE("FlashbackDatabase", SQLStatementType.DDL),
    
    FLASHBACK_TABLE("FlashbackTable", SQLStatementType.DDL),
    
    PURGE("Purge", SQLStatementType.DDL),
    
    RENAME("Rename", SQLStatementType.DDL),
    
    ALTER_ROUTINE("AlterRoutine", SQLStatementType.DDL),
    
    CREATE_EXTENSION("CreateExtension", SQLStatementType.DDL),
    
    ALTER_EXTENSION("AlterExtension", SQLStatementType.DDL),
    
    DROP_EXTENSION("DropExtension", SQLStatementType.DDL),
    
    ALTER_RULE("AlterRule", SQLStatementType.DDL),
    
    DECLARE("Declare", SQLStatementType.DDL),
    
    DISCARD("Discard", SQLStatementType.DDL),
    
    LISTEN("Listen", SQLStatementType.DDL),
    
    NOTIFY("NotifyStmt", SQLStatementType.DDL),
    
    REFRESH_MATERIALIZED_VIEW("RefreshMatViewStmt", SQLStatementType.DDL),
    
    REINDEX("Reindex", SQLStatementType.DDL),
    
    SECURITY_LABEL("SecurityLabelStmt", SQLStatementType.DDL),
    
    UNLISTEN("Unlisten", SQLStatementType.DDL),
    
    RESUME_JOB("ResumeJob", SQLStatementType.DDL),
    
    ALTER_CATALOG("AlterCatalog", SQLStatementType.DDL),
    
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
    
    COMMIT_PREPARED("CommitPrepared", SQLStatementType.TCL),
    
    ROLLBACK_PREPARED("RollbackPrepared", SQLStatementType.TCL),
    
    LOCK("Lock", SQLStatementType.LCL),
    
    UNLOCK("Unlock", SQLStatementType.LCL),
    
    GRANT("Grant", SQLStatementType.DCL),
    
    GRANT_ROLE_OR_PRIVILEGE_TO("GrantRoleOrPrivilegeTo", SQLStatementType.DCL),
    
    GRANT_ROLE_OR_PRIVILEGE_ON_TO("GrantRoleOrPrivilegeOnTo", SQLStatementType.DCL),
    
    GRANT_PROXY("GrantProxy", SQLStatementType.DCL),
    
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
    
    REVERT("Revert", SQLStatementType.DCL),
    
    USE("Use", SQLStatementType.DAL),
    
    DESC("Desc", SQLStatementType.DAL),
    
    DESCRIBE("Describe", SQLStatementType.DAL),
    
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
    
    ALTER_RESOURCE_COST("AlterResourceCost", SQLStatementType.DAL),
    
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
    
    CHECKPOINT("Checkpoint", SQLStatementType.DAL),
    
    DROP_RESOURCE_GROUP("DropResourceGroup", SQLStatementType.DAL),
    
    ALTER_RESOURCE_GROUP("AlterResourceGroup", SQLStatementType.DAL),
    
    ALTER_RESOURCE("AlterResource", SQLStatementType.DAL),
    
    DELIMITER("Delimiter", SQLStatementType.DAL),
    
    CALL("Call", SQLStatementType.DML),
    
    IMPORT_STATEMENT("ImportStatement", SQLStatementType.DML),
    
    LOAD_STATEMENT("LoadStatement", SQLStatementType.DML),
    
    CHANGE_MASTER("ChangeMasterTo", SQLStatementType.DAL),
    
    CHANGE_REPLICATION_SOURCE_TO("ChangeReplicationSourceTo", SQLStatementType.DAL),
    
    START_SLAVE("StartSlave", SQLStatementType.DAL),
    
    STOP_SLAVE("StopSlave", SQLStatementType.DAL),
    
    XA_BEGIN("XaBegin", SQLStatementType.TCL),
    
    XA_PREPARE("XaPrepare", SQLStatementType.TCL),
    
    XA_COMMIT("XaCommit", SQLStatementType.TCL),
    
    XA_ROLLBACK("XaRollback", SQLStatementType.TCL),
    
    XA_END("XaEnd", SQLStatementType.TCL),
    
    XA_RECOVERY("XaRecovery", SQLStatementType.TCL),
    
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
    
    CREATE_CAST("CreateCast", SQLStatementType.DDL),
    
    CREATE_CLUSTER("CreateCluster", SQLStatementType.DDL),
    
    CREATE_TYPE("CreateType", SQLStatementType.DDL),
    
    DROP_CONVERSION("DropConversion", SQLStatementType.DDL),
    
    ALTER_DOMAIN("AlterDomain", SQLStatementType.DDL),
    
    ALTER_POLICY("AlterPolicy", SQLStatementType.DDL),
    
    ALTER_CONVERSION("AlterConversion", SQLStatementType.DDL),
    
    CREATE_TEXT_SEARCH("CreateTextSearch", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_CONFIGURATION("AlterTextSearchConfiguration", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_DICTIONARY("AlterTextSearchDictionary", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_TEMPLATE("AlterTextSearchTemplate", SQLStatementType.DDL),
    
    ALTER_TEXT_SEARCH_PARSER("AlterTextSearchParser", SQLStatementType.DDL),
    
    DROP_POLICY("DropPolicy", SQLStatementType.DDL),
    
    DROP_OWNED("DropOwned", SQLStatementType.DDL),
    
    DROP_OPERATOR("DropOperator", SQLStatementType.DDL),
    
    DROP_MATERIALIZED_VIEW("DropMaterializedView", SQLStatementType.DDL),
    
    DROP_AGGREGATE("DropAggregate", SQLStatementType.DDL),
    
    DROP_COLLATION("DropCollation", SQLStatementType.DDL),
    
    DROP_FOREIGN_DATA_WRAPPER("DropForeignDataWrapper", SQLStatementType.DDL),
    
    DROP_TYPE("DropType", SQLStatementType.DDL),
    
    DROP_OPERATOR_CLASS("DropOperatorClass", SQLStatementType.DDL),
    
    DROP_OPERATOR_FAMILY("DropOperatorFamily", SQLStatementType.DDL),
    
    DROP_ACCESS_METHOD("DropAccessMethod", SQLStatementType.DDL),
    
    DROP_OUTLINE("DropOutline", SQLStatementType.DDL),
    
    ALTER_OUTLINE("AlterOutline", SQLStatementType.DDL),
    
    CREATE_OUTLINE("CreateOutline", SQLStatementType.DDL),
    
    ALTER_ANALYTIC_VIEW("AlterAnalyticView", SQLStatementType.DDL),
    
    DROP_EDITION("DropEdition", SQLStatementType.DDL),
    
    ALTER_ATTRIBUTE_DIMENSION("AlterAttributeDimension", SQLStatementType.DDL),
    
    CREATE_CONTEXT("CreateContext", SQLStatementType.DDL),
    
    CREATE_SPFILE("CreateSPFile", SQLStatementType.DDL),
    
    CREATE_PFILE("CreatePFile", SQLStatementType.DDL),
    
    CREATE_CONTROL_FILE("CreateControlFile", SQLStatementType.DDL),
    
    CREATE_FLASHBACK_ARCHIVE("CreateFlashbackArchive", SQLStatementType.DDL),
    
    ALTER_FLASHBACK_ARCHIVE("AlterFlashbackArchive", SQLStatementType.DDL),
    
    DROP_FLASHBACK_ARCHIVE("DropFlashbackArchive", SQLStatementType.DDL),
    
    CREATE_DISKGROUP("CreateDiskgroup", SQLStatementType.DDL),
    
    DROP_DISKGROUP("DropDiskgroup", SQLStatementType.DDL),
    
    CREATE_ROLLBACK_SEGMENT("CreateRollbackSegment", SQLStatementType.DDL),
    
    DROP_ROLLBACK_SEGMENT("DropRollbackSegment", SQLStatementType.DDL),
    
    CREATE_LOCKDOWN_PROFILE("CreateLockdownProfile", SQLStatementType.DDL),
    
    DROP_LOCKDOWN_PROFILE("DropLockdownProfile", SQLStatementType.DDL),
    
    CREATE_INMEMORY_JOIN_GROUP("CreateInmemoryJoinGroup", SQLStatementType.DDL),
    
    ALTER_INMEMORY_JOIN_GROUP("AlterInmemoryJoinGroup", SQLStatementType.DDL),
    
    DROP_INMEMORY_JOIN_GROUP("DropInmemoryJoinGroup", SQLStatementType.DDL),
    
    CREATE_RESTORE_POINT("CreateRestorePoint", SQLStatementType.DDL),
    
    DROP_RESTORE_POINT("DropRestorePoint", SQLStatementType.DDL),
    
    DROP_TABLE_SPACE("DropTableSpace", SQLStatementType.DDL),
    
    ALTER_LIBRARY("AlterLibrary", SQLStatementType.DDL),
    
    ALTER_MATERIALIZED_ZONEMAP("AlterMaterializedZonemap", SQLStatementType.DDL),
    
    ALTER_JAVA("AlterJava", SQLStatementType.DDL),
    
    ALTER_AUDIT_POLICY("AlterAuditPolicy", SQLStatementType.DDL),
    
    ALTER_CLUSTER("AlterCluster", SQLStatementType.DDL),
    
    ALTER_DISKGROUP("AlterDiskgroup", SQLStatementType.DDL),
    
    ALTER_HIERARCHY("AlterHierarchy", SQLStatementType.DDL),
    
    ALTER_INDEX_TYPE("AlterIndexType", SQLStatementType.DDL),
    
    ALTER_LOCKDOWN_PROFILE("AlterLockdownProfile", SQLStatementType.DDL),
    
    CURSOR("Cursor", SQLStatementType.DDL),
    
    CLOSE("Close", SQLStatementType.DDL),
    
    MOVE("Move", SQLStatementType.DDL),
    
    FETCH("Fetch", SQLStatementType.DDL),
    
    CLUSTER("Cluster", SQLStatementType.DDL),
    
    CREATE_ACCESS_METHOD("CreateAccessMethod", SQLStatementType.DDL),
    
    DO("DoStatement", SQLStatementType.DML),
    
    PREPARE_TRANSACTION("PrepareTransaction", SQLStatementType.TCL),
    
    REASSIGN_OWNED("ReassignOwned", SQLStatementType.DCL),
    
    CREATE_COLLATION("CreateCollation", SQLStatementType.DDL),
    
    CREATE_EVENT_TRIGGER("CreateEventTrigger", SQLStatementType.DDL),
    
    CREATE_FOREIGN_DATA_WRAPPER("CreateForeignDataWrapper", SQLStatementType.DDL),
    
    CREATE_FOREIGN_TABLE("CreateForeignTable", SQLStatementType.DDL),
    
    CREATE_GROUP("CreateGroup", SQLStatementType.DCL),
    
    CREATE_MATERIALIZED_VIEW("CreateMaterializedView", SQLStatementType.DDL),
    
    CREATE_MACRO("CreateMacro", SQLStatementType.DDL),
    
    DROP_MACRO("DropMacro", SQLStatementType.DDL),
    
    CREATE_MATERIALIZED_VIEW_LOG("CreateMaterializedViewLog", SQLStatementType.DDL),
    
    CREATE_OPERATOR("CreateOperator", SQLStatementType.DDL),
    
    CREATE_POLICY("CreatePolicy", SQLStatementType.DDL),
    
    DROP_INDEX_TYPE("DropIndexType", SQLStatementType.DDL),
    
    DROP_PROFILE("DropProfile", SQLStatementType.DDL),
    
    DROP_PLUGGABLE_DATABASE("DropPluggableDatabase", SQLStatementType.DDL),
    
    DROP_JAVA("DropJava", SQLStatementType.DDL),
    
    DROP_LIBRARY("DropLibrary", SQLStatementType.DDL),
    
    DROP_CLUSTER("DropCluster", SQLStatementType.DDL),
    
    DROP_MATERIALIZED_VIEW_LOG("DropMaterializedViewLog", SQLStatementType.DDL),
    
    DROP_MATERIALIZED_ZONEMAP("DropMaterializedZonemap", SQLStatementType.DDL),
    
    DROP_CONTEXT("DropContext", SQLStatementType.DDL),
    
    DROP_ENCRYPT_KEY("DropEncryptKey", SQLStatementType.DDL),
    
    SYSTEM_ACTION("SystemAction", SQLStatementType.DDL),
    
    EMPTY_STATEMENT("EmptyStatement", SQLStatementType.DAL),
    
    CREATE_JAVA("CreateJava", SQLStatementType.DDL),
    
    PLSQL_BLOCK("PlsqlBlock", SQLStatementType.DDL),
    
    CREATE_LIBRARY("CreateLibrary", SQLStatementType.DDL),
    
    SWITCH("Switch", SQLStatementType.DDL),
    
    CREATE_PROFILE("CreateProfile", SQLStatementType.DDL),
    
    UPDATE_STATISTICS("UpdateStatistics", SQLStatementType.DDL),
    
    SPOOL("Spool", SQLStatementType.DAL),
    
    START_REPLICA("StartReplica", SQLStatementType.DAL),
    
    REFRESH("Refresh", SQLStatementType.DAL),
    
    OPEN("Open", SQLStatementType.DDL);
    
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
     * @throws SQLASTVisitorException SQL AST visitor exception
     */
    public static SQLVisitorRule valueOf(final Class<? extends ParseTree> parseTreeClass) {
        String parseTreeClassName = parseTreeClass.getSimpleName();
        for (SQLVisitorRule each : values()) {
            if (each.getContextName().equals(parseTreeClassName)) {
                return each;
            }
        }
        throw new SQLASTVisitorException(parseTreeClass);
    }
}
