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

import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Collections;
import java.util.List;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Merged result for show databases.
 *
 * @author chenqingyang
 * @author xiayan
 */
public final class ShowDatabasesMergedResult extends LocalMergedResultAdapter implements MergedResult {
    
    private final List<String> schemas;
    
    private int currentIndex;

    public ShowDatabasesMergedResult() {
        this(Collections.singletonList(ShardingConstant.LOGIC_SCHEMA_NAME));
    }

    public ShowDatabasesMergedResult(final List<String> schemas) {
        this.schemas = schemas;
        this.currentIndex = 0;
    }

    public ShowDatabasesMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults)throws SQLException {
        this(convertToScheme(queryResults));
    }

    private static List<String> convertToScheme(final List<QueryResult> queryResults) throws SQLException {
        final LinkedList<String> result = new LinkedList<>();
        for (QueryResult queryResult : queryResults) {
            while (queryResult.next()) {
                result.add((String) queryResult.getValue(1, String.class));
            }
        }
        return new ArrayList<>(result);
    }
    
    @Override
    public boolean next() {
        return currentIndex++ < schemas.size();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Blob.class == type || Clob.class == type || Reader.class == type || InputStream.class == type || SQLXML.class == type) {
            throw new SQLFeatureNotSupportedException();
        }
        return schemas.get(currentIndex - 1);
    }
}
