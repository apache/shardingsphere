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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.RLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeMasterToContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeReplicationSourceToContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StartSlaveContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StopSlaveContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.ChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.ChangeReplicationSourceToStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StartReplicaStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StartSlaveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.rl.StopSlaveStatement;

/**
 * RL statement visitor for MySQL.
 */
public final class MySQLRLStatementVisitor extends MySQLStatementVisitor implements RLStatementVisitor {
    
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
    public ASTNode visitStartReplica(final MySQLStatementParser.StartReplicaContext ctx) {
        return new StartReplicaStatement();
    }
}
