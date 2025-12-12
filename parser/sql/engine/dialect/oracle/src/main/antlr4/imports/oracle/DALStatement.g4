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
    : (APPI | APPINFO)
    | (ARRAY | ARRAYSIZE)
    | (AUTO | AUTOCOMMIT)
    | (AUTOP | AUTOPN)
    | AUTORECOVERY
    | (AUTOT | AUTOTRACE)
    | (BLO | BLOCKTERMINATOR)
    | (CMDS | CMDSEP)
    | (COLINVI | COLINVISIBLE)
    | COLSEP
    | (CON | CONCAT)
    | (COPYC | COPYCOMMIT)
    | COPYTYPECHECK
    | (DEF | DEFINE)
    | DESCRIBE
    | ECHO
    | (EDITF | EDITFILE)
    | (EMB | EMBEDDED)
    | ERRORDETAILS
    | (ERRORL | ERRORLOGGING)
    | (ESC | ESCAPE)
    | ESCCHAR
    | (EXITCOMMIT | OMMIT)
    | FEEDBACK
    | FLAGGER
    | (FLU | FLUSH)
    | (HEADING | HEA)
    | (HISTORY | HIST)
    | INSTANCE
    | JSONPRINT
    | (LINESIZE | LIN)
    | (LOBOFFSET | LOBOF)
    | LOGSOURCE
    | LONG
    | LONGCHUNKSIZE
    | (MARK | MARKUP)
    | (NEWPAGE | NEWP)
    | NULL
    | (NUMFORMAT | NUMF)
    | (NUMWIDTH | NUM)
    | (PAGESIZE | PAGES)
    | (PAUSE | PAU)
    | RECSEP
    | RECSEPCHAR
    | ROWLIMIT
    | ROWPREFETCH
    | SECUREDCOL
    | (SERVEROUTPUT | SERVEROUT)
    | (SHIFTINOUT | SHIFT)
    | (SHOWMODE | SHOW)
    | (SQLBLANKLINES | SQLBL)
    | (SQLCASE | SQLC)
    | (SQLCONTINUE | SQLCO)
    | (SQLNUMBER | SQLN)
    | (SQLPLUSCOMPATIBILITY | SQLPLUSCOMPAT)
    | (SQLPREFIX | SQLPRE)
    | (SQLPROMPT | SQLP)
    | (SQLTERMINATOR | SQLT)
    | (STATEMENTCACHE | STATEMENTC)
    | (SUFFIX | SUF)
    | TAB
    | (TERMOUT | TERM)
    | (TIME | TI)
    | (TIMING | TIMI)
    | (TRIMOUT | TRIM)
    | (TRIMSPOOL | TRIMS)
    | (UNDERLINE | UND)
    | (VERIFY | VER)
    | (WRAP | WRA)
    | (XMLOPTIMIZATIONCHECK | XMLOPT)
    | XQUERY
    ;

showOptions
    : systemVariable
    | ALL
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
