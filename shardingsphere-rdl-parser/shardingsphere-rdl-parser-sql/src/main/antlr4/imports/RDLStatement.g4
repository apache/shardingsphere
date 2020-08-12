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

createSchema
    : CREATE SCHEMA schemaName
    ;

createDatasource
    : CREATE DATASOURCE dataSource (COMMA dataSource)*
    ;

createShardingRule
    : CREATE SHARDINGRULE tableRule (COMMA tableRule)*
    ;

tableRule
    : tableName EQ tableRuleDefinition
    ;

dataSource
    : dataSourceName EQ dataSourceDefinition
    ;
       
dataSourceDefinition
    : hostName COLON port COLON dbName COLON user COLON password
    ;

tableRuleDefinition
    : strategyType LP strategyDefinition RP
    ;

strategyType
    : IDENTIFIER
    ;

strategyDefinition
    : columName COMMA strategyProps
    ;

strategyProps
    : strategyProp (COMMA strategyProp)*
    ;
    
strategyProp
    : IDENTIFIER | NUMBER | INT
    ;

dataSourceName
    : IDENTIFIER
    ;
 
schemaName
    : IDENTIFIER
    ;

tableName
    : IDENTIFIER
    ;

columName
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
    : IDENTIFIER | NUMBER | STRING
    ;
