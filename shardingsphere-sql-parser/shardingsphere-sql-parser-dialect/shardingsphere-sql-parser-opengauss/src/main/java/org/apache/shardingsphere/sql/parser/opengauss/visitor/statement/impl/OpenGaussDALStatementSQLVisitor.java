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
import org.apache.shardingsphere.sql.parser.api.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ConfigurationParameterClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExplainableStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LoadContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumRelationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumRelationListContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussAnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussLoadStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussSetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussVacuumStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DAL Statement SQL visitor for openGauss.
 */
@NoArgsConstructor
public final class OpenGaussDALStatementSQLVisitor extends OpenGaussStatementSQLVisitor implements DALSQLVisitor, SQLStatementVisitor {
    
    public OpenGaussDALStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        if (null != ctx.varName()) {
            return new OpenGaussShowStatement(ctx.varName().getText());
        }
        if (null != ctx.ZONE()) {
            return new OpenGaussShowStatement("timezone");
        }
        if (null != ctx.ISOLATION()) {
            return new OpenGaussShowStatement("transaction_isolation");
        }
        if (null != ctx.AUTHORIZATION()) {
            return new OpenGaussShowStatement("session_authorization");
        }
        return new OpenGaussShowStatement("ALL");
    }
    
    @Override
    public ASTNode visitSet(final SetContext ctx) {
        OpenGaussSetStatement result = new OpenGaussSetStatement();
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
        return new OpenGaussResetParameterStatement(null != ctx.ALL() ? "ALL" : ctx.identifier().getText());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        OpenGaussAnalyzeTableStatement result = new OpenGaussAnalyzeTableStatement();
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
        return new OpenGaussLoadStatement();
    }
    
    @Override
    public ASTNode visitVacuum(final VacuumContext ctx) {
        return new OpenGaussVacuumStatement();
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        OpenGaussExplainStatement result = new OpenGaussExplainStatement();
        result.setStatement((SQLStatement) visit(ctx.explainableStmt()));
        return result;
    }
    
    @Override
    public ASTNode visitExplainableStmt(final ExplainableStmtContext ctx) {
        if (null != ctx.select()) {
            return visit(ctx.select());
        } else if (null != ctx.insert()) {
            return visit(ctx.insert());
        } else if (null != ctx.update()) {
            return visit(ctx.update());
        } else if (null != ctx.delete()) {
            return visit(ctx.delete());
        } else if (null != ctx.declare()) {
            // TODO visit declare statement
            return visit(ctx.declare());
        } else if (null != ctx.executeStmt()) {
            return visit(ctx.executeStmt());
        } else if (null != ctx.createMaterializedView()) {
            // TODO visit create materialized view statement
            return visit(ctx.createMaterializedView());
        } else {
            // TODO visit refresh materialized view statement
            return visit(ctx.refreshMatViewStmt());
        }
    }
}
