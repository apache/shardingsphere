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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CopyContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ReturningClauseContext;
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;

import java.util.Collections;

/**
 * DML statement visitor for openGauss.
 */
public final class OpenGaussDMLStatementVisitor extends OpenGaussStatementVisitor implements DMLStatementVisitor {
    
    public OpenGaussDMLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        return new CallStatement(getDatabaseType(), null, Collections.emptyList());
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        return new DoStatement(getDatabaseType(), Collections.emptyList());
    }
    
    @Override
    public ASTNode visitCopy(final CopyContext ctx) {
        return new PostgreSQLCopyStatement(getDatabaseType(), null == ctx.qualifiedName() ? null : (SimpleTableSegment) visit(ctx.qualifiedName()), Collections.emptyList(), null);
    }
    
    @Override
    public ASTNode visitReturningClause(final ReturningClauseContext ctx) {
        return new ReturningSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ProjectionsSegment) visit(ctx.targetList()));
    }
}
