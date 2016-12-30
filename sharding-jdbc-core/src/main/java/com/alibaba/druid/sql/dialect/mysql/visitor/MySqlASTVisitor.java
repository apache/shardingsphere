/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package com.alibaba.druid.sql.dialect.mysql.visitor;

import com.alibaba.druid.sql.dialect.mysql.ast.MySqlForceIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUseIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement.MySqlWhenStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlElseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement.MySqlElseIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIterateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLeaveStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLoopStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlParameter;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlRepeatStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlSelectIntoStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlWhileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetPasswordStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public interface MySqlASTVisitor extends SQLASTVisitor {
    
    boolean visit(MySqlSelectQueryBlock.Limit x);
    
    void endVisit(MySqlSelectQueryBlock.Limit x);
    
    boolean visit(MySqlTableIndex x);
    
    void endVisit(MySqlTableIndex x);
    
    void endVisit(MySqlIntervalExpr x);
    
    boolean visit(MySqlIntervalExpr x);
    
    void endVisit(MySqlExtractExpr x);
    
    boolean visit(MySqlExtractExpr x);
    
    void endVisit(MySqlMatchAgainstExpr x);
    
    boolean visit(MySqlMatchAgainstExpr x);
    
    void endVisit(MySqlPrepareStatement x);
    
    boolean visit(MySqlPrepareStatement x);
    
    void endVisit(MySqlExecuteStatement x);
    
    boolean visit(MySqlExecuteStatement x);
    
    void endVisit(MySqlDeleteStatement x);
    
    boolean visit(MySqlDeleteStatement x);
    
    void endVisit(MySqlInsertStatement x);
    
    boolean visit(MySqlInsertStatement x);
    
    void endVisit(MySqlSelectGroupBy x);
    
    boolean visit(MySqlSelectGroupBy x);
    
    boolean visit(MySqlSelectQueryBlock x);
    
    void endVisit(MySqlSelectQueryBlock x);
    
    boolean visit(MySqlOutFileExpr x);
    
    void endVisit(MySqlOutFileExpr x);
    
    boolean visit(MySqlUpdateStatement x);
    
    void endVisit(MySqlUpdateStatement x);
    
    boolean visit(MySqlSetTransactionStatement x);
    
    void endVisit(MySqlSetTransactionStatement x);
    
    boolean visit(MySqlSetNamesStatement x);
    
    void endVisit(MySqlSetNamesStatement x);
    
    boolean visit(MySqlSetCharSetStatement x);
    
    void endVisit(MySqlSetCharSetStatement x);
    
    boolean visit(MySqlUserName x);
    
    void endVisit(MySqlUserName x);
    
    boolean visit(MySqlUnionQuery x);
    
    void endVisit(MySqlUnionQuery x);
    
    boolean visit(MySqlUseIndexHint x);
    
    void endVisit(MySqlUseIndexHint x);
    
    boolean visit(MySqlIgnoreIndexHint x);
    
    void endVisit(MySqlIgnoreIndexHint x);
    
    boolean visit(MySqlUnlockTablesStatement x);
    
    void endVisit(MySqlUnlockTablesStatement x);
    
    boolean visit(MySqlForceIndexHint x);
    
    void endVisit(MySqlForceIndexHint x);
    
    boolean visit(MySqlCharExpr x);
    
    void endVisit(MySqlCharExpr x);
    
    boolean visit(MySqlPartitioningDef x);
    
    void endVisit(MySqlPartitioningDef x);
    
    boolean visit(MySqlPartitioningDef.LessThanValues x);
    
    void endVisit(MySqlPartitioningDef.LessThanValues x);
    
    boolean visit(MySqlPartitioningDef.InValues x);
    
    void endVisit(MySqlPartitioningDef.InValues x);
    
    boolean visit(MySqlSetPasswordStatement x);
    
    void endVisit(MySqlSetPasswordStatement x);
    
    boolean visit(MySqlHintStatement x);
    
    void endVisit(MySqlHintStatement x);
    
    boolean visit(MySqlSelectGroupByExpr x);
    
    void endVisit(MySqlSelectGroupByExpr x);
    
    boolean visit(MySqlParameter x);
    
    void endVisit(MySqlParameter x);
    
    boolean visit(MySqlWhileStatement x);
    
    void endVisit(MySqlWhileStatement x);
    
    boolean visit(MySqlIfStatement x);
    
    void endVisit(MySqlIfStatement x);
    
    boolean visit(MySqlElseIfStatement x);
    
    void endVisit(MySqlElseIfStatement x);
    
    boolean visit(MySqlElseStatement x);
    
    void endVisit(MySqlElseStatement x);
    
    boolean visit(MySqlCaseStatement x);
    
    void endVisit(MySqlCaseStatement x);
    
    boolean visit(MySqlSelectIntoStatement x);
    
    void endVisit(MySqlSelectIntoStatement x);
    
    boolean visit(MySqlWhenStatement x);
    
    void endVisit(MySqlWhenStatement x);
    
    boolean visit(MySqlLoopStatement x);
    
    void endVisit(MySqlLoopStatement x);
    
    boolean visit(MySqlLeaveStatement x);
    
    void endVisit(MySqlLeaveStatement x);
    
    boolean visit(MySqlIterateStatement x);
    
    void endVisit(MySqlIterateStatement x);
    
    boolean visit(MySqlRepeatStatement x);
    
    void endVisit(MySqlRepeatStatement x);
    
    boolean visit(MySqlUpdateTableSource x);
    
    void endVisit(MySqlUpdateTableSource x);
}
