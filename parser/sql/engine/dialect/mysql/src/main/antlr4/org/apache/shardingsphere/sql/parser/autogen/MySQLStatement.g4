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

grammar MySQLStatement;

import Comments, DDLStatement, TCLStatement, LCLStatement, DCLStatement;

@lexer::members {
    private int previousTokenType;
    private boolean javaScriptRoutine;

    private void adjustNumberToken() {
        if ('.' == _input.LA(_tokenStartCharIndex - _input.index())) {
            int previousCharacter = _input.LA(_tokenStartCharIndex - _input.index() - 1);
            int nextCharacter = _input.LA(1);
            if ((Character.isLetterOrDigit(previousCharacter) || '_' == previousCharacter || '$' == previousCharacter || previousCharacter >= 0x80 && previousCharacter <= 0xFFFF)
                    && (Character.isLetter(nextCharacter) || '_' == nextCharacter || '$' == nextCharacter || nextCharacter >= 0x80 && nextCharacter <= 0xFFFF)) {
                _input.seek(_tokenStartCharIndex + 1);
                setCharPositionInLine(_tokenStartCharPositionInLine + 1);
                setType(DOT_);
            }
        }
    }

    @Override
    public org.antlr.v4.runtime.Token emit() {
        org.antlr.v4.runtime.Token result = super.emit();
        if (org.antlr.v4.runtime.Token.DEFAULT_CHANNEL == result.getChannel()) {
            if (JAVASCRIPT == result.getType() && LANGUAGE == previousTokenType) {
                javaScriptRoutine = true;
            } else if (SEMI_ == result.getType() || DOLLAR_QUOTED_TEXT == result.getType()) {
                javaScriptRoutine = false;
            }
            previousTokenType = result.getType();
        }
        return result;
    }
}

execute
    : (select
    | insert
    | update
    | delete
    | replace
    | binlog
    | createTable
    | alterStatement
    | repairTable
    | dropTable
    | truncateTable
    | createIndex
    | dropIndex
    | createProcedure
    | dropProcedure
    | createFunction
    | dropFunction
    | createDatabase
    | dropDatabase
    | createEvent
    | dropEvent
    | createLogfileGroup
    | dropLogfileGroup
    | createServer
    | dropServer
    | createView
    | dropView
    | createTrigger
    | dropTrigger
    | alterResourceGroup
    | createResourceGroup
    | dropResourceGroup
    | prepare
    | executeStmt
    | deallocate
    | setTransaction
    | beginTransaction
    | setAutoCommit
    | commit
    | rollback
    | savepoint
    | grant
    | revoke
    | createUser
    | dropUser
    | alterUser
    | renameUser
    | createRole
    | dropRole
    | setDefaultRole
    | setRole
    | createSRSStatement
    | dropSRSStatement
    | flush
    | getDiagnosticsStatement
    | groupReplication
    | handlerStatement
    | help
    | importStatement
    | install
    | kill
    | loadStatement
    | cacheIndex
    | loadIndexInfo
    | optimizeTable
    | purgeBinaryLog
    | releaseSavepoint
    | resetStatement
    | setPassword
    | setTransaction
    | setResourceGroup
    | resignalStatement
    | signalStatement
    | restart
    | shutdown
    | begin
    | use
    | explain
    | doStatement
    | show
    | setVariable
    | setCharacter
    | call
    | changeMasterTo
    | changeReplicationFilter
    | checkTable
    | checksumTable
    | clone
    | changeReplicationSourceTo
    | startSlave
    | stopSlave
    | analyzeTable
    | renameTable
    | uninstall
    | xaBegin
    | xaPrepare
    | xaCommit
    | xaRollback
    | xaEnd
    | xaRecovery
    | lock
    | unlock
    | createLoadableFunction
    | createTablespace
    | alterTablespace
    | dropTablespace
    | delimiter
    | startReplica
    // TODO consider refactor following sytax to SEMI_? EOF
    ) (SEMI_ EOF? | EOF)
    | EOF
    ;
