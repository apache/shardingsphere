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

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractResultSetAdapter;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 迭代归并结果集.
 *
 * @author gaohongtao
 */
@Slf4j
public class IteratorReducerResultSet extends AbstractResultSetAdapter implements ReducerResultSet {
    
    private int resultSetIndex;
    
    private int currentResultSetOffset;
    
    @Override
    public void init(final List<ResultSet> preResultSet) {
        setResultSets(preResultSet);
        resultSetIndex++;
        setCurrentResultSet(preResultSet.get(0));
    }
    
    @Override
    public boolean next() throws SQLException {
        if (null != getCurrentResultSet() && getCurrentResultSet().next()) {
            currentResultSetOffset++;
            log.trace(toString());
            return true;
        }
        if (resultSetIndex >= getResultSets().size()) {
            return false;
        }
        currentResultSetOffset = 1;
        ResultSet rs = getResultSets().get(resultSetIndex++);
        setCurrentResultSet(rs);
        log.trace(toString());
        return rs.next();
    }
    
    @Override
    public String toString() {
        return String.format("Current access %d of %d result set, offset is %d", resultSetIndex, getResultSets().size(), currentResultSetOffset);
    }
}
