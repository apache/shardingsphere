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

grammar TCLStatement;

import Symbol, Keyword, MySQLKeyword, Literals, BaseRule;

setTransaction
    : SET optionType? TRANSACTION transactionCharacteristics
    ;

setAutoCommit
    : SET (AT_? AT_)? optionType? DOT_? AUTOCOMMIT EQ_ autoCommitValue=(NUMBER_ | ON | OFF)
    ;

beginTransaction
    : BEGIN | START TRANSACTION (transactionCharacteristic (COMMA_ transactionCharacteristic)*)?
    ;

transactionCharacteristic
    : WITH CONSISTENT SNAPSHOT | transactionAccessMode
    ;

commit
    : COMMIT WORK? optionChain? optionRelease?
    ;

rollback
    : ROLLBACK (WORK? TO SAVEPOINT? identifier | WORK? optionChain? optionRelease?)
    ;

savepoint
    : SAVEPOINT identifier
    ;

begin
    : BEGIN WORK?
    ;

lock
    : LOCK (INSTANCE FOR BACKUP | (TABLE | TABLES) tableLock (COMMA_ tableLock)* )
    ;

unlock
    : UNLOCK (INSTANCE | TABLE | TABLES)
    ;

releaseSavepoint
    : RELEASE SAVEPOINT identifier
    ;

xa
    : XA ((START | BEGIN) xid (JOIN | RESUME)
        | END xid (SUSPEND (FOR MIGRATE)?)?
        | PREPARE xid
        | COMMIT xid (ONE PHASE)?
        | ROLLBACK xid
        | RECOVER (CONVERT xid)?
    )
    ;

optionChain
    : AND NO? CHAIN
    ;

optionRelease
    : NO? RELEASE
    ;

tableLock
    : tableName (AS? alias)? lockOption
    ;

lockOption
    : READ LOCAL? | LOW_PRIORITY? WRITE
    ;

xid
    : string_ (COMMA_ string_)* numberLiterals?
    ;
