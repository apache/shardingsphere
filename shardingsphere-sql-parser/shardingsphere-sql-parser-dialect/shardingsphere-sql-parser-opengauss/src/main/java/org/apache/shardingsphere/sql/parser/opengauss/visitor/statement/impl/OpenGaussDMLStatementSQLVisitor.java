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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CopyContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussCallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussCopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussDoStatement;

import java.util.Properties;

/**
 * DML Statement SQL visitor for openGauss.
 */
@NoArgsConstructor
public final class OpenGaussDMLStatementSQLVisitor extends OpenGaussStatementSQLVisitor implements DMLSQLVisitor, SQLStatementVisitor {
    
    public OpenGaussDMLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        return new OpenGaussCallStatement();
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        return new OpenGaussDoStatement();
    }
    
    @Override
    public ASTNode visitCopy(final CopyContext ctx) {
        return new OpenGaussCopyStatement();
    }
}
