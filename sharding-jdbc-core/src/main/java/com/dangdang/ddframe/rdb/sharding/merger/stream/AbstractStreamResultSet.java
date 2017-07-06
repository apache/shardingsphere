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

package com.dangdang.ddframe.rdb.sharding.merger.stream;

import com.dangdang.ddframe.rdb.sharding.merger.AbstractDelegateResultSet;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 流式结果集抽象类.
 * 
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractStreamResultSet extends AbstractDelegateResultSet {
    
    private boolean beforeFirst = true;
    
    public AbstractStreamResultSet(final List<ResultSet> resultSets) throws SQLException {
        super(resultSets);
    }
    
    @Override
    public final boolean next() throws SQLException {
        boolean result = beforeFirst ? firstNext() : afterFirstNext();
        beforeFirst = false;
        return result;
    }
    
    protected abstract boolean firstNext() throws SQLException;
    
    protected abstract boolean afterFirstNext() throws SQLException;
}
