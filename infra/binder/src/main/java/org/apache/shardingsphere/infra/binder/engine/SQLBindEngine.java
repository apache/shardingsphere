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

package org.apache.shardingsphere.infra.binder.engine;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.List;

/**
 * SQL bind engine.
 */
public final class SQLBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String defaultDatabaseName;
    
    public SQLBindEngine(final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        this.metaData = metaData;
        this.defaultDatabaseName = defaultDatabaseName;
    }
    
    /**
     * Bind SQL statement with metadata.
     *
     * @param sqlStatement SQL statement
     * @param params parameters
     * @return SQL statement context
     */
    public SQLStatementContext bind(final SQLStatement sqlStatement, final List<Object> params) {
        SQLStatement buoundedSQLStatement = bind(sqlStatement, metaData, defaultDatabaseName);
        return SQLStatementContextFactory.newInstance(metaData, params, buoundedSQLStatement, defaultDatabaseName);
    }
    
    private SQLStatement bind(final SQLStatement statement, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        if (statement instanceof SelectStatement) {
            return new SelectStatementBinder().bind((SelectStatement) statement, metaData, defaultDatabaseName);
        }
        return statement;
    }
}
