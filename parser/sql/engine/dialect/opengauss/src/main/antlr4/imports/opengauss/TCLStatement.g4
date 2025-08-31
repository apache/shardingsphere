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

parser grammar TCLStatement;

import DMLStatement;

options {
    tokenVocab = ModeLexer;
}

setTransaction
    : SET (SESSION CHARACTERISTICS AS)? TRANSACTION transactionModeList
    | SET TRANSACTION SNAPSHOT STRING_
    ;

beginTransaction
    : BEGIN (WORK | TRANSACTION)? transactionModeList?
    ;

commit
    : COMMIT (WORK | TRANSACTION)? (AND (NO)? CHAIN)?
    ;

savepoint
    : SAVEPOINT colId
    ;

abort
    : ABORT (WORK | TRANSACTION)? (AND (NO)? CHAIN)?
    ;

startTransaction
    : START TRANSACTION transactionModeList?
    ;

end
    : END (WORK | TRANSACTION)? (AND (NO)? CHAIN)?
    ;

rollback
    : ROLLBACK (WORK | TRANSACTION)? (AND (NO)? CHAIN)?
    ;

releaseSavepoint
    : RELEASE SAVEPOINT? colId
    ;

rollbackToSavepoint
    : ROLLBACK (WORK | TRANSACTION)? TO SAVEPOINT? colId
    ;

prepareTransaction
    : PREPARE TRANSACTION gid
    ;

commitPrepared
    : COMMIT PREPARED gid
    ;

rollbackPrepared
    : ROLLBACK PREPARED gid
    ;

gid
    : STRING_
    ;

setConstraints
    : SET CONSTRAINTS constraintsSetList constraintsSetMode
    ;

constraintsSetMode
    : DEFERRED | IMMEDIATE
    ;

constraintsSetList
    : ALL | qualifiedNameList
    ;

lock
    : LOCK TABLE? relationExprList (IN lockType MODE)? NOWAIT?
    ;

lockType
    : ACCESS SHARE
    | ROW SHARE
    | ROW EXCLUSIVE
    | SHARE UPDATE EXCLUSIVE
    | SHARE
    | SHARE ROW EXCLUSIVE
    | EXCLUSIVE
    | ACCESS EXCLUSIVE
    ;
