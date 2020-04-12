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

grammar RLStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

changeMasterTo
    : CHANGE MASTER TO (identifier EQ_ (identifier | ignoredIdentifiers_))+  (FOR CHANNEL identifier)?
    ;

startSlave
    : START SLAVE threadTypes_ utilOption_* connectionOptions_* channelOption_*
    ;

stopSlave
    : STOP SLAVE threadTypes_ channelOption_*
    ;

threadTypes_
    : threadType_*
    ;

threadType_
    : (IO_THREAD | SQL_THREAD)
    ;

utilOption_
    : UNTIL ((SQL_BEFORE_GTIDS | SQL_AFTER_GTIDS) EQ_ identifier
    | MASTER_LOG_FILE EQ_ identifier COMMA_ MASTER_LOG_POS EQ_ identifier
    | RELAY_LOG_FILE EQ_ identifier COMMA_ RELAY_LOG_POS  EQ_ identifier
    | SQL_AFTER_MTS_GAPS)
    ;

connectionOptions_
    : USER EQ_ identifier | PASSWORD EQ_ identifier | DEFAULT_AUTH EQ_ identifier | PLUGIN_DIR EQ_ identifier
    ;

channelOption_
    : FOR CHANNEL identifier
    ;
