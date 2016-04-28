/**
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

package com.dangdang.ddframe.rdb.sharding.merger.component.coupling;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractForwardingResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ComponentResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.Limit;
import lombok.extern.slf4j.Slf4j;

/**
 * 限制结果集.
 * 
 * @author gaohongtao
 */
@Slf4j
public class LimitCouplingResultSet extends AbstractForwardingResultSetAdapter implements CouplingResultSet {
    
    private final Limit limit;
    
    private int rowNumber;
    
    private ResultSet preResultSet;
    
    private boolean initial;
    
    public LimitCouplingResultSet(final Limit limit) {
        this.limit = limit;
    }
    
    @Override
    public ComponentResultSet init(final ResultSet preResultSet) {
        setDelegate(preResultSet);
        this.preResultSet = preResultSet;
        return this;
    }
    
    @Override
    public boolean next() throws SQLException {
        boolean result = true;
        if (!initial) {
            result = skipOffset();
        }
        if (!result) {
            return false;
        }
        result = ++rowNumber <= limit.getRowCount() && preResultSet.next();
        if (result) {
            increaseStat();
        }
        return result;
    }
    
    private boolean skipOffset() throws SQLException {
        boolean result = true;
        for (int i = 0; i < limit.getOffset(); i++) {
            result = preResultSet.next();
            if (!result) {
                break;
            }
        }
        initial = true;
        return result;
    }
}
