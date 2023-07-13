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

package org.apache.shardingsphere.proxy.backend.postgresql.connector.jdbc.statement;

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.StatementMemoryStrictlyFetchSizeSetter;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Statement memory strictly fetch size setter for PostgreSQL.
 */
public final class PostgreSQLStatementMemoryStrictlyFetchSizeSetter implements StatementMemoryStrictlyFetchSizeSetter {
    
    @Override
    public void setFetchSize(final Statement statement) throws SQLException {
        int configuredFetchSize = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE);
        statement.setFetchSize(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE.getDefaultValue().equals(String.valueOf(configuredFetchSize)) ? 1 : configuredFetchSize);
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
