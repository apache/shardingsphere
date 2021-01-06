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

package org.apache.shardingsphere.infra.optimize.schema.row;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Calcite row enumerator.
 *
 */
public final class CalciteRowEnumerator implements Enumerator<Object[]> {
    
    private final Collection<QueryResult> queryResults = new LinkedList<>();
    
    private final Iterator<QueryResult> iterator;
    
    private QueryResult currentResultSet;
    
    private Object[] currentRow;
    
    public CalciteRowEnumerator(final Collection<QueryResult> queryResults) {
        this.queryResults.addAll(queryResults);
        iterator = this.queryResults.iterator();
        currentResultSet = iterator.next();
    }
    
    @Override
    public Object[] current() {
        return currentRow;
    }
    
    @Override
    public boolean moveNext() {
        try {
            return moveNext0();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private boolean moveNext0() throws SQLException {
        if (currentResultSet.next()) {
            setCurrentRow();
            return true;
        }
        if (!iterator.hasNext()) {
            currentRow = null;
            return false;
        }
        currentResultSet = iterator.next();
        if (currentResultSet.next()) {
            setCurrentRow();
            return true;
        }
        return false;
    }
    
    private void setCurrentRow() throws SQLException {
        int columnCount = currentResultSet.getMetaData().getColumnCount();
        currentRow = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            currentRow[i] = currentResultSet.getValue(i + 1, Object.class);
        }
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void close() {
        try {
            for (QueryResult each : queryResults) {
                each.close();
            }
            currentRow = null;
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
}
