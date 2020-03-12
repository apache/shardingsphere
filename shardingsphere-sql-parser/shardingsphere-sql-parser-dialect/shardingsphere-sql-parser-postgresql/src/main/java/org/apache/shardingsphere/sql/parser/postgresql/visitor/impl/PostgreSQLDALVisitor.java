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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.impl;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DALVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ConfigurationParameterClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SetContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.PostgreSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ShowStatement;

/**
 * DAL visitor for PostgreSQL.
 */
public final class PostgreSQLDALVisitor extends PostgreSQLVisitor implements DALVisitor {
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        return new ShowStatement();
    }
    
    @Override
    public ASTNode visitSet(final SetContext ctx) {
        SetStatement result = new SetStatement();
        if (null != ctx.configurationParameterClause()) {
            result.setVariable((VariableSegment) visit(ctx.configurationParameterClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitConfigurationParameterClause(final ConfigurationParameterClauseContext ctx) {
        return new VariableSegment(ctx.identifier(0).getStart().getStartIndex(), ctx.identifier(0).getStop().getStopIndex(), ctx.identifier(0).getText());
    }
    
    @Override
    public ASTNode visitResetParameter(final ResetParameterContext ctx) {
        return new ResetParameterStatement();
    }
}
