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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetConstraintsContext;
import org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.LockStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetConstraintsStatement;

import java.util.Collections;

/**
 * LCL statement visitor for Oracle.
 */
public final class OracleLCLStatementVisitor extends OracleStatementVisitor implements LCLStatementVisitor {
    
    public OracleLCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitSetConstraints(final SetConstraintsContext ctx) {
        return new SetConstraintsStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        return new LockStatement(getDatabaseType(), Collections.emptyList());
    }
}
