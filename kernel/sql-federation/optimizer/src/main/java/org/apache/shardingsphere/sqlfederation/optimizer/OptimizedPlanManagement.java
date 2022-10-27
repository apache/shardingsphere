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

package org.apache.shardingsphere.sqlfederation.optimizer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Provide cache for storing optimized plan.
 */
@Getter
public class OptimizedPlanManagement {
    
    public static final long CAPACITY = 2000;
    
    private static final int EXPIRETIME = 1;
    
    private final Cache<String, SQLOptimizeContext> cache;
    
    private final SQLOptimizeEngine optimizer;
    
    private final SqlValidator validator;
    
    private final SqlToRelConverter converter;
    
    public OptimizedPlanManagement(final SQLOptimizeEngine optimizer, final SqlValidator validator, final SqlToRelConverter converter) {
        this.cache = buildCache();
        this.optimizer = optimizer;
        this.validator = validator;
        this.converter = converter;
    }
    
    private Cache<String, SQLOptimizeContext> buildCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(EXPIRETIME, TimeUnit.MILLISECONDS)
                .maximumSize(CAPACITY)
                .build();
    }
    
    /**
     * Get rel node from cache by sql node.
     *
     * @param sqlNode ast node
     * @param parameters sql parameters
     * @param useCache whether use cache
     * @return rel node
     */
    public SQLOptimizeContext get(final SqlNode sqlNode, final Map<String, Object> parameters, final boolean useCache) {
        if (useCache) {
            return optimizer.optimize(sqlNode);
        }
        SQLOptimizeContext result = cache.getIfPresent(new SQLInfo(sqlNode, parameters).toString());
        if (null == result) {
            result = optimizer.optimize(sqlNode);
            cache.put(new SQLInfo(sqlNode, parameters).toString(), result);
            return result;
        }
        return result;
    }
    
    @AllArgsConstructor
    private class SQLInfo {
        
        private SqlNode sqlNode;
        
        private Map<String, Object> parameters;
        
        @Override
        public String toString() {
            return "SQLInfo{" + "sqlNode=" + sqlNode.toString() + ", parameters=" + parameters.toString() + '}';
        }
    }
}
