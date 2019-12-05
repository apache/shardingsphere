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

package org.apache.shardingsphere.core.merge.dal.show;

import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Merged result for show databases.
 *
 * @author chenqingyang
 * @author xiayan
 */
public final class ShowDatabasesMergedResult extends LocalMergedResultAdapter implements MergedResult {
    
    private final Iterator<String> schemas;
    
    private String currentSchema;
    
    public ShowDatabasesMergedResult(final Collection<String> schemas) {
        this.schemas = schemas.iterator();
    }
    
    public ShowDatabasesMergedResult(final List<QueryResult> queryResults)throws SQLException {
        this(convertToScheme(queryResults));
    }
    
    private static Collection<String> convertToScheme(final List<QueryResult> queryResults) throws SQLException {
        Collection<String> result = new LinkedList<>();
        for (QueryResult queryResult : queryResults) {
            while (queryResult.next()) {
                result.add((String) queryResult.getValue(1, String.class));
            }
        }
        return result;
    }
    
    @Override
    public boolean next() {
        if (schemas.hasNext()) {
            currentSchema = schemas.next();
            return true;
        }
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Blob.class == type || Clob.class == type || Reader.class == type || InputStream.class == type || SQLXML.class == type) {
            throw new SQLFeatureNotSupportedException();
        }
        return currentSchema;
    }
}
