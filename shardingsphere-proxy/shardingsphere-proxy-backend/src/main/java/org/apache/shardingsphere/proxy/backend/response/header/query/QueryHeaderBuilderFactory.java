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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.MySQLQueryHeaderBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Factory for {@link QueryHeaderBuilder}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryHeaderBuilderFactory {
    
    private static final Map<String, QueryHeaderBuilder> QUERY_HEADER_BUILDERS = new HashMap<>();
    
    private static final QueryHeaderBuilder DEFAULT_QUERY_HEADER_BUILDER = new MySQLQueryHeaderBuilder();
    
    static {
        ServiceLoader.load(QueryHeaderBuilder.class).forEach(each -> QUERY_HEADER_BUILDERS.put(each.getDatabaseType(), each));
    }
    
    /**
     * Get {@link QueryHeaderBuilder} for specified database type.
     *
     * @param databaseType database type
     * @return query header builder
     */
    public static QueryHeaderBuilder getQueryHeaderBuilder(final DatabaseType databaseType) {
        return QUERY_HEADER_BUILDERS.getOrDefault(null == databaseType ? null : databaseType.getName(), DEFAULT_QUERY_HEADER_BUILDER);
    }
}
