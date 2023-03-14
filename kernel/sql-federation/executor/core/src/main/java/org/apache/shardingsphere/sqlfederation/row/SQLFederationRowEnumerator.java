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
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

/**
 * SQL federation row enumerator.
 */
public final class SQLFederationRowEnumerator<T> implements Enumerator<T> {
    
    private final Collection<T> rows;
    
    private final Collection<Statement> statements;
    
    private Iterator<T> iterator;
    
    private T currentRow;
    
    public SQLFederationRowEnumerator(final Collection<T> rows, final Collection<Statement> statements) {
        this.rows = rows;
        this.statements = statements;
        iterator = rows.iterator();
    }
    
    @Override
    public T current() {
        return currentRow;
    }
    
    @Override
    public boolean moveNext() {
        if (iterator.hasNext()) {
            currentRow = iterator.next();
            return true;
        }
        currentRow = null;
        iterator = rows.iterator();
        return false;
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
            iterator = rows.iterator();
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
}
