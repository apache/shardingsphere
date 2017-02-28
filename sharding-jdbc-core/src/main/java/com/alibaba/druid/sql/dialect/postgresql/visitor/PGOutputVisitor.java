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

package com.alibaba.druid.sql.dialect.postgresql.visitor;

import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
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
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.PGLimit;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.WindowClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGValuesQuery;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

public class PGOutputVisitor extends SQLASTOutputVisitor implements PGASTVisitor {
    
    public PGOutputVisitor(Appendable appender) {
        super(appender);
    }
    
    @Override
    public void endVisit(final WindowClause x) {
    }
    
    @Override
    public boolean visit(final WindowClause x) {
        print("WINDOW ");
        x.getName().accept(this);
        print(" AS ");
        for (int i = 0; i < x.getDefinition().size(); ++i) {
            if (i != 0) {
                println(", ");
            }
            print("(");
            x.getDefinition().get(i).accept(this);
            print(")");
        }
        return false;
    }

    @Override
    public void endVisit(PGWithQuery x) {

    }

    @Override
    public boolean visit(PGWithQuery x) {
        x.getName().accept(this);

        if (x.getColumns().size() > 0) {
            print(" (");
            printAndAccept(x.getColumns(), ", ");
            print(")");
        }
        println();
        print("AS");
        println();
        print("(");
        incrementIndent();
        println();
        x.getQuery().accept(this);
        decrementIndent();
        println();
        print(")");

        return false;
    }

    @Override
    public void endVisit(PGWithClause x) {

    }

    @Override
    public boolean visit(PGWithClause x) {
        print("WITH");
        if (x.isRecursive()) {
            print(" RECURSIVE ");
        }
        incrementIndent();
        println();
        printlnAndAccept(x.getWithQuery(), ", ");
        decrementIndent();
        return false;
    }

    public boolean visit(PGSelectQueryBlock x) {
        if (x.getWith() != null) {
            x.getWith().accept(this);
            println();
        }

        print("SELECT ");

        printSelectList(x.getSelectList());

        if (x.getInto() != null) {
            println();
            if (x.getIntoOption() != null) {
                print(x.getIntoOption().name());
                print(" ");
            }

            x.getInto().accept(this);
        }

        if (x.getFrom() != null) {
            println();
            print("FROM ");
            x.getFrom().accept(this);
        }

        if (x.getWhere() != null) {
            println();
            print("WHERE ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
        }

        if (x.getGroupBy() != null) {
            println();
            x.getGroupBy().accept(this);
        }

        if (x.getWindow() != null) {
            println();
            x.getWindow().accept(this);
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (x.getLimit() != null) {
            println();
            print("LIMIT ");
            x.getLimit().accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(PGSelectQueryBlock x) {

    }

    @Override
    public void endVisit(PGParameter x) {

    }

    @Override
    public boolean visit(PGParameter x) {
        x.getName().accept(this);
        print(" ");

        x.getDataType().accept(this);

        return false;
    }

    @Override
    public boolean visit(PGFunctionTableSource x) {
        x.getExpr().accept(this);

        if (x.getAlias() != null) {
            print(" AS ");
            print(x.getAlias());
        }

        if (x.getParameters().size() > 0) {
            print('(');
            printAndAccept(x.getParameters(), ", ");
            print(')');
        }

        return false;
    }

    @Override
    public void endVisit(PGFunctionTableSource x) {

    }

	@Override
	public boolean visit(PGLimit x) {
	    x.getRowCount().accept(this);
	    if (x.getOffset() != null) {
	        print(" OFFSET ");
	        x.getOffset().accept(this);
	    }
		return false;
	}

	@Override
	public void endVisit(PGLimit x) {
		
	}

    @Override
    public void endVisit(PGTypeCastExpr x) {
        
    }

    @Override
    public boolean visit(PGTypeCastExpr x) {
        x.getExpr().accept(this);
        print("::");
        x.getDataType().accept(this);
        return false;
    }

    @Override
    public void endVisit(PGValuesQuery x) {
        
    }

    @Override
    public boolean visit(PGValuesQuery x) {
        print("VALUES(");
        printAndAccept(x.getValues(), ", ");
        print(")");
        return false;
    }
    
    @Override
    public void endVisit(PGExtractExpr x) {
        
    }
    
    @Override
    public boolean visit(PGExtractExpr x) {
        print("EXTRACT (");
        print(x.getField().name());
        print(" FROM ");
        x.getSource().accept(this);
        print(")");
        return false;
    }
    
    @Override
    public boolean visit(PGBoxExpr x) {
        print("BOX ");
        x.getValue().accept(this);
        return false;
    }

    @Override
    public void endVisit(PGBoxExpr x) {
        
    }
    
    @Override
    public boolean visit(PGPointExpr x) {
        print("POINT ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGPointExpr x) {
        
    }
    
    @Override
    public boolean visit(PGMacAddrExpr x) {
        print("macaddr ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGMacAddrExpr x) {
        
    }
    
    @Override
    public boolean visit(PGInetExpr x) {
        print("inet ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGInetExpr x) {
        
    }
    
    @Override
    public boolean visit(PGCidrExpr x) {
        print("cidr ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGCidrExpr x) {
        
    }
    
    @Override
    public boolean visit(PGPolygonExpr x) {
        print("polygon ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGPolygonExpr x) {
        
    }
    
    @Override
    public boolean visit(PGCircleExpr x) {
        print("circle ");
        x.getValue().accept(this);
        return false;
    }
    
    @Override
    public void endVisit(PGCircleExpr x) {
        
    }
    
    @Override
    public boolean visit(PGLineSegmentsExpr x) {
        print("lseg ");
        x.getValue().accept(this);
        return false;
    }

    @Override
    public void endVisit(PGIntervalExpr x) {

    }

    @Override
    public boolean visit(PGIntervalExpr x) {
        print("INTERVAL ");
        x.getValue().accept(this);
        return true;
    }

    @Override
    public void endVisit(PGLineSegmentsExpr x) {
        
    }
    
    @Override
    public boolean visit(SQLBinaryExpr x) {
        print("B'");
        print(x.getValue());
        print('\'');

        return false;
    }
}
