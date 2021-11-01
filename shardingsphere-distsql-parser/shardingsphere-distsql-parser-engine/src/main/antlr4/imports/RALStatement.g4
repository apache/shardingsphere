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

grammar RALStatement;

import Keyword, Literals, Symbol;

setVariable
    : SET VARIABLE variableName EQ variableValue
    ;

showVariable
    : SHOW VARIABLE variableName
    ;

showAllVariables
    : SHOW ALL VARIABLES
    ;

enableInstance
    :ENABLE INSTANCE IP EQ ip COMMA PORT EQ port
    ;

disableInstance
    :DISABLE INSTANCE IP EQ ip COMMA PORT EQ port
    ;

showInstance
    : SHOW INSTANCE LIST
    ;

clearHint
    : CLEAR HINT
    ;

variableName
    : IDENTIFIER
    ;

variableValue
    : IDENTIFIER | STRING | (MINUS)? INT 
    ;

ip
    : IDENTIFIER | NUMBER+
    ;

port
    : INT
    ;
