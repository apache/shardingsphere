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
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprHint;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;

public class SQLASTVisitorAdapter implements SQLASTVisitor {
    
    @Override
    public void endVisit(final SQLAllColumnExpr x) {
    }
    
    @Override
    public void endVisit(final SQLBetweenExpr x) {
    }
    
    @Override
    public void endVisit(final SQLBinaryOpExpr x) {
    }
    
    @Override
    public void endVisit(final SQLCaseExpr x) {
    }
    
    @Override
    public void endVisit(final SQLCaseExpr.Item x) {
    }
    
    @Override
    public void endVisit(final SQLCharExpr x) {
    }
    
    @Override
    public void endVisit(final SQLIdentifierExpr x) {
    }
    
    @Override
    public void endVisit(final SQLInListExpr x) {
    }
    
    @Override
    public void endVisit(final SQLIntegerExpr x) {
    }
    
    @Override
    public void endVisit(final SQLExistsExpr x) {
    }
    
    @Override
    public void endVisit(final SQLNCharExpr x) {
    }
    
    @Override
    public void endVisit(final SQLNotExpr x) {
    }
    
    @Override
    public void endVisit(final SQLNullExpr x) {
    }
    
    @Override
    public void endVisit(final SQLNumberExpr x) {
    }
    
    @Override
    public void endVisit(final SQLPropertyExpr x) {
    }
    
    @Override
    public void endVisit(final SQLSelectGroupByClause x) {
    }
    
    @Override
    public void endVisit(final SQLSelectItem x) {
    }
    
    @Override
    public void endVisit(final SQLSelectStatement selectStatement) {
    }
    
    @Override
    public void postVisit(final SQLObject astNode) {
    }
    
    @Override
    public void preVisit(final SQLObject astNode) {
    }
    
    @Override
    public boolean visit(final SQLAllColumnExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLBetweenExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLBinaryOpExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLCaseExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLCaseExpr.Item x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLCastExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLCharExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLExistsExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLIdentifierExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLInListExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLIntegerExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLNCharExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLNotExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLNullExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLNumberExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLPropertyExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLSelectGroupByClause x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLSelectItem x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLCastExpr x) {
    }
    
    @Override
    public boolean visit(final SQLSelectStatement astNode) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLAggregateExpr x) {
    }
    
    @Override
    public boolean visit(final SQLAggregateExpr x) {
        return true;
    }
    
    @Override
    public boolean visit(final SQLVariantRefExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLVariantRefExpr x) {
    }
    
    @Override
    public boolean visit(final SQLQueryExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLQueryExpr x) {
    }
    
    @Override
    public boolean visit(final SQLSelect x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLSelect select) {
    }
    
    @Override
    public boolean visit(final SQLSelectQueryBlock x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLSelectQueryBlock x) {
    }
    
    @Override
    public boolean visit(final SQLExprTableSource x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLExprTableSource x) {
    }
    
    @Override
    public boolean visit(final SQLOrderBy x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLOrderBy x) {
    }
    
    @Override
    public boolean visit(final SQLSelectOrderByItem x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLSelectOrderByItem x) {
    }
    
    @Override
    public boolean visit(final SQLDataType x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLDataType x) {
    }
    
    @Override
    public boolean visit(final SQLDeleteStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLDeleteStatement x) {
    }
    
    @Override
    public boolean visit(final SQLUpdateSetItem x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLUpdateSetItem x) {
    }
    
    @Override
    public boolean visit(final SQLUpdateStatement x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLUpdateStatement x) {
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
    public void endVisit(final SQLJoinTableSource x) {

    }

    @Override
    public boolean visit(final SQLJoinTableSource x) {
        return true;
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
    public boolean visit(final SQLCommentHint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLCommentHint x) {

    }

    @Override
    public void endVisit(final SQLOver x) {
    }

    @Override
    public boolean visit(final SQLOver x) {
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
    public boolean visit(final SQLExprHint x) {
        return true;
    }

    @Override
    public void endVisit(final SQLExprHint x) {

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
    public void endVisit(final SQLBinaryExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLBinaryExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final SQLArrayExpr x) {
        
    }
    
    @Override
    public boolean visit(final SQLArrayExpr x) {
        return true;
    }
}
