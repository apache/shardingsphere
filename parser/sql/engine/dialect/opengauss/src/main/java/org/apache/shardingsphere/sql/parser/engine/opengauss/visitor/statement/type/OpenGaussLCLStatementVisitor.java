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
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RelationExprContext;
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.LockStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * LCL statement visitor for openGauss.
 */
public final class OpenGaussLCLStatementVisitor extends OpenGaussStatementVisitor implements LCLStatementVisitor {
    
    public OpenGaussLCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        return new LockStatement(getDatabaseType(), null == ctx.relationExprList() ? Collections.emptyList() : getLockTables(ctx.relationExprList().relationExpr()));
    }
    
    private Collection<SimpleTableSegment> getLockTables(final Collection<RelationExprContext> relationExprContexts) {
        return relationExprContexts.stream().map(each -> (SimpleTableSegment) visit(each.qualifiedName())).collect(Collectors.toList());
    }
}
