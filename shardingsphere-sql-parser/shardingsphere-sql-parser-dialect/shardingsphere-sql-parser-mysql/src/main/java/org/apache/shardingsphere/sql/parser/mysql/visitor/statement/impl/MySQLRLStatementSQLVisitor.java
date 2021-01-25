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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.type.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeMasterToContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StartSlaveContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StopSlaveContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.rl.MySQLChangeMasterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.rl.MySQLStartSlaveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.rl.MySQLStopSlaveStatement;

import java.util.Properties;

/**
 * RL Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLRLStatementSQLVisitor extends MySQLStatementSQLVisitor implements RLSQLVisitor, SQLStatementVisitor {
    
    public MySQLRLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override public ASTNode visitChangeMasterTo(final ChangeMasterToContext ctx) {
        return new MySQLChangeMasterStatement();
    }
    
    @Override
    public ASTNode visitStartSlave(final StartSlaveContext ctx) {
        return new MySQLStartSlaveStatement();
    }
    
    @Override
    public ASTNode visitStopSlave(final StopSlaveContext ctx) {
        return new MySQLStopSlaveStatement();
    }
}
