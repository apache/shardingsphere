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

import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public interface MySqlASTVisitor extends SQLASTVisitor {
    
    boolean visit(MySqlSelectQueryBlock.Limit x);
    
    void endVisit(MySqlSelectQueryBlock.Limit x);
    
    void endVisit(MySqlIntervalExpr x);
    
    boolean visit(MySqlIntervalExpr x);
    
    void endVisit(MySqlExtractExpr x);
    
    boolean visit(MySqlExtractExpr x);
    
    void endVisit(MySqlMatchAgainstExpr x);
    
    boolean visit(MySqlMatchAgainstExpr x);
    
    boolean visit(MySqlSelectQueryBlock x);
    
    void endVisit(MySqlSelectQueryBlock x);
    
    boolean visit(MySqlUserName x);
    
    void endVisit(MySqlUserName x);
    
    boolean visit(MySqlUnionQuery x);
    
    void endVisit(MySqlUnionQuery x);
    
    boolean visit(MySqlCharExpr x);
    
    void endVisit(MySqlCharExpr x);
    
    boolean visit(MySqlSelectGroupByExpr x);
    
    void endVisit(MySqlSelectGroupByExpr x);
}
