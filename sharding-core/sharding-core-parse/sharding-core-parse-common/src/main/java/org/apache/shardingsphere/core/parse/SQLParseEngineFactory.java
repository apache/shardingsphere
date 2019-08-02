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

package org.apache.shardingsphere.core.parse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL parse engine factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParseEngineFactory {
    
    private static final Map<String, SQLParseEngine> ENGINES = new ConcurrentHashMap<>();
    
    /**
     * Get SQL parse engine.
     *
     * @param databaseType database type
     * @return SQL parse engine
     */
    public static SQLParseEngine getSQLParseEngine(final DatabaseType databaseType) {
        if (ENGINES.containsKey(databaseType.getName())) {
            return ENGINES.get(databaseType.getName());
        }
        synchronized (ENGINES) {
            if (ENGINES.containsKey(databaseType.getName())) {
                return ENGINES.get(databaseType.getName());
            }
            SQLParseEngine result = new SQLParseEngine(databaseType);
            ENGINES.put(databaseType.getName(), result);
            return result;
        }
    }
}
