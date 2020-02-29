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

package org.apache.shardingsphere.sql.parser.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.shardingsphere.sql.parser.api.visitor.DALVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.TimeZoneContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VariableContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VariablePropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.VariableValueContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.TimeZoneSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableValueSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.VariableProperty;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ShowStatement;
import org.apache.shardingsphere.sql.parser.visitor.PostgreSQLVisitor;

/**
 * DAL visitor for PostgreSQL.
 */
public final class PostgreSQLDALVisitor extends PostgreSQLVisitor implements DALVisitor {
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        return new ShowStatement();
    }
    
    @Override
    public ASTNode visitSetVariable(final SetVariableContext ctx) {
        SetStatement result = new SetStatement();
        if (null != ctx.variableProperty()) {
            VariablePropertyContext variablePropertyContext = ctx.variableProperty();
            List<VariableProperty> variablePropertyList = new ArrayList<VariableProperty>(1);
                VariableSegment variableSegment = null;
                VariableValueSegment variableValueSegment = null;
                String scopeType = variablePropertyContext.scopeKeyword() == null ? null : variablePropertyContext.scopeKeyword().getText();
                if (null != variablePropertyContext.timeZone()) {
                    TimeZoneSegment timeZoneSegment = (TimeZoneSegment)visit(variablePropertyContext.timeZone());
                    variableSegment = new VariableSegment(timeZoneSegment.getStartIndex(), timeZoneSegment.getStopIndex(), timeZoneSegment.getVariable());
                    variableValueSegment = (VariableValueSegment) visit(variablePropertyContext.variableValue());
                }else {
                    variableSegment = (VariableSegment) visitVariable(variablePropertyContext.variable());
                    variableValueSegment = (VariableValueSegment) visitVariableValue(variablePropertyContext.variableValue());
                }
                VariableProperty variableProperty = new VariableProperty(variableSegment, variableValueSegment, scopeType);
                variablePropertyList.add(variableProperty);
            result.setVariablePropertyList(variablePropertyList);
        }
        return result;
    }
    
    @Override
    public ASTNode visitVariableValue(final VariableValueContext ctx) {
        VariableValueSegment result = new VariableValueSegment(ctx.getText());
        return result;
    }
    
    @Override
    public ASTNode visitVariable(final VariableContext ctx) {
        return new VariableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitTimeZone(final TimeZoneContext ctx) {
        return new TimeZoneSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitResetParameter(final ResetParameterContext ctx) {
        return new ResetParameterStatement();
    }
}
