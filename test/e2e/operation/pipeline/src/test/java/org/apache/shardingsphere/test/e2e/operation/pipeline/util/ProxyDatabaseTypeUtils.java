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

package org.apache.shardingsphere.test.e2e.operation.pipeline.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;

/**
 * Proxy database type utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyDatabaseTypeUtils {
    
    /**
     * Get proxy database type.
     *
     * @param databaseType database type
     * @return proxy database type
     */
    public static DatabaseType getProxyDatabaseType(final DatabaseType databaseType) {
        return getUnsupportedProxyDatabaseTypes().stream().anyMatch(each -> each.equals(databaseType.getTrunkDatabaseType().orElse(databaseType).getType()))
                ? TypedSPILoader.getService(DatabaseType.class, "MySQL")
                : databaseType;
    }
    
    private static Collection<String> getUnsupportedProxyDatabaseTypes() {
        return Collections.singleton("Oracle");
    }
}
