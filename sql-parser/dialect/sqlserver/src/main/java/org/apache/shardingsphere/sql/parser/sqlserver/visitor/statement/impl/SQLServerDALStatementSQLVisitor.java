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

package org.apache.shardingsphere.sql.parser.sqlserver.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExplainableStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dal.SQLServerExplainStatement;

import java.util.Properties;

/**
 * DAL Statement SQL visitor for SQLServer.
 */
@NoArgsConstructor
public final class SQLServerDALStatementSQLVisitor extends SQLServerStatementSQLVisitor implements DALSQLVisitor, SQLStatementVisitor {
    
    public SQLServerDALStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        SQLServerExplainStatement result = new SQLServerExplainStatement();
        result.setStatement((SQLStatement) visit(ctx.explainableStatement()));
        return result;
    }
    
    @Override
    public ASTNode visitExplainableStatement(final ExplainableStatementContext ctx) {
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
        return visit(ctx.createTableAsSelectClause());
    }
}
