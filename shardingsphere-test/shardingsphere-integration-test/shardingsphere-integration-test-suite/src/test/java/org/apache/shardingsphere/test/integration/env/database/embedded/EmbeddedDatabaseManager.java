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

package org.apache.shardingsphere.test.integration.env.database.embedded;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.database.embedded.type.MySQLEmbeddedDatabase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Embedded database manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmbeddedDatabaseManager {
    
    private static final Map<String, EmbeddedDatabase> EMBEDDED_DATABASES_CACHE = new ConcurrentHashMap<>();
    
    private static final Lock DATABASE_RESOURCE_LOCK = new ReentrantLock();
    
    /**
     * Start up embedded database.
     *
     * @param databaseType database type
     * @param scenario scenario
     * @param embeddedDatabaseProps embedded database distribution properties
     * @param port port
     */
    public static void startUp(final DatabaseType databaseType, final String scenario, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        String embeddedDatabaseKey = String.join("_", databaseType.getName(), scenario);
        if (EMBEDDED_DATABASES_CACHE.containsKey(embeddedDatabaseKey)) {
            return;
        }
        DATABASE_RESOURCE_LOCK.lock();
        try {
            if (EMBEDDED_DATABASES_CACHE.containsKey(embeddedDatabaseKey)) {
                return;
            }
            EmbeddedDatabase embeddedDatabase = newInstance(databaseType, embeddedDatabaseProps, port);
            embeddedDatabase.start();
            EMBEDDED_DATABASES_CACHE.put(embeddedDatabaseKey, embeddedDatabase);
        } finally {
            DATABASE_RESOURCE_LOCK.unlock();
        }
    }
    
    private static EmbeddedDatabase newInstance(final DatabaseType databaseType, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        if (databaseType instanceof MySQLDatabaseType) {
            return new MySQLEmbeddedDatabase(embeddedDatabaseProps, port);
        }
        throw new UnsupportedOperationException(String.format("Unsupported embedded database type: `%s`", databaseType.getName()));
    }
}
