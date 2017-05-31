/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling;

import com.dangdang.ddframe.rdb.sharding.merger.resultset.delegate.AbstractDelegateResultSet;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * 分页限制条件的连接结果集.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class LimitCouplingResultSet extends AbstractDelegateResultSet {
    
    private final Limit limit;
    
    private int rowNumber;
    
    public LimitCouplingResultSet(final ResultSet resultSet, final SQLStatement sqlStatement) throws SQLException {
        super(Collections.singletonList(resultSet));
        limit = sqlStatement.getLimit();
    }
    
    @Override
    protected boolean firstNext() throws SQLException {
        return skipOffset() && doNext();
    }
    
    private boolean skipOffset() throws SQLException {
        for (int i = 0; i < limit.getOffset(); i++) {
            if (!getDelegate().next()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected boolean afterFirstNext() throws SQLException {
        return doNext();
    }
    
    private boolean doNext() throws SQLException {
        return ++rowNumber <= limit.getRowCount() && getDelegate().next();
    }
}
