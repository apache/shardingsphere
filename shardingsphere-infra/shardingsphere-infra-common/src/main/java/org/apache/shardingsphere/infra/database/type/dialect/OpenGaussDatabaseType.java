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

package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.dialect.OpenGaussDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.SchemaSupportedDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Database type of openGauss.
 */
public final class OpenGaussDatabaseType implements SchemaSupportedDatabaseType {
    
    private static final Map<String, Collection<String>> SYSTEM_DATABASE_SCHEMA_MAP = new HashMap<>();
    
    private static final Collection<String> SYSTEM_SCHEMAS = new HashSet<>(Arrays.asList("information_schema", "pg_catalog",
            "blockchain", "cstore", "db4ai", "dbe_perf", "dbe_pldebugger", "gaussdb", "oracle", "pkg_service", "snapshot", "sqladvisor", "dbe_pldeveloper", "pg_toast", "pkg_util", "shardingsphere"));
    
    static {
        SYSTEM_DATABASE_SCHEMA_MAP.put("postgres", SYSTEM_SCHEMAS);
    }
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton(String.format("jdbc:%s:", getType().toLowerCase()));
    }
    
    @Override
    public OpenGaussDataSourceMetaData getDataSourceMetaData(final String url, final String username) {
        return new OpenGaussDataSourceMetaData(url);
    }
    
    @Override
    public void handleRollbackOnly(final boolean rollbackOnly, final SQLStatement statement) throws SQLException {
        ShardingSpherePreconditions.checkState(!rollbackOnly || statement instanceof CommitStatement || statement instanceof RollbackStatement,
                () -> new SQLFeatureNotSupportedException("Current transaction is aborted, commands ignored until end of transaction block."));
    }
    
    @Override
    public Map<String, Collection<String>> getSystemDatabaseSchemaMap() {
        return SYSTEM_DATABASE_SCHEMA_MAP;
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return SYSTEM_SCHEMAS;
    }
    
    @Override
    public boolean isSchemaAvailable() {
        return true;
    }
    
    @Override
    public String getDefaultSchema() {
        return "public";
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
