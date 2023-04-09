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

package org.apache.shardingsphere.sqlfederation.row;

import org.apache.calcite.linq4j.Enumerator;
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
public final class SQLFederationRowEnumerator<T> implements Enumerator<T> {
    
    private final MergedResult mergedResult;
    
    private final Collection<Statement> statements;
    
    private T currentRow;
    
    private int size;
    
    public SQLFederationRowEnumerator(final MergedResult mergedResult, final int size, final Collection<Statement> statements) {
        this.mergedResult = mergedResult;
        this.size = size;
        this.statements = statements;
    }
    
    @Override
    public T current() {
        return currentRow;
    }
    
    @Override
    public boolean moveNext() {
        try {
            if (mergedResult.next()) {
                currentRow = getRows();
                return true;
            }
        } catch (SQLException ex) {
            throw new SQLWrapperException(ex);
        } finally {
            currentRow = null;
            return false;
        }
    }
    
    private T getRows() throws SQLException {
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = mergedResult.getValue(i + 1, Object.class);
        }
        return (T) result;
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
