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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.impl;

import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AnalyzeContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ConfigurationParameterClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DAL Statement SQL visitor for PostgreSQL.
 */
public final class PostgreSQLDALStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DALSQLVisitor, SQLStatementVisitor {
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        return new PostgreSQLShowStatement();
    }
    
    @Override
    public ASTNode visitSet(final SetContext ctx) {
        PostgreSQLSetStatement result = new PostgreSQLSetStatement();
        Collection<VariableAssignSegment> variableAssigns = new LinkedList<>();
        if (null != ctx.configurationParameterClause()) {
            VariableAssignSegment variableAssignSegment = (VariableAssignSegment) visit(ctx.configurationParameterClause());
            if (null != ctx.runtimeScope()) {
                variableAssignSegment.getVariable().setScope(ctx.runtimeScope().getText());
            }
            variableAssigns.add(variableAssignSegment);
            result.getVariableAssigns().addAll(variableAssigns);
        }
        return result;
    }
    
    @Override
    public ASTNode visitConfigurationParameterClause(final ConfigurationParameterClauseContext ctx) {
        VariableAssignSegment result = new VariableAssignSegment();
        result.setStartIndex(ctx.start.getStartIndex());
        result.setStopIndex(ctx.stop.getStopIndex());
        VariableSegment variable = new VariableSegment();
        variable.setStartIndex(ctx.varName().start.getStartIndex());
        variable.setStopIndex(ctx.varName().stop.getStopIndex());
        variable.setVariable(ctx.varName().getText());
        result.setVariable(variable);
        if (null != ctx.varList()) {
            result.setAssignValue(ctx.varList().getText());
        }
        if (null != ctx.DEFAULT()) {
            result.setAssignValue(ctx.DEFAULT().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitResetParameter(final ResetParameterContext ctx) {
        return new PostgreSQLResetParameterStatement();
    }
    
    @Override
    public ASTNode visitAnalyze(final AnalyzeContext ctx) {
        return new PostgreSQLAnalyzeTableStatement();
    }
}
