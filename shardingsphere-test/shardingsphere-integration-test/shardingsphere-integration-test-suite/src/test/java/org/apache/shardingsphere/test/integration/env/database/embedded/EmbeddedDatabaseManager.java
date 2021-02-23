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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Map;
import java.util.Properties;
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
    
    static {
        ShardingSphereServiceLoader.register(EmbeddedDatabase.class);
    }
    
    /**
     * Start up embedded database.
     *
     * @param databaseType database type
     * @param scenario scenario
     * @param embeddedDatabaseProps embedded database distribution properties
     * @param port port
     */
    public static void startUp(final String databaseType, final String scenario, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        DatabaseType databaseTypeImpl = DatabaseTypeRegistry.getActualDatabaseType(databaseType);
        if (databaseTypeImpl instanceof H2DatabaseType) {
            return;
        }
        String embeddedDatabaseKey = databaseType;
        if (EMBEDDED_DATABASES_CACHE.containsKey(embeddedDatabaseKey)) {
            return;
        }
        try {
            DATABASE_RESOURCE_LOCK.lock();
            startUpSafely(embeddedDatabaseKey, databaseType, embeddedDatabaseProps, port);
        } finally {
            DATABASE_RESOURCE_LOCK.unlock();
        }
    }
    
    //CHECKSTYLE:OFF
    private static void startUpSafely(final String embeddedDatabaseKey, final String databaseType, final EmbeddedDatabaseDistributionProperties embeddedDatabaseProps, final int port) {
        int retries = 3;
        do {
            try {
                if (EMBEDDED_DATABASES_CACHE.containsKey(embeddedDatabaseKey)) {
                    return;
                }
                EmbeddedDatabase embeddedDatabase = TypedSPIRegistry.getRegisteredService(EmbeddedDatabase.class, databaseType, new Properties());
                Runtime.getRuntime().addShutdownHook(new Thread(embeddedDatabase::stop));
                embeddedDatabase.start(embeddedDatabaseProps, port);
                EMBEDDED_DATABASES_CACHE.put(embeddedDatabaseKey, embeddedDatabase);
                break;
            } catch (Throwable e) {
                retries--;
            }
        } while (retries != 0);
        //CHECKSTYLE:ON
    }
}
