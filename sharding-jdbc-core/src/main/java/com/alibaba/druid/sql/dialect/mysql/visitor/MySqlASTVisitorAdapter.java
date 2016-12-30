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
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef.InValues;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef.LessThanValues;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetPasswordStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateTableSource;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

public class MySqlASTVisitorAdapter extends SQLASTVisitorAdapter implements MySqlASTVisitor {
    @Override
    public boolean visit(Limit x) {
        return true;
    }

    @Override
    public void endVisit(Limit x) {

    }

    @Override
    public boolean visit(MySqlTableIndex x) {
        return true;
    }

    @Override
    public void endVisit(MySqlTableIndex x) {

    }

    @Override
    public void endVisit(MySqlIntervalExpr x) {

    }

    @Override
    public boolean visit(MySqlIntervalExpr x) {

        return true;
    }

    @Override
    public void endVisit(MySqlExtractExpr x) {

    }

    @Override
    public boolean visit(MySqlExtractExpr x) {

        return true;
    }

    @Override
    public void endVisit(MySqlMatchAgainstExpr x) {

    }

    @Override
    public boolean visit(MySqlMatchAgainstExpr x) {

        return true;
    }

    @Override
    public void endVisit(MySqlPrepareStatement x) {

    }

    @Override
    public boolean visit(MySqlPrepareStatement x) {

        return true;
    }

    @Override
    public void endVisit(MySqlExecuteStatement x) {

    }

    @Override
    public boolean visit(MySqlExecuteStatement x) {

        return true;
    }

    @Override
    public void endVisit(MySqlDeleteStatement x) {

    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {

        return true;
    }

    @Override
    public void endVisit(MySqlInsertStatement x) {

    }

    @Override
    public boolean visit(MySqlInsertStatement x) {

        return true;
    }

    @Override
    public void endVisit(MySqlSelectGroupBy x) {

    }

    @Override
    public boolean visit(MySqlSelectGroupBy x) {

        return true;
    }
    
    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {

    }

    @Override
    public boolean visit(MySqlOutFileExpr x) {
        return true;
    }

    @Override
    public void endVisit(MySqlOutFileExpr x) {

    }

    @Override
    public boolean visit(MySqlUpdateStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUpdateStatement x) {

    }

    @Override
    public boolean visit(MySqlSetTransactionStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetTransactionStatement x) {

    }

    @Override
    public boolean visit(MySqlSetNamesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetNamesStatement x) {

    }

    @Override
    public boolean visit(MySqlSetCharSetStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetCharSetStatement x) {

    }

    @Override
    public boolean visit(MySqlUserName x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUserName x) {

    }

    @Override
    public boolean visit(MySqlUnionQuery x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUnionQuery x) {

    }

    @Override
    public boolean visit(MySqlUseIndexHint x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUseIndexHint x) {

    }

    @Override
    public boolean visit(MySqlIgnoreIndexHint x) {
        return true;
    }

    @Override
    public void endVisit(MySqlIgnoreIndexHint x) {

    }

    @Override
    public boolean visit(MySqlUnlockTablesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUnlockTablesStatement x) {

    }

    @Override
    public boolean visit(MySqlForceIndexHint x) {
        return true;
    }

    @Override
    public void endVisit(MySqlForceIndexHint x) {

    }

    @Override
    public boolean visit(MySqlCharExpr x) {
        return true;
    }

    @Override
    public void endVisit(MySqlCharExpr x) {

    }

    @Override
    public boolean visit(MySqlPartitioningDef x) {
        return true;
    }

    @Override
    public void endVisit(MySqlPartitioningDef x) {

    }

    @Override
    public boolean visit(LessThanValues x) {
        return true;
    }

    @Override
    public void endVisit(LessThanValues x) {

    }

    @Override
    public boolean visit(InValues x) {
        return true;
    }

    @Override
    public void endVisit(InValues x) {

    }

    @Override
    public boolean visit(MySqlSetPasswordStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetPasswordStatement x) {

    }

    @Override
    public boolean visit(MySqlHintStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlHintStatement x) {
        
    }
    
    @Override
    public boolean visit(MySqlSelectGroupByExpr x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSelectGroupByExpr x) {

    }

	@Override
	public boolean visit(MySqlParameter x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlParameter x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlWhileStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlWhileStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlIfStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlIfStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlElseIfStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlElseIfStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlElseStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlElseStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlCaseStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlCaseStatement x) {
		// TODO Auto-generated method stub
		
	}
    

	@Override
	public boolean visit(MySqlSelectIntoStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlSelectIntoStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlWhenStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlWhenStatement x) {
		// TODO Auto-generated method stub
		
	}
	//add:end

	@Override
	public boolean visit(MySqlLoopStatement x) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void endVisit(MySqlLoopStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlLeaveStatement x) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void endVisit(MySqlLeaveStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlIterateStatement x) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void endVisit(MySqlIterateStatement x) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean visit(MySqlRepeatStatement x) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void endVisit(MySqlRepeatStatement x) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    public boolean visit(MySqlUpdateTableSource x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUpdateTableSource x) {

    }
}
