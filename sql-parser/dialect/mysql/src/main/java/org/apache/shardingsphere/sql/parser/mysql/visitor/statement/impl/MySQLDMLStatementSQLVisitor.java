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
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadDataStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadXmlStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLCallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLLoadXMLStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * DML Statement SQL visitor for MySQL.
 */
@NoArgsConstructor
public final class MySQLDMLStatementSQLVisitor extends MySQLStatementSQLVisitor implements DMLSQLVisitor, SQLStatementVisitor {
    
    public MySQLDMLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        List<ExpressionSegment> params = new ArrayList<>();
        ctx.expr().forEach(each -> params.add((ExpressionSegment) visit(each)));
        return new MySQLCallStatement(ctx.identifier().getText(), params);
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        List<ExpressionSegment> params = new ArrayList<>();
        ctx.expr().forEach(each -> params.add((ExpressionSegment) visit(each)));
        return new MySQLDoStatement(params);
    }
    
    @Override
    public ASTNode visitLoadStatement(final LoadStatementContext ctx) {
        return null != ctx.loadDataStatement() ? visit(ctx.loadDataStatement()) : visit(ctx.loadXmlStatement());
    }
    
    @Override
    public ASTNode visitLoadDataStatement(final LoadDataStatementContext ctx) {
        return new MySQLLoadDataStatement((SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitLoadXmlStatement(final LoadXmlStatementContext ctx) {
        return new MySQLLoadXMLStatement((SimpleTableSegment) visit(ctx.tableName()));
    }
}
