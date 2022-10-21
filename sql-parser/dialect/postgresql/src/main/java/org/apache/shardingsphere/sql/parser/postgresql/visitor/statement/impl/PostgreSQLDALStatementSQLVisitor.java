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

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ConfigurationParameterClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ExplainableStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.LoadContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VacuumContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VacuumRelationContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VacuumRelationListContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLLoadStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLVacuumStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DAL Statement SQL visitor for PostgreSQL.
 */
@NoArgsConstructor
public final class PostgreSQLDALStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DALSQLVisitor, SQLStatementVisitor {
    
    public PostgreSQLDALStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        if (null != ctx.varName()) {
            return new PostgreSQLShowStatement(ctx.varName().getText());
        }
        if (null != ctx.ZONE()) {
            return new PostgreSQLShowStatement("timezone");
        }
        if (null != ctx.ISOLATION()) {
            return new PostgreSQLShowStatement("transaction_isolation");
        }
        if (null != ctx.AUTHORIZATION()) {
            return new PostgreSQLShowStatement("session_authorization");
        }
        return new PostgreSQLShowStatement("ALL");
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
        }
        if (null != ctx.encoding()) {
            VariableAssignSegment variableAssignSegment = new VariableAssignSegment();
            VariableSegment variableSegment = new VariableSegment();
            variableSegment.setVariable("client_encoding");
            variableAssignSegment.setVariable(variableSegment);
            String value = ctx.encoding().getText();
            variableAssignSegment.setAssignValue(value);
            variableAssigns.add(variableAssignSegment);
        }
        result.getVariableAssigns().addAll(variableAssigns);
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
        return new PostgreSQLResetParameterStatement(null != ctx.ALL() ? "ALL" : ctx.identifier().getText());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        PostgreSQLAnalyzeTableStatement result = new PostgreSQLAnalyzeTableStatement();
        if (null != ctx.vacuumRelationList()) {
            result.getTables().addAll(((CollectionValue<SimpleTableSegment>) visit(ctx.vacuumRelationList())).getValue());
        }
        return result;
    }
    
    @Override
    public ASTNode visitVacuumRelationList(final VacuumRelationListContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (VacuumRelationContext each : ctx.vacuumRelation()) {
            ColIdContext colId = each.qualifiedName().colId();
            TableNameSegment tableName = new TableNameSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
            result.getValue().add(new SimpleTableSegment(tableName));
        }
        return result;
    }
    
    @Override
    public ASTNode visitLoad(final LoadContext ctx) {
        return new PostgreSQLLoadStatement();
    }
    
    @Override
    public ASTNode visitVacuum(final VacuumContext ctx) {
        return new PostgreSQLVacuumStatement();
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        PostgreSQLExplainStatement result = new PostgreSQLExplainStatement();
        result.setStatement((SQLStatement) visit(ctx.explainableStmt()));
        return result;
    }
    
    @Override
    public ASTNode visitExplainableStmt(final ExplainableStmtContext ctx) {
        if (null != ctx.select()) {
            return visit(ctx.select());
        }
        if (null != ctx.insert()) {
            return visit(ctx.insert());
        }
        if (null != ctx.update()) {
            return visit(ctx.update());
        }
        if (null != ctx.delete()) {
            return visit(ctx.delete());
        }
        if (null != ctx.declare()) {
            // TODO visit declare statement
            return visit(ctx.declare());
        }
        if (null != ctx.executeStmt()) {
            return visit(ctx.executeStmt());
        }
        if (null != ctx.createMaterializedView()) {
            return visit(ctx.createMaterializedView());
        }
        // TODO visit refresh materialized view statement
        return visit(ctx.refreshMatViewStmt());
    }
}
