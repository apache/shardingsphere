/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sqlfederation.executor.row;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * SQL federation row enumerator.
 * 
 * @param <T> type of row
 */
@RequiredArgsConstructor
public final class SQLFederationRowEnumerator<T> implements Enumerator<T> {
    
    private final MergedResult queryResult;
    
    private final QueryResultMetaData metaData;
    
    private final Collection<Statement> statements;
    
    private T currentRow;
    
    @Override
    public T current() {
        return currentRow;
    }
    
    @SneakyThrows
    @Override
    public boolean moveNext() {
        return moveNext0();
    }
    
    private boolean moveNext0() throws SQLException {
        if (queryResult.next()) {
            setCurrentRow();
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private void setCurrentRow() throws SQLException {
        if (1 == metaData.getColumnCount()) {
            this.currentRow = (T) queryResult.getValue(1, Object.class);
        } else {
            Object[] rowValues = new Object[metaData.getColumnCount()];
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                rowValues[i] = queryResult.getValue(i + 1, Object.class);
            }
            this.currentRow = (T) rowValues;
        }
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void close() {
        try {
            for (Statement each : statements) {
                each.close();
            }
            currentRow = null;
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
}
