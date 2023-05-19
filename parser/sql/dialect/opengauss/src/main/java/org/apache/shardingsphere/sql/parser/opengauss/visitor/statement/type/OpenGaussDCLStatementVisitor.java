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

package org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussAlterRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussAlterUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussCreateRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussDropRoleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussDropUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussGrantStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dcl.OpenGaussRevokeStatement;

import java.util.Collection;

/**
 * DCL statement visitor for openGauss.
 */
public final class OpenGaussDCLStatementVisitor extends OpenGaussStatementVisitor implements DCLStatementVisitor {
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        OpenGaussGrantStatement result = new OpenGaussGrantStatement();
        if (containsTableSegment(ctx.privilegeClause())) {
            result.getTables().addAll(getTableSegments(ctx.privilegeClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        OpenGaussRevokeStatement result = new OpenGaussRevokeStatement();
        if (containsTableSegment(ctx.privilegeClause())) {
            result.getTables().addAll(getTableSegments(ctx.privilegeClause()));
        }
        return result;
    }
    
    private boolean containsTableSegment(final PrivilegeClauseContext ctx) {
        return null != ctx && null != ctx.onObjectClause() && null != ctx.onObjectClause().privilegeLevel() && null != ctx.onObjectClause().privilegeLevel().tableNames();
    }
    
    @SuppressWarnings("unchecked")
    private Collection<SimpleTableSegment> getTableSegments(final PrivilegeClauseContext ctx) {
        return ((CollectionValue<SimpleTableSegment>) visit(ctx.onObjectClause().privilegeLevel().tableNames())).getValue();
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new OpenGaussCreateUserStatement();
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new OpenGaussDropUserStatement();
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new OpenGaussAlterUserStatement();
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new OpenGaussCreateRoleStatement();
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new OpenGaussAlterRoleStatement();
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new OpenGaussDropRoleStatement();
    }
}
