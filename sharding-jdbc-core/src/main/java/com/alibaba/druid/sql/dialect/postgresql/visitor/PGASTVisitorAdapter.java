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

package com.alibaba.druid.sql.dialect.postgresql.visitor;

import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithQuery;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGBoxExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGCidrExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGCircleExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGExtractExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGInetExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGIntervalExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGLineSegmentsExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGMacAddrExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGParameter;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGPointExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGPolygonExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.expr.PGTypeCastExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGFunctionTableSource;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.FetchClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.ForClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.PGLimit;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.WindowClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGValuesQuery;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

public class PGASTVisitorAdapter extends SQLASTVisitorAdapter implements PGASTVisitor {
    
    @Override
    public void endVisit(final WindowClause x) {
    }
    
    @Override
    public boolean visit(final WindowClause x) {
        return true;
    }
    
    @Override
    public void endVisit(final FetchClause x) {
    }
    
    @Override
    public boolean visit(final FetchClause x) {
        return true;
    }
    
    @Override
    public void endVisit(final ForClause x) {
    }
    
    @Override
    public boolean visit(final ForClause x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGWithQuery x) {
    }
    
    @Override
    public boolean visit(final PGWithQuery x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGWithClause x) {
    }
    
    @Override
    public boolean visit(final PGWithClause x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGSelectQueryBlock x) {
    }
    
    @Override
    public boolean visit(final PGSelectQueryBlock x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGParameter x) {
    }
    
    @Override
    public boolean visit(final PGParameter x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGFunctionTableSource x) {
    }
    
    @Override
    public boolean visit(final PGFunctionTableSource x) {
        return true;
    }
    
    @Override 
    public boolean visit(final PGLimit x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGLimit x) {
    }
    
    @Override
    public boolean visit(final PGTypeCastExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGTypeCastExpr x) {
    }
    
    @Override
    public void endVisit(final PGValuesQuery x) {
    }
    
    @Override
    public boolean visit(final PGValuesQuery x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGExtractExpr x) {
    }
    
    @Override
    public boolean visit(final PGExtractExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGBoxExpr x) {
    }
    
    @Override
    public boolean visit(final PGBoxExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGPointExpr x) {
    }
    
    @Override
    public boolean visit(final PGPointExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGMacAddrExpr x) {
    }
    
    @Override
    public boolean visit(final PGMacAddrExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGInetExpr x) {
    }
    
    @Override
    public boolean visit(final PGInetExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGCidrExpr x) {
    }
    
    @Override
    public boolean visit(final PGCidrExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGPolygonExpr x) {
    }
    
    @Override
    public boolean visit(final PGPolygonExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGCircleExpr x) {
    }
    
    @Override
    public boolean visit(final PGCircleExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGLineSegmentsExpr x) {
    }
    
    @Override
    public boolean visit(final PGLineSegmentsExpr x) {
        return true;
    }
    
    @Override
    public void endVisit(final PGIntervalExpr x) {
    }

    @Override
    public boolean visit(final PGIntervalExpr x) {
        return true;
    }
}
