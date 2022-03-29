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

import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.database.metadata.dialect.PostgreSQLDataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database type of PostgreSQL.
 */
public final class PostgreSQLDatabaseType implements DatabaseType {
    
    private static final Collection<String> SYSTEM_DATABASES = Sets.newHashSet("postgres");
    
    private static final Collection<String> SYSTEM_SCHEMAS = Sets.newHashSet("information_schema", "pg_catalog");
    
    @Override
    public String getName() {
        return "PostgreSQL";
    }
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton(String.format("jdbc:%s:", getName().toLowerCase()));
    }
    
    @Override
    public PostgreSQLDataSourceMetaData getDataSourceMetaData(final String url, final String username) {
        return new PostgreSQLDataSourceMetaData(url);
    }
    
    @Override
    public Optional<String> getDataSourceClassName() {
        return Optional.of("org.postgresql.ds.PGSimpleDataSource");
    }
    
    @Override
    public void handleRollbackOnly(final boolean rollbackOnly, final SQLStatement statement) throws SQLException {
        if (rollbackOnly && !(statement instanceof CommitStatement) && !(statement instanceof RollbackStatement)) {
            throw new SQLFeatureNotSupportedException("ERROR:  current transaction is aborted, commands ignored until end of transaction block.");
        }
    }
    
    @Override
    public Collection<String> getSystemDatabases() {
        return SYSTEM_DATABASES;
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return SYSTEM_SCHEMAS;
    }
}
