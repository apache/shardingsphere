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

grammar DALStatement;

import BaseRule, DMLStatement;

show
    : showDatabases
    | showConnectors
    | showTables
    | showViews
    | showMaterializedViews
    | showPartitions
    | showTablesExtended
    | showTblproperties
    | showCreateTable
    | showIndex
    | showColumns
    | showFunctions
    | showGrantedRolesAndPrivileges
    | showLocks
    | showConf
    | showTransactions
    | showCompactions
    ;

describe
    : describeDatabase
    | describeConnector
    | describeTable
    ;

showDatabases
    : SHOW (DATABASES|SCHEMAS) showLike?
    ;

showConnectors
    : SHOW CONNECTORS
    ;

showTables
    : SHOW TABLES (IN databaseName)? stringLiterals?
    ;

showViews
    : SHOW VIEWS showFrom? showLike?
    ;

showMaterializedViews
    : SHOW MATERIALIZED VIEWS showFrom? showLike?
    ;

showPartitions
    : SHOW PARTITIONS tableName partitionSpec? whereClause? orderByClause? limitClause?
    ;

showTablesExtended
    : SHOW TABLE EXTENDED showFrom? showLike partitionSpec?
    ;

showTblproperties
    : SHOW TBLPROPERTIES tableName
    | SHOW TBLPROPERTIES tableName LP_ string_ RP_
    ;

showCreateTable
    : SHOW CREATE TABLE (tableName | viewName)
    ;

showIndex
    : SHOW FORMATTED? (INDEX | INDEXES) ON tableName showFrom?
    ;

showColumns
    : SHOW COLUMNS (FROM | IN) tableName showFrom? showLike?
    ;

showFunctions
    : SHOW FUNCTIONS showLike?
    ;

showGrantedRolesAndPrivileges
    : SHOW ROLE GRANT
    | SHOW GRANT
    | SHOW CURRENT ROLES
    | SHOW ROLES
    | SHOW PRINCIPALS
    ;

showLocks
    : SHOW LOCKS tableName
    | SHOW LOCKS tableName EXTENDED
    | SHOW LOCKS tableName partitionSpec
    | SHOW LOCKS tableName partitionSpec EXTENDED
    | SHOW LOCKS (DATABASE | SCHEMA) databaseName
    ;

showConf
    : SHOW CONF configurationName
    ;

showTransactions
    : SHOW TRANSACTIONS
    ;

showCompactions
    : SHOW COMPACTIONS (DATABASE | SCHEMA) databaseName
    | SHOW COMPACTIONS tableName? partitionSpec? (POOL stringLiterals)? (TYPE stringLiterals)? (STATE stringLiterals)? orderByClause? limitClause?
    | SHOW COMPACTIONS COMPACTIONID EQ_ NUMBER_
    ;

showFrom
    : (IN | FROM) databaseName
    ;

showLike
    : LIKE stringLiterals
    ;

describeDatabase
    : DESCRIBE (DATABASE | SCHEMA) EXTENDED? databaseName
    ;

describeConnector
    : DESCRIBE CONNECTOR EXTENDED? connectorName
    ;

describeTable
    : DESCRIBE (EXTENDED | FORMATTED)? tableName partitionSpec? columnClause?
    ;

columnClause
    : columnName columnOptions
    ;

columnOptions
    : columnOption*
    ;

columnOption
    : DOT_ identifier
    | DOT_ string_
    ;
