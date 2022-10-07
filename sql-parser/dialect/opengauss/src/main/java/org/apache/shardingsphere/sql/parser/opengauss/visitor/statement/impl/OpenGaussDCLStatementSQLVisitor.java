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
import org.apache.shardingsphere.sql.parser.api.visitor.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RevokeContext;
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
import java.util.Optional;
import java.util.Properties;

/**
 * DCL Statement SQL visitor for openGauss.
 */
@NoArgsConstructor
public final class OpenGaussDCLStatementSQLVisitor extends OpenGaussStatementSQLVisitor implements DCLSQLVisitor, SQLStatementVisitor {
    
    public OpenGaussDCLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        OpenGaussGrantStatement result = new OpenGaussGrantStatement();
        Optional<Collection<SimpleTableSegment>> tableSegment = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegment.ifPresent(optional -> result.getTables().addAll(optional));
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        OpenGaussRevokeStatement result = new OpenGaussRevokeStatement();
        Optional<Collection<SimpleTableSegment>> tableSegment = null == ctx.privilegeClause() ? Optional.empty() : getTableFromPrivilegeClause(ctx.privilegeClause());
        tableSegment.ifPresent(optional -> result.getTables().addAll(optional));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<Collection<SimpleTableSegment>> getTableFromPrivilegeClause(final PrivilegeClauseContext ctx) {
        return null == ctx.onObjectClause() || null == ctx.onObjectClause().privilegeLevel() || null == ctx.onObjectClause().privilegeLevel().tableNames()
                ? Optional.empty()
                : Optional.of(((CollectionValue<SimpleTableSegment>) visit(ctx.onObjectClause().privilegeLevel().tableNames())).getValue());
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
