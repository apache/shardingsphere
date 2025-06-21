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
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.autogen.OracleStatementParser.SetRoleContext;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.OracleStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.SetRoleStatement;

import java.util.Collections;

/**
 * DCL statement visitor for Oracle.
 */
public final class OracleDCLStatementVisitor extends OracleStatementVisitor implements DCLStatementVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement();
        if (null != ctx.objectPrivilegeClause() && null != ctx.objectPrivilegeClause().onObjectClause().tableName()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.objectPrivilegeClause().onObjectClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        RevokeStatement result = new RevokeStatement();
        if (null != ctx.objectPrivilegeClause() && null != ctx.objectPrivilegeClause().onObjectClause().tableName()) {
            result.getTables().add((SimpleTableSegment) visit(ctx.objectPrivilegeClause().onObjectClause().tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new CreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement(Collections.emptyList());
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new AlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement();
    }
    
    @Override
    public ASTNode visitSetRole(final SetRoleContext ctx) {
        return new SetRoleStatement();
    }
}
