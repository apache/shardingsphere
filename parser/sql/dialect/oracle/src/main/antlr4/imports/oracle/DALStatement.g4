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

alterResourceCost
    : ALTER RESOURCE COST ((CPU_PER_SESSION | CONNECT_TIME | LOGICAL_READS_PER_SESSION | PRIVATE_SGA) INTEGER_)+
    ;

dbLinkInfo
    : databaseName (DOT_ domain)* (AT_ connectionQualifier)?
    ;

explain
    : EXPLAIN PLAN (SET STATEMENT_ID EQ_ stringLiterals)? (INTO (schemaName DOT_)? tableName (AT_ dbLinkInfo)? )? FOR (insert | delete | update | select)
    ;

schema
    : identifier
    ;

parameterName
    : identifier
    ;

originalName
    : identifier
    ;

systemVariable
    : identifier
    ;

showOptions
// TODO refactor systemVariable sytax according to https://docs.oracle.com/en/database/oracle/oracle-database/23/sqpug/SET-system-variable-summary.html#GUID-A6A5ADFF-4119-4BA4-A13E-BC8D29166FAE
//    : systemVariable
    : ALL
    | CON_ID
    | CON_NAME
    | EDITION
    | (BTI | BTITLE)
    | (ERR | ERRORS) ((ANALYTIC VIEW | ATTRIBUTE DIMENSION | HIERARCHY | FUNCTION | PROCEDURE | PACKAGE | PACKAGE BODY | TRIGGER  | VIEW | TYPE | TYPE BODY | DIMENSION | JAVA CLASS) (schema DOT_)? name)?
    | HISTORY
    | LNO
    | LOBPREFETCH
    | (PARAMETER | PARAMETERS) parameterName?
    | PDBS
    | PNO
    | (RECYC | RECYCLEBIN) originalName?
    | (REL | RELEASE)
    | (REPF | REPFOOTER)
    | (REPH | REPHEADER)
    | (ROWPREF | ROWPREFETCH)
    | SGA
    | (SPOO | SPOOL)
    | (SPPARAMETER | SPPARAMETERS) parameterName?
    | SQLCODE
    | (STATEMENTC | STATEMENTCACHE)
    | (TTI | TLE)
    | USER
    | XQUERY
    ;

show
    : (SHO | SHOW) showOptions
    ;

fileExt
    : DOT_ identifier
    ;

spoolFileName
    : identifier fileExt?
    ;

spool
    : (SPOOL | SPO) (spoolFileName (CRE | CREATE | REP | REPLACE | APP | APPEND)?) | OFF | OUT
    ;
