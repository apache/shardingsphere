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

package com.dangdang.ddframe.rdb.sharding.merger;

import com.dangdang.ddframe.rdb.sharding.merger.resultset.delegate.AbstractDelegateResultSet;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分片结果集集合.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingResultSets {
    
    private final List<ResultSet> resultSets;
    
    private final Type type;
    
    public ShardingResultSets(final List<ResultSet> resultSets) throws SQLException {
        this.resultSets = filterResultSets(resultSets);
        type = generateType();
    }
    
    private List<ResultSet> filterResultSets(final List<ResultSet> resultSets) throws SQLException {
        List<ResultSet> result = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            if (each.next()) {
                result.add(new WrapperResultSet(each));
            }
        }
        return result;
    }
    
    private Type generateType() {
        if (this.resultSets.isEmpty()) {
            return Type.EMPTY;
        } else if (1 == this.resultSets.size()) {
            return Type.SINGLE;
        } else {
            return Type.MULTIPLE;
        }
    }
    
    enum Type {
        EMPTY, SINGLE, MULTIPLE
    }
    
    private static final class WrapperResultSet extends AbstractDelegateResultSet {
        
        private WrapperResultSet(final ResultSet resultSetWhenNextOnce) throws SQLException {
            super(Collections.singletonList(resultSetWhenNextOnce));
        }
        
        @Override
        protected boolean firstNext() throws SQLException {
            return true;
        }
        
        @Override
        protected boolean afterFirstNext() throws SQLException {
            return getDelegate().next();
        }
    }
}
