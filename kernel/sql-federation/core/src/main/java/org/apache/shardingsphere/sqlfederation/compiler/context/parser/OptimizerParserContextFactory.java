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

package org.apache.shardingsphere.sqlfederation.compiler.context.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.context.parser.dialect.OptimizerSQLDialectBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer parser context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerParserContextFactory {
    
    /**
     * Create optimizer parser context map.
     *
     * @param databases databases
     * @return created optimizer parser context map
     */
    public static Map<String, OptimizerParserContext> create(final Map<String, ShardingSphereDatabase> databases) {
        Map<String, OptimizerParserContext> result = new ConcurrentHashMap<>();
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            DatabaseType databaseType = DatabaseTypeEngine.getTrunkDatabaseType(entry.getValue().getProtocolType().getType());
            result.put(entry.getKey(), new OptimizerParserContext(databaseType, createSQLDialectProperties(databaseType)));
        }
        return result;
    }
    
    /**
     * Create optimizer parser context.
     * 
     * @param databaseType database type
     * @return optimizer parser context
     */
    public static OptimizerParserContext create(final DatabaseType databaseType) {
        return new OptimizerParserContext(databaseType, createSQLDialectProperties(databaseType));
    }
    
    private static Properties createSQLDialectProperties(final DatabaseType databaseType) {
        return DatabaseTypedSPILoader.getService(OptimizerSQLDialectBuilder.class, databaseType).build();
    }
}
