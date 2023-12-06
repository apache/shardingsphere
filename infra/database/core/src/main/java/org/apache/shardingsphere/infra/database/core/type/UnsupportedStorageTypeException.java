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

package org.apache.shardingsphere.infra.database.core.type;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.ConnectionSQLException;

/**
 * Unsupported storage type exception. When this exception is thrown, it means that the relevant jdbcUrl lacks the corresponding {@link DatabaseType} SPI implementation.
 * Assume that there is a jdbcUrl of `jdbc:vertica://node01.example.com:5433/dbname`, but there is no corresponding implementation of DatabaseType SPI.
 * User can temporarily create the class and implement {@link DatabaseType#getJdbcUrlPrefixes()} and {@link DatabaseType#getTrunkDatabaseType()}
 * to require the corresponding jdbcUrl use the SQL92 dialect.
 * // CHECKSTYLE:OFF
 * <pre class="code">
 * public final class VerticaDatabaseType implements DatabaseType {
 * <p/>
 *     &#064;Override
 *     public Collection<String> getJdbcUrlPrefixes() {
 *         return Collections.singleton("jdbc:vertica:");
 *     }
 * <p/>
 *     &#064;Override
 *     public Optional<DatabaseType> getTrunkDatabaseType() {
 *         return Optional.of(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
 *     }
 * <p/>
 *     &#064;Override
 *     public String getType() {
 *         return "Vertica";
 *     }
 * }
 * </pre>
 * // CHECKSTYLE:ON
 * To fully support the corresponding database dialect, user need to refer to `org.apache.shardingsphere:shardingsphere-infra-database-mysql` module to implement additional SPIs.
 *
 * @see org.apache.shardingsphere.infra.database.core.type.DatabaseType
 */
public final class UnsupportedStorageTypeException extends ConnectionSQLException {
    
    private static final long serialVersionUID = 8981789100727786183L;
    
    public UnsupportedStorageTypeException(final String url) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 40,
                "Unsupported storage type of URL `%s`. Add corresponding SPI implementation of `%s` to enable handling. Through reference SQL State: %s. Through reference Vendor Code: %s.",
                url, DatabaseType.class.getName(), XOpenSQLState.FEATURE_NOT_SUPPORTED.getValue(), 40);
    }
}
