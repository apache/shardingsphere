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

package com.dangdang.ddframe.rdb.sharding.merger.component.other;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.jdbc.adapter.AbstractForwardingResultSetAdapter;
import lombok.Getter;

/**
 * 原始结果集包装类.
 * 
 * @author gaohongtao
 */
public class WrapperResultSet extends AbstractForwardingResultSetAdapter {
    
    @Getter
    private final boolean isEmpty;
    
    private boolean isFirstNext;
    
    public WrapperResultSet(final ResultSet resultSet) throws SQLException {
        isEmpty = !resultSet.next();
        if (isEmpty) {
            return;
        }
        setDelegate(resultSet);
        increaseStat();
    }
    
    @Override
    public boolean next() throws SQLException {
        if (isEmpty) {
            return false;
        }
        if (!isFirstNext) {
            return isFirstNext = true;
        }
        return super.next();
    }
}
