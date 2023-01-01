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

package org.apache.shardingsphere.sqltranslator.jooq;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sqltranslator.exception.SQLTranslationException;
import org.apache.shardingsphere.sqltranslator.exception.syntax.UnsupportedTranslatedDatabaseException;
import org.jooq.SQLDialect;

import java.util.HashMap;
import java.util.Map;

/**
 * JOOQ SQL dialect registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JooQDialectRegistry {
    
    private static final Map<DatabaseType, SQLDialect> DATABASE_DIALECT_MAP = new HashMap<>();
    
    static {
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "PostgreSQL"), SQLDialect.POSTGRES);
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "MySQL"), SQLDialect.MYSQL);
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "MariaDB"), SQLDialect.MARIADB);
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "openGauss"), SQLDialect.POSTGRES);
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "H2"), SQLDialect.H2);
        DATABASE_DIALECT_MAP.put(TypedSPIRegistry.getRegisteredService(DatabaseType.class, "SQL92"), SQLDialect.DEFAULT);
    }
    
    /**
     * Get SQL dialect.
     *
     * @param databaseType database type
     * @return SQL dialect
     * @throws SQLTranslationException SQL translation exception
     */
    public static SQLDialect getSQLDialect(final DatabaseType databaseType) throws SQLTranslationException {
        SQLDialect result = DATABASE_DIALECT_MAP.get(databaseType);
        ShardingSpherePreconditions.checkState(null != result, () -> new UnsupportedTranslatedDatabaseException(databaseType));
        return result;
    }
}
