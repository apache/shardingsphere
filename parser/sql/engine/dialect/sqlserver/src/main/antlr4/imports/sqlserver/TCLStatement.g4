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
    : SET TRANSACTION ISOLATION LEVEL isolationLevel
    ;

isolationLevel
    : READ UNCOMMITTED | READ COMMITTED | REPEATABLE READ | SNAPSHOT | SERIALIZABLE
    ;

setImplicitTransactions
    : SET IMPLICIT_TRANSACTIONS implicitTransactionsValue
    ;

implicitTransactionsValue
    : ON | OFF
    ;

beginTransaction
    : BEGIN (TRAN | TRANSACTION) ((transactionName | transactionVariableName) (WITH MARK (stringLiterals | NCHAR_TEXT)?)?)?
    ;

commit
    : COMMIT ((TRAN | TRANSACTION) (transactionName | transactionVariableName)?)? (WITH LP_ DELAYED_DURABILITY EQ_ (OFF | ON) RP_)?
    ;

commitWork
    : COMMIT WORK?
    ;

rollback
    : ROLLBACK (TRAN | TRANSACTION) (transactionName | transactionVariableName | savepointName | savepointVariableName)?
    ;

rollbackWork
    : ROLLBACK WORK?
    ;

savepoint
    : SAVE (TRAN | TRANSACTION) (savepointName | savepointVariableName)
    ;

beginDistributedTransaction
    : BEGIN DISTRIBUTED (TRAN | TRANSACTION) (transactionName | transactionVariableName)?
    ;
