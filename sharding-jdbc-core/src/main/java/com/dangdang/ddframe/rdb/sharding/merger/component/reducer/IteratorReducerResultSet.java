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

package com.dangdang.ddframe.rdb.sharding.merger.component.reducer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractReducerResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import lombok.extern.slf4j.Slf4j;

/**
 * 迭代归并结果集.
 *
 * @author gaohongtao
 */
@Slf4j
public class IteratorReducerResultSet extends AbstractReducerResultSetAdapter implements ReducerResultSet {
    
    private int resultSetIndex;
    
    private int currentResultSetOffset;
    
    @Override
    public void init(final List<ResultSet> preResultSet) {
        input(preResultSet);
        resultSetIndex++;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (null != getDelegate() && getDelegate().next()) {
            currentResultSetOffset++;
            log.trace(toString());
            return true;
        }
        if (resultSetIndex >= getInputResultSets().size()) {
            return false;
        }
        currentResultSetOffset = 1;
        ResultSet rs = getInputResultSets().get(resultSetIndex++);
        setDelegate(rs);
        log.trace(toString());
        return rs.next();
    }
    
    @Override
    // TODO 同样的toString问题
    public String toString() {
        return String.format("Current access %d of %d result set, offset is %d", resultSetIndex, getInputResultSets().size(), currentResultSetOffset);
    }
}
