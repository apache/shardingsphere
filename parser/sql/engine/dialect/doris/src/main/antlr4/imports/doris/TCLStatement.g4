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

import BaseRule;

setTransaction
    : SET optionType? TRANSACTION transactionCharacteristics
    ;

setAutoCommit
    : SET (AT_? AT_)? optionType? DOT_? AUTOCOMMIT EQ_ autoCommitValue=(NUMBER_ | ON | OFF)
    ;

beginTransaction
    : BEGIN WORK? | START TRANSACTION (transactionCharacteristic (COMMA_ transactionCharacteristic)*)?
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

releaseSavepoint
    : RELEASE SAVEPOINT identifier
    ;

optionChain
    : AND NO? CHAIN
    ;

optionRelease
    : NO? RELEASE
    ;

xaBegin
    : XA (START | BEGIN) xid (JOIN | RESUME)?
    ;

xaPrepare
    : XA PREPARE xid
    ;

xaCommit
    : XA COMMIT xid (ONE PHASE)?
    ;

xaRollback
    : XA ROLLBACK xid
    ;

xaEnd
    : XA END xid (SUSPEND (FOR MIGRATE)?)?
    ;

xaRecovery
    : XA RECOVER (CONVERT XID)?
    ;

xid
    : gtrid=textString (COMMA_ bqual=textString (COMMA_ formatID=NUMBER_)?)?
    ;
