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

package com.dangdang.ddframe.rdb.sharding.merger.aggregation;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResultSetAggregationValue implements AggregationValue {
    
    private final ResultSet resultSet;
    
    @Override
    public Comparable<?> getValue(final ResultSetQueryIndex resultSetQueryIndex) throws SQLException {
        return resultSetQueryIndex.isQueryBySequence()
                ? (Comparable<?>) resultSet.getObject(resultSetQueryIndex.getQueryIndex()) : getQueryNameValue(resultSetQueryIndex.getQueryName());
    }
    
    private Comparable<?> getQueryNameValue(final String queryName) throws SQLException {
        try {
            return (Comparable<?>) resultSet.getObject(queryName);
        } catch (final SQLException ex) {
            return (Comparable<?>) resultSet.getObject(SQLUtil.getExactlyValue(queryName));
        }
    }
}
