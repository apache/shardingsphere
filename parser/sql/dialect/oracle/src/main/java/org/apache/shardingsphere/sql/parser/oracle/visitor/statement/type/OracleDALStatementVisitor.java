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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterResourceCostContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dal.OracleAlterResourceCostStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dal.OracleExplainStatement;

/**
 * DAL statement visitor for Oracle.
 */
public final class OracleDALStatementVisitor extends OracleStatementVisitor implements DALStatementVisitor {
    
    @Override
    public ASTNode visitAlterResourceCost(final AlterResourceCostContext ctx) {
        return new OracleAlterResourceCostStatement();
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        OracleExplainStatement result = new OracleExplainStatement();
        OracleDMLStatementVisitor visitor = new OracleDMLStatementVisitor();
        visitor.getParameterMarkerSegments().addAll(getParameterMarkerSegments());
        if (null != ctx.insert()) {
            result.setStatement((SQLStatement) visitor.visit(ctx.insert()));
        } else if (null != ctx.delete()) {
            result.setStatement((SQLStatement) visitor.visit(ctx.delete()));
        } else if (null != ctx.update()) {
            result.setStatement((SQLStatement) visitor.visit(ctx.update()));
        } else if (null != ctx.select()) {
            result.setStatement((SQLStatement) visitor.visit(ctx.select()));
        }
        result.addParameterMarkerSegments(getParameterMarkerSegments());
        return result;
    }
}
