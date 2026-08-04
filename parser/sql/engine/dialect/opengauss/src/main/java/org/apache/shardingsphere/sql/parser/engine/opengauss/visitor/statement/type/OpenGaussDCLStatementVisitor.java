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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AlterUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AnyNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AttrsContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CreateUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropRoleContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.DropUserContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FuncNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.FunctionWithArgtypesContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.GrantContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.IndirectionContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.NumericOnlyContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.OnObjectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrivilegeClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.PrivilegeLevelContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.QualifiedNameContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.QualifiedNameListContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.RevokeContext;
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dcl.PrivilegeObjectSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DCL statement visitor for openGauss.
 */
public final class OpenGaussDCLStatementVisitor extends OpenGaussStatementVisitor implements DCLStatementVisitor {
    
    public OpenGaussDCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitGrant(final GrantContext ctx) {
        GrantStatement result = new GrantStatement(getDatabaseType(), containsTableSegment(ctx.privilegeClause()) ? getTableSegments(ctx.privilegeClause()) : Collections.emptyList());
        fillPrivilegeObjects(result.getPrivilegeObjects(), ctx.privilegeClause());
        return result;
    }
    
    @Override
    public ASTNode visitRevoke(final RevokeContext ctx) {
        RevokeStatement result = new RevokeStatement(getDatabaseType(), containsTableSegment(ctx.privilegeClause()) ? getTableSegments(ctx.privilegeClause()) : Collections.emptyList());
        fillPrivilegeObjects(result.getPrivilegeObjects(), ctx.privilegeClause());
        return result;
    }
    
    private boolean containsTableSegment(final PrivilegeClauseContext ctx) {
        return null != ctx && null != ctx.onObjectClause() && null != ctx.onObjectClause().privilegeLevel() && null != ctx.onObjectClause().privilegeLevel().tableNames();
    }
    
    @SuppressWarnings("unchecked")
    private Collection<SimpleTableSegment> getTableSegments(final PrivilegeClauseContext ctx) {
        return ((CollectionValue<SimpleTableSegment>) visit(ctx.onObjectClause().privilegeLevel().tableNames())).getValue();
    }
    
    private void fillPrivilegeObjects(final Collection<PrivilegeObjectSegment> result, final PrivilegeClauseContext ctx) {
        if (null == ctx || null == ctx.onObjectClause()) {
            return;
        }
        OnObjectClauseContext onObjectClause = ctx.onObjectClause();
        String objectType = getObjectType(onObjectClause);
        if (null != onObjectClause.nameList()) {
            fillNamePrivilegeObjects(result, objectType, onObjectClause.nameList());
        } else if (null != onObjectClause.anyNameList()) {
            for (AnyNameContext each : onObjectClause.anyNameList().anyName()) {
                addPrivilegeObject(result, objectType, each, getAnyNameIdentifiers(each));
            }
        } else if (null != onObjectClause.functionWithArgtypesList()) {
            for (FunctionWithArgtypesContext each : onObjectClause.functionWithArgtypesList().functionWithArgtypes()) {
                addPrivilegeObject(result, objectType, each, getFunctionIdentifiers(each));
            }
        } else if (null != onObjectClause.numericOnlyList()) {
            for (NumericOnlyContext each : onObjectClause.numericOnlyList().numericOnly()) {
                addPrivilegeObject(result, objectType, each, Collections.singletonList(new IdentifierValue(each.getText())));
            }
        } else if (null != onObjectClause.qualifiedNameList()) {
            fillQualifiedNamePrivilegeObjects(result, objectType, onObjectClause.qualifiedNameList());
        } else {
            fillPrivilegeLevelObjects(result, objectType, onObjectClause.privilegeLevel());
        }
    }
    
    private String getObjectType(final OnObjectClauseContext ctx) {
        if (null != ctx.ALL() || null != ctx.SCHEMA()) {
            return "SCHEMA";
        }
        if (null != ctx.DATABASE()) {
            return "DATABASE";
        }
        if (null != ctx.DOMAIN()) {
            return "DOMAIN";
        }
        if (null != ctx.FUNCTION()) {
            return "FUNCTION";
        }
        if (null != ctx.PROCEDURE()) {
            return "PROCEDURE";
        }
        if (null != ctx.ROUTINE()) {
            return "ROUTINE";
        }
        if (null != ctx.LANGUAGE()) {
            return "LANGUAGE";
        }
        if (null != ctx.LARGE()) {
            return "LARGE OBJECT";
        }
        if (null != ctx.TABLESPACE()) {
            return "TABLESPACE";
        }
        if (null != ctx.TYPE()) {
            return "TYPE";
        }
        if (null != ctx.SEQUENCE()) {
            return "SEQUENCE";
        }
        if (null != ctx.FOREIGN()) {
            return null == ctx.DATA() ? "FOREIGN SERVER" : "FOREIGN DATA WRAPPER";
        }
        if (null != ctx.CLIENT_MASTER_KEY()) {
            return "CLIENT MASTER KEY";
        }
        if (null != ctx.COLUMN_ENCRYPTION_KEY()) {
            return "COLUMN ENCRYPTION KEY";
        }
        return "TABLE";
    }
    
    private void fillNamePrivilegeObjects(final Collection<PrivilegeObjectSegment> result, final String objectType, final NameListContext ctx) {
        if (null != ctx.nameList()) {
            fillNamePrivilegeObjects(result, objectType, ctx.nameList());
        }
        NameContext name = ctx.name();
        addPrivilegeObject(result, objectType, name, Collections.singletonList(new IdentifierValue(name.getText())));
    }
    
    private List<IdentifierValue> getAnyNameIdentifiers(final AnyNameContext ctx) {
        List<IdentifierValue> result = new LinkedList<>();
        result.add(new IdentifierValue(ctx.colId().getText()));
        if (null != ctx.attrs()) {
            fillAttrsIdentifiers(result, ctx.attrs());
        }
        return result;
    }
    
    private void fillAttrsIdentifiers(final Collection<IdentifierValue> result, final AttrsContext ctx) {
        if (null != ctx.attrs()) {
            fillAttrsIdentifiers(result, ctx.attrs());
        }
        result.add(new IdentifierValue(ctx.attrName().getText()));
    }
    
    private List<IdentifierValue> getFunctionIdentifiers(final FunctionWithArgtypesContext ctx) {
        if (null != ctx.funcName()) {
            return getFunctionIdentifiers(ctx.funcName());
        }
        List<IdentifierValue> result = new LinkedList<>();
        if (null != ctx.typeFuncNameKeyword()) {
            result.add(new IdentifierValue(ctx.typeFuncNameKeyword().getText()));
        } else {
            result.add(new IdentifierValue(ctx.colId().getText()));
            fillIndirectionIdentifiers(result, ctx.indirection());
        }
        return result;
    }
    
    private List<IdentifierValue> getFunctionIdentifiers(final FuncNameContext ctx) {
        List<IdentifierValue> result = new LinkedList<>();
        if (null != ctx.typeFunctionName()) {
            result.add(new IdentifierValue(ctx.typeFunctionName().getText()));
        } else {
            result.add(new IdentifierValue(ctx.colId().getText()));
            fillIndirectionIdentifiers(result, ctx.indirection());
        }
        return result;
    }
    
    private void fillIndirectionIdentifiers(final Collection<IdentifierValue> result, final IndirectionContext ctx) {
        if (null == ctx) {
            return;
        }
        if (null != ctx.indirection()) {
            fillIndirectionIdentifiers(result, ctx.indirection());
        }
        if (null != ctx.indirectionEl().attrName()) {
            result.add(new IdentifierValue(ctx.indirectionEl().attrName().getText()));
        }
    }
    
    private void fillQualifiedNamePrivilegeObjects(final Collection<PrivilegeObjectSegment> result, final String objectType, final QualifiedNameListContext ctx) {
        if (null != ctx.qualifiedNameList()) {
            fillQualifiedNamePrivilegeObjects(result, objectType, ctx.qualifiedNameList());
        }
        QualifiedNameContext qualifiedName = ctx.qualifiedName();
        addSimpleTablePrivilegeObject(result, objectType, (SimpleTableSegment) visit(qualifiedName));
    }
    
    @SuppressWarnings("unchecked")
    private void fillPrivilegeLevelObjects(final Collection<PrivilegeObjectSegment> result, final String objectType, final PrivilegeLevelContext ctx) {
        if (null != ctx.tableNames()) {
            for (SimpleTableSegment each : ((CollectionValue<SimpleTableSegment>) visit(ctx.tableNames())).getValue()) {
                addSimpleTablePrivilegeObject(result, objectType, each);
            }
            return;
        }
        List<IdentifierValue> identifiers = new LinkedList<>();
        if (null != ctx.identifier()) {
            identifiers.add((IdentifierValue) visit(ctx.identifier()));
            identifiers.add(new IdentifierValue("*"));
        } else if (null != ctx.schemaName()) {
            identifiers.add((IdentifierValue) visit(ctx.schemaName()));
            identifiers.add(new IdentifierValue(ctx.routineName().getText()));
        } else {
            identifiers.add(new IdentifierValue("*"));
            if (null != ctx.DOT_ASTERISK_()) {
                identifiers.add(new IdentifierValue("*"));
            }
        }
        addPrivilegeObject(result, objectType, ctx, identifiers);
    }
    
    private void addSimpleTablePrivilegeObject(final Collection<PrivilegeObjectSegment> result, final String objectType, final SimpleTableSegment table) {
        List<IdentifierValue> identifiers = new LinkedList<>();
        table.getOwner().ifPresent(owner -> fillOwnerIdentifiers(identifiers, owner));
        identifiers.add(table.getTableName().getIdentifier());
        PrivilegeObjectSegment privilegeObject = new PrivilegeObjectSegment(table.getStartIndex(), table.getStopIndex(), objectType);
        privilegeObject.getIdentifiers().addAll(identifiers);
        result.add(privilegeObject);
    }
    
    private void fillOwnerIdentifiers(final Collection<IdentifierValue> result, final OwnerSegment owner) {
        owner.getOwner().ifPresent(each -> fillOwnerIdentifiers(result, each));
        result.add(owner.getIdentifier());
    }
    
    private void addPrivilegeObject(final Collection<PrivilegeObjectSegment> result, final String objectType, final ParserRuleContext ctx,
                                    final Collection<IdentifierValue> identifiers) {
        PrivilegeObjectSegment privilegeObject = new PrivilegeObjectSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), objectType);
        privilegeObject.getIdentifiers().addAll(identifiers);
        result.add(privilegeObject);
    }
    
    @Override
    public ASTNode visitCreateUser(final CreateUserContext ctx) {
        return new CreateUserStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropUser(final DropUserContext ctx) {
        return new DropUserStatement(getDatabaseType(), Collections.emptyList());
    }
    
    @Override
    public ASTNode visitAlterUser(final AlterUserContext ctx) {
        return new AlterUserStatement(getDatabaseType(), null);
    }
    
    @Override
    public ASTNode visitCreateRole(final CreateRoleContext ctx) {
        return new CreateRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitAlterRole(final AlterRoleContext ctx) {
        return new AlterRoleStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitDropRole(final DropRoleContext ctx) {
        return new DropRoleStatement(getDatabaseType());
    }
}
