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
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterResourceCostContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExecuteContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SpoolContext;
import org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dal.OracleAlterResourceCostStatement;
import org.apache.shardingsphere.sql.parser.statement.oracle.dal.OracleSpoolStatement;

import java.util.Optional;

/**
 * DAL statement visitor for Oracle.
 */
public final class OracleDALStatementVisitor extends OracleStatementVisitor implements DALStatementVisitor {
    
    public OracleDALStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitAlterResourceCost(final AlterResourceCostContext ctx) {
        return new OracleAlterResourceCostStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        OracleDMLStatementVisitor visitor = new OracleDMLStatementVisitor(getDatabaseType());
        getGlobalParameterMarkerSegments().addAll(visitor.getGlobalParameterMarkerSegments());
        getStatementParameterMarkerSegments().addAll(visitor.getStatementParameterMarkerSegments());
        ExplainStatement result = new ExplainStatement(getDatabaseType(), getExplainableSQLStatement(ctx, visitor).orElse(null));
        result.addParameterMarkers(ctx.getParent() instanceof ExecuteContext ? getGlobalParameterMarkerSegments() : popAllStatementParameterMarkerSegments());
        result.getVariableNames().addAll(getVariableNames());
        return result;
    }
    
    private Optional<SQLStatement> getExplainableSQLStatement(final ExplainContext ctx, final OracleDMLStatementVisitor visitor) {
        if (null != ctx.insert()) {
            return Optional.of((SQLStatement) visitor.visit(ctx.insert()));
        }
        if (null != ctx.delete()) {
            return Optional.of((SQLStatement) visitor.visit(ctx.delete()));
        }
        if (null != ctx.update()) {
            return Optional.of((SQLStatement) visitor.visit(ctx.update()));
        }
        if (null != ctx.select()) {
            return Optional.of((SQLStatement) visitor.visit(ctx.select()));
        }
        return Optional.empty();
    }
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        return new ShowStatement(getDatabaseType(), "");
    }
    
    @Override
    public ASTNode visitSpool(final SpoolContext ctx) {
        return new OracleSpoolStatement(getDatabaseType(), null == ctx.spoolFileName() ? null : ctx.spoolFileName().getText());
    }
}
