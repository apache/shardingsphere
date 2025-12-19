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

package org.apache.shardingsphere.proxy.backend.opengauss.response.header.query;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.postgresql.response.header.query.PostgreSQLQueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;

import java.sql.SQLException;

/**
 * Query header builder for openGauss.
 */
public final class OpenGaussQueryHeaderBuilder implements QueryHeaderBuilder {
    
    private final PostgreSQLQueryHeaderBuilder delegate = new PostgreSQLQueryHeaderBuilder();
    
    @Override
    public QueryHeader build(final QueryResultMetaData queryResultMetaData,
                             final ShardingSphereDatabase database, final String columnName, final String columnLabel, final int columnIndex) throws SQLException {
        return delegate.build(queryResultMetaData, database, columnName, columnLabel, columnIndex);
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
