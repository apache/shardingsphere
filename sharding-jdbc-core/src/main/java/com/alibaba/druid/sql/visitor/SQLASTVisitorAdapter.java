/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (final the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.visitor;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLArrayExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLDefaultExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLTimestampExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.NotNullConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddPartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAlterColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropForeignKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropPartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRename;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRenameColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRenamePartition;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableSetComment;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableSetLifecycle;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableTouch;
import com.alibaba.druid.sql.ast.statement.SQLAlterViewRenameStatement;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLCheck;
import com.alibaba.druid.sql.ast.statement.SQLCloseStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLColumnReference;
import com.alibaba.druid.sql.ast.statement.SQLColumnUniqueKey;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropFunctionStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropProcedureStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropSequenceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableSpaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropUserStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLExplainStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprHint;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLFetchStatement;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLGrantStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLOpenStatement;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLReleaseSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLRevokeStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLShowTablesStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnique;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;

public class SQLASTVisitorAdapter implements SQLASTVisitor {

    public void endVisit(final SQLAllColumnExpr x) {
    }

    public void endVisit(final SQLBetweenExpr x) {
    }

    public void endVisit(final SQLBinaryOpExpr x) {
    }

    public void endVisit(final SQLCaseExpr x) {
    }

    public void endVisit(final SQLCaseExpr.Item x) {
    }

    public void endVisit(final SQLCharExpr x) {
    }

    public void endVisit(final SQLIdentifierExpr x) {
    }

    public void endVisit(final SQLInListExpr x) {
    }

    public void endVisit(final SQLIntegerExpr x) {
    }

    public void endVisit(final SQLExistsExpr x) {
    }

    public void endVisit(final SQLNCharExpr x) {
    }

    public void endVisit(final SQLNotExpr x) {
    }

    public void endVisit(final SQLNullExpr x) {
    }

    public void endVisit(final SQLNumberExpr x) {
    }

    public void endVisit(final SQLPropertyExpr x) {
    }

    public void endVisit(final SQLSelectGroupByClause x) {
    }

    public void endVisit(final SQLSelectItem x) {
    }

    public void endVisit(final SQLSelectStatement selectStatement) {
    }

    public void postVisit(final SQLObject astNode) {
    }

    public void preVisit(final SQLObject astNode) {
    }

    public boolean visit(final SQLAllColumnExpr x) {
        return true;
    }

    public boolean visit(final SQLBetweenExpr x) {
        return true;
    }

    public boolean visit(final SQLBinaryOpExpr x) {
        return true;
    }

    public boolean visit(final SQLCaseExpr x) {
        return true;
    }

    public boolean visit(final SQLCaseExpr.Item x) {
        return true;
    }

    public boolean visit(final SQLCastExpr x) {
        return true;
    }

    public boolean visit(final SQLCharExpr x) {
        return true;
    }

    public boolean visit(final SQLExistsExpr x) {
        return true;
    }

    public boolean visit(final SQLIdentifierExpr x) {
        return true;
    }

    public boolean visit(final SQLInListExpr x) {
        return true;
    }

    public boolean visit(final SQLIntegerExpr x) {
        return true;
    }

    public boolean visit(final SQLNCharExpr x) {
        return true;
    }

    public boolean visit(final SQLNotExpr x) {
        return true;
    }

    public boolean visit(final SQLNullExpr x) {
        return true;
    }

    public boolean visit(final SQLNumberExpr x) {
        return true;
    }

    public boolean visit(final SQLPropertyExpr x) {
        return true;
    }

    public boolean visit(final SQLSelectGroupByClause x) {
        return true;
    }

    public boolean visit(final SQLSelectItem x) {
        return true;
    }

    public void endVisit(final SQLCastExpr x) {
    }

    public boolean visit(final SQLSelectStatement astNode) {
        return true;
    }

    public void endVisit(final SQLAggregateExpr x) {
    }

    public boolean visit(final SQLAggregateExpr x) {
        return true;
    }

    public boolean visit(final SQLVariantRefExpr x) {
        return true;
    }

    public void endVisit(final SQLVariantRefExpr x) {
    }

    public boolean visit(final SQLQueryExpr x) {
        return true;
    }

    public void endVisit(final SQLQueryExpr x) {
    }

    public boolean visit(final SQLSelect x) {
        return true;
    }

    public void endVisit(final SQLSelect select) {
    }

    public boolean visit(final SQLSelectQueryBlock x) {
        return true;
    }

    public void endVisit(final SQLSelectQueryBlock x) {
    }

    public boolean visit(final SQLExprTableSource x) {
        return true;
    }

    public void endVisit(final SQLExprTableSource x) {
    }

    public boolean visit(final SQLOrderBy x) {
        return true;
    }

    public void endVisit(final SQLOrderBy x) {
    }

    public boolean visit(final SQLSelectOrderByItem x) {
        return true;
    }

    public void endVisit(final SQLSelectOrderByItem x) {
    }

    public boolean visit(final SQLDropTableStatement x) {
        return true;
    }

    public void endVisit(final SQLDropTableStatement x) {
    }

    public boolean visit(final SQLCreateTableStatement x) {
        return true;
    }

    public void endVisit(final SQLCreateTableStatement x) {
    }

    public boolean visit(final SQLColumnDefinition x) {
        return true;
    }

    public void endVisit(final SQLColumnDefinition x) {
    }

    public boolean visit(final SQLDataType x) {
        return true;
    }

    public void endVisit(final SQLDataType x) {
    }

    public boolean visit(final SQLDeleteStatement x) {
        return true;
    }

    public void endVisit(final SQLDeleteStatement x) {
    }

    public boolean visit(final SQLCurrentOfCursorExpr x) {
        return true;
    }

    public void endVisit(final SQLCurrentOfCursorExpr x) {
    }

    public boolean visit(final SQLInsertStatement x) {
        return true;
    }

    public void endVisit(final SQLInsertStatement x) {
    }

    public boolean visit(final SQLUpdateSetItem x) {
        return true;
    }

    public void endVisit(final SQLUpdateSetItem x) {
    }

    public boolean visit(final SQLUpdateStatement x) {
        return true;
    }

    public void endVisit(final SQLUpdateStatement x) {
    }

    public boolean visit(final SQLCreateViewStatement x) {
        return true;
    }

    public void endVisit(final SQLCreateViewStatement x) {
    }
    
    public boolean visit(final SQLCreateViewStatement.Column x) {
        return true;
    }
    
    public void endVisit(final SQLCreateViewStatement.Column x) {
    }

    public boolean visit(final NotNullConstraint x) {
        return true;
    }

    public void endVisit(final NotNullConstraint x) {
    }

    @Override
    public void endVisit(final SQLMethodInvokeExpr x) {

    }

    @Override
    public boolean visit(final SQLMethodInvokeExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLUnionQuery x) {

    }

    @Override
    public boolean visit(final SQLUnionQuery x) {
        return true;
    }

    @Override
    public boolean visit(final SQLUnaryExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLUnaryExpr x) {

    }

    @Override
    public boolean visit(final SQLHexExpr x) {
        return false;
    }

    @Override
    public void endVisit(final SQLHexExpr x) {

    }

    @Override
    public void endVisit(final SQLSetStatement x) {

    }

    @Override
    public boolean visit(final SQLSetStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAssignItem x) {

    }

    @Override
    public boolean visit(final SQLAssignItem x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCallStatement x) {

    }

    @Override
    public boolean visit(final SQLCallStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLJoinTableSource x) {

    }

    @Override
    public boolean visit(final SQLJoinTableSource x) {
        return true;
    }

    @Override
    public boolean visit(final ValuesClause x) {
        return true;
    }

    @Override
    public void endVisit(final ValuesClause x) {

    }

    @Override
    public void endVisit(final SQLSomeExpr x) {

    }

    @Override
    public boolean visit(final SQLSomeExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAnyExpr x) {

    }

    @Override
    public boolean visit(final SQLAnyExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAllExpr x) {

    }

    @Override
    public boolean visit(final SQLAllExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLInSubQueryExpr x) {

    }

    @Override
    public boolean visit(final SQLInSubQueryExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLListExpr x) {

    }

    @Override
    public boolean visit(final SQLListExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLSubqueryTableSource x) {

    }

    @Override
    public boolean visit(final SQLSubqueryTableSource x) {
        return true;
    }

    @Override
    public void endVisit(final SQLTruncateStatement x) {

    }

    @Override
    public boolean visit(final SQLTruncateStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDefaultExpr x) {

    }

    @Override
    public boolean visit(final SQLDefaultExpr x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCommentStatement x) {

    }

    @Override
    public boolean visit(final SQLCommentStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLUseStatement x) {

    }

    @Override
    public boolean visit(final SQLUseStatement x) {
        return true;
    }

    @Override
    public boolean visit(final SQLAlterTableAddColumn x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableAddColumn x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDropColumnItem x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDropColumnItem x) {

    }

    @Override
    public boolean visit(final SQLDropIndexStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDropIndexStatement x) {

    }

    @Override
    public boolean visit(final SQLDropViewStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDropViewStatement x) {

    }

    @Override
    public boolean visit(final SQLSavePointStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLSavePointStatement x) {

    }

    @Override
    public boolean visit(final SQLRollbackStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLRollbackStatement x) {

    }

    @Override
    public boolean visit(final SQLReleaseSavePointStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLReleaseSavePointStatement x) {
    }

    @Override
    public boolean visit(final SQLCommentHint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCommentHint x) {

    }

    @Override
    public void endVisit(final SQLCreateDatabaseStatement x) {

    }

    @Override
    public boolean visit(final SQLCreateDatabaseStatement x) {
        return true;
    }

    @Override
    public boolean visit(final SQLAlterTableDropIndex x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDropIndex x) {

    }

    @Override
    public void endVisit(final SQLOver x) {
    }

    @Override
    public boolean visit(final SQLOver x) {
        return true;
    }

    @Override
    public void endVisit(final SQLColumnPrimaryKey x) {

    }

    @Override
    public boolean visit(final SQLColumnPrimaryKey x) {
        return true;
    }

    @Override
    public void endVisit(final SQLColumnUniqueKey x) {

    }

    @Override
    public boolean visit(final SQLColumnUniqueKey x) {
        return true;
    }

    @Override
    public void endVisit(final SQLWithSubqueryClause x) {
    }

    @Override
    public boolean visit(final SQLWithSubqueryClause x) {
        return true;
    }

    @Override
    public void endVisit(final SQLWithSubqueryClause.Entry x) {
    }

    @Override
    public boolean visit(final SQLWithSubqueryClause.Entry x) {
        return true;
    }

    @Override
    public boolean visit(final SQLCharacterDataType x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCharacterDataType x) {

    }

    @Override
    public void endVisit(final SQLAlterTableAlterColumn x) {

    }

    @Override
    public boolean visit(final SQLAlterTableAlterColumn x) {
        return true;
    }

    @Override
    public boolean visit(final SQLCheck x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCheck x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDropForeignKey x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDropForeignKey x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDropPrimaryKey x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDropPrimaryKey x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDisableKeys x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDisableKeys x) {

    }

    @Override
    public boolean visit(final SQLAlterTableEnableKeys x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableEnableKeys x) {

    }

    @Override
    public boolean visit(final SQLAlterTableStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableStatement x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDisableConstraint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDisableConstraint x) {

    }

    @Override
    public boolean visit(final SQLAlterTableEnableConstraint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableEnableConstraint x) {

    }

    @Override
    public boolean visit(final SQLColumnCheck x) {
        return true;
    }

    @Override
    public void endVisit(final SQLColumnCheck x) {

    }

    @Override
    public boolean visit(final SQLExprHint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLExprHint x) {

    }

    @Override
    public boolean visit(final SQLAlterTableDropConstraint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableDropConstraint x) {

    }

    @Override
    public boolean visit(final SQLUnique x) {
        for (SQLExpr column : x.getColumns()) {
            column.accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(final SQLUnique x) {

    }

    @Override
    public boolean visit(final SQLCreateIndexStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCreateIndexStatement x) {

    }

    @Override
    public boolean visit(final SQLPrimaryKeyImpl x) {
        return true;
    }

    @Override
    public void endVisit(final SQLPrimaryKeyImpl x) {

    }

    @Override
    public boolean visit(final SQLAlterTableRenameColumn x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableRenameColumn x) {

    }

    @Override
    public boolean visit(final SQLColumnReference x) {
        return true;
    }

    @Override
    public void endVisit(final SQLColumnReference x) {

    }

    @Override
    public boolean visit(final SQLForeignKeyImpl x) {
        return true;
    }

    @Override
    public void endVisit(final SQLForeignKeyImpl x) {

    }

    @Override
    public boolean visit(final SQLDropSequenceStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDropSequenceStatement x) {

    }

    @Override
    public boolean visit(final SQLDropTriggerStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDropTriggerStatement x) {

    }

    @Override
    public void endVisit(final SQLDropUserStatement x) {

    }

    @Override
    public boolean visit(final SQLDropUserStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLExplainStatement x) {

    }

    @Override
    public boolean visit(final SQLExplainStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLGrantStatement x) {

    }

    @Override
    public boolean visit(final SQLGrantStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLDropDatabaseStatement x) {

    }

    @Override
    public boolean visit(final SQLDropDatabaseStatement x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableAddIndex x) {

    }

    @Override
    public boolean visit(final SQLAlterTableAddIndex x) {
        return true;
    }

    @Override
    public void endVisit(final SQLAlterTableAddConstraint x) {

    }

    @Override
    public boolean visit(final SQLAlterTableAddConstraint x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLCreateTriggerStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLCreateTriggerStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLDropFunctionStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLDropFunctionStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLDropTableSpaceStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLDropTableSpaceStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLDropProcedureStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLDropProcedureStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLBooleanExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLBooleanExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLUnionQueryTableSource x) {
        
    }

    @Override
    public boolean visit(final SQLUnionQueryTableSource x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLTimestampExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLTimestampExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLRevokeStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLRevokeStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLBinaryExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLBinaryExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableRename x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableRename x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterViewRenameStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterViewRenameStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLShowTablesStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLShowTablesStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableAddPartition x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableAddPartition x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableDropPartition x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableDropPartition x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableRenamePartition x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableRenamePartition x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableSetComment x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableSetComment x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableSetLifecycle x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableSetLifecycle x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableEnableLifecycle x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableEnableLifecycle x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableDisableLifecycle x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableDisableLifecycle x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAlterTableTouch x) {
        
    }
    
    @Override
    public boolean visit(final SQLAlterTableTouch x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLArrayExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLArrayExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLOpenStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLOpenStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLFetchStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLFetchStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLCloseStatement x) {
        
    }
    
    @Override
    public boolean visit(final SQLCloseStatement x) {
        return true;
    }
    
}
