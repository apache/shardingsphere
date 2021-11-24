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

package org.apache.shardingsphere.infra.federation.context.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.federation.context.parser.dialect.OptimizerSQLDialectBuilderFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Optimizer parser context factory.
 */
@RequiredArgsConstructor
@Getter
public final class OptimizerParserContextFactory {
    
    /**
     * Create optimizer parser context map.
     *
     * @param metaDataMap meta data map
     * @return created optimizer parser context map
     */
    public static Map<String, OptimizerParserContext> create(final Map<String, ShardingSphereMetaData> metaDataMap) {
        Map<String, OptimizerParserContext> result = new HashMap<>();
        for (Entry<String, ShardingSphereMetaData> entry : metaDataMap.entrySet()) {
            DatabaseType databaseType = entry.getValue().getResource().getDatabaseType();
            result.put(entry.getKey(), new OptimizerParserContext(databaseType, createSQLDialectProperties(databaseType)));
        }
        return result;
    }
    
    private static Properties createSQLDialectProperties(final DatabaseType databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(OptimizerSQLDialectBuilderFactory.build(databaseType, result));
        return result;
    }
}
