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

grammar RDLStatement;

import Keyword, Literals, Symbol;

addResource
    : ADD RESOURCE dataSource (COMMA dataSource)*
    ;

dropResource
    : DROP RESOURCE IDENTIFIER (COMMA IDENTIFIER)*
    ;

dataSource
    : dataSourceName LP HOST EQ hostName COMMA PORT EQ port COMMA DB EQ dbName COMMA USER EQ user (COMMA PASSWORD EQ password)? RP
    ;

dataSourceName
    : IDENTIFIER
    ;

hostName
    : IDENTIFIER | ip
    ;

ip
    : NUMBER+
    ;

port
    : INT
    ;

dbName
    : IDENTIFIER
    ;

user
    : IDENTIFIER | NUMBER
    ;

password
    : IDENTIFIER | INT | STRING
    ;
