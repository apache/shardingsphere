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

package org.apache.shardingsphere.sql.parser.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.RLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ChangeMasterToContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ChangeReplicationSourceToContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StartSlaveContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StopSlaveContext;
import org.apache.shardingsphere.sql.parser.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.ChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.ChangeReplicationSourceToStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StartReplicaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StopSlaveStatement;

/**
 * RL statement visitor for Doris.
 */
public final class DorisRLStatementVisitor extends DorisStatementVisitor implements RLStatementVisitor {
    
    @Override
    public ASTNode visitChangeMasterTo(final ChangeMasterToContext ctx) {
        return new ChangeMasterStatement();
    }
    
    @Override
    public ASTNode visitStartSlave(final StartSlaveContext ctx) {
        return new StartSlaveStatement();
    }
    
    @Override
    public ASTNode visitStopSlave(final StopSlaveContext ctx) {
        return new StopSlaveStatement();
    }
    
    @Override
    public ASTNode visitChangeReplicationSourceTo(final ChangeReplicationSourceToContext ctx) {
        return new ChangeReplicationSourceToStatement();
    }
    
    @Override
    public ASTNode visitStartReplica(final DorisStatementParser.StartReplicaContext ctx) {
        return new StartReplicaStatement();
    }
}
