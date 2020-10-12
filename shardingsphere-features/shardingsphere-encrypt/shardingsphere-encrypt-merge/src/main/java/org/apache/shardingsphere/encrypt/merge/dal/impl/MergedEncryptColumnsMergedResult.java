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

package org.apache.shardingsphere.encrypt.merge.dal.impl;

import org.apache.shardingsphere.infra.metadata.model.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;

import java.sql.SQLException;

/**
 * Merged encrypt column merged result.
 */
public final class MergedEncryptColumnsMergedResult extends EncryptColumnsMergedResult {
    
    private final QueryResult queryResult;
    
    public MergedEncryptColumnsMergedResult(final QueryResult queryResult, final SQLStatementContext sqlStatementContext, final SchemaMetaData schemaMetaData) {
        super(sqlStatementContext, schemaMetaData);
        this.queryResult = queryResult;
    }
    
    @Override
    protected boolean nextValue() throws SQLException {
        return queryResult.next();
    }
    
    @Override
    protected Object getOriginalValue(final int columnIndex, final Class<?> type) throws SQLException {
        return queryResult.getValue(columnIndex, type);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return queryResult.wasNull();
    }
}
