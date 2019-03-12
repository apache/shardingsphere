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

grammar MySQLBase;

import MySQLKeyword, Keyword, Symbol, BaseRule, DataType;

alias
    : ID | PASSWORD | STRING_
    ;

tableName
    : ID | ID DOT_ASTERISK_ | ASTERISK_
    ;

assignmentValueList
    : LP_ assignmentValues RP_
    ;

assignmentValues
    : assignmentValue (COMMA_ assignmentValue)*
    ;

assignmentValue
    : DEFAULT | MAXVALUE | expr
    ;

functionCall
    : (ID | DATE) LP_ distinct? (exprs | ASTERISK_)? RP_ | groupConcat | windowFunction
    ;

groupConcat
    : GROUP_CONCAT LP_ distinct? (exprs | ASTERISK_)? (orderByClause (SEPARATOR expr)?)? RP_
    ;

windowFunction
    : ID exprList overClause
    ;

overClause
    : OVER LP_ windowSpec RP_ | OVER ID
    ;

windowSpec
    : ID? windowPartitionClause? orderByClause? frameClause?
    ;

windowPartitionClause
    : PARTITION BY exprs
    ;

frameClause
    : frameUnits frameExtent
    ;

frameUnits
    : ROWS | RANGE
    ;

frameExtent
    : frameStart | frameBetween
    ;

frameStart
    : CURRENT ROW
    | UNBOUNDED PRECEDING
    | UNBOUNDED FOLLOWING
    | expr PRECEDING
    | expr FOLLOWING
    ;

frameBetween
    : BETWEEN frameStart AND frameEnd
    ;

frameEnd
    : frameStart
    ;

variable
    : (AT_ AT_)? (GLOBAL | PERSIST | PERSIST_ONLY | SESSION)? DOT_? ID
    ;

assignmentList
    : assignment (COMMA_ assignment)*
    ;

assignment
    : columnName EQ_ assignmentValue
    ;

tableReferences
    : matchNone
    ;

whereClause
    : WHERE expr
    ;
