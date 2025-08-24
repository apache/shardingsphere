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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.SQLServerStatementParser.ExplainableStatementContext;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.SQLServerStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;

/**
 * DAL statement visitor for SQLServer.
 */
public final class SQLServerDALStatementVisitor extends SQLServerStatementVisitor implements DALStatementVisitor {
    
    public SQLServerDALStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        return new ExplainStatement(getDatabaseType(), (SQLStatement) visit(ctx.explainableStatement()));
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
