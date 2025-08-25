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

package org.apache.shardingsphere.sqlfederation.compiler.compiler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL statement compiler engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementCompilerEngineFactory {
    
    private static final Map<String, SQLStatementCompilerEngine> COMPILER_ENGINES = new ConcurrentHashMap<>(1, 1F);
    
    /**
     * Get SQL statement compiler engine.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param cacheOption execution plan cache option
     * @return SQL statement compiler engine
     */
    public static SQLStatementCompilerEngine getSQLStatementCompilerEngine(final String databaseName, final String schemaName, final SQLFederationCacheOption cacheOption) {
        String cacheKey = databaseName + "." + schemaName;
        SQLStatementCompilerEngine result = COMPILER_ENGINES.get(cacheKey);
        if (null == result) {
            result = COMPILER_ENGINES.computeIfAbsent(cacheKey, unused -> new SQLStatementCompilerEngine(cacheOption));
        } else if (isOnlyModifyMaximumSizeConfig(cacheOption, result)) {
            result.updateCacheOption(cacheOption);
        } else if (!cacheOption.equals(result.getCacheOption())) {
            result = new SQLStatementCompilerEngine(cacheOption);
            COMPILER_ENGINES.put(cacheKey, result);
        }
        return result;
    }
    
    private static boolean isOnlyModifyMaximumSizeConfig(final SQLFederationCacheOption cacheOption, final SQLStatementCompilerEngine compilerEngine) {
        return cacheOption.getInitialCapacity() == compilerEngine.getCacheOption().getInitialCapacity()
                && cacheOption.getMaximumSize() != compilerEngine.getCacheOption().getMaximumSize();
    }
}
