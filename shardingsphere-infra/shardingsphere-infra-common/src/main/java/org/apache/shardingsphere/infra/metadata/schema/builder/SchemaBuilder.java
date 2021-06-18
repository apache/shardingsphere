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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
    
    static {
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }

    /**
     * build actual and logic table meta data.
     *
     * @param materials schema builder materials
     * @return actual and logic table meta data
     * @throws SQLException SQL exception
     */
    public static Map<Map<String, TableMetaData>, Map<String, TableMetaData>> build(final SchemaBuilderMaterials materials) throws SQLException {
        Map<Map<String, TableMetaData>, Map<String, TableMetaData>> result = new HashMap<>();
        Map<String, TableMetaData> actualTableMetaMap = new HashMap<>();
        Map<String, TableMetaData> logicTableMetaMap = new HashMap<>();
        addRuleConfiguredTables(materials, logicTableMetaMap);
        appendRemainTables(materials, actualTableMetaMap);
        result.put(actualTableMetaMap, logicTableMetaMap);
        return result;
    }
    
    private static void addRuleConfiguredTables(final SchemaBuilderMaterials materials, final Map<String, TableMetaData> logicTableMetaMap) throws SQLException {
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (!logicTableMetaMap.containsKey(table)) {
                        TableMetaDataBuilder.load(table, materials).map(optional -> logicTableMetaMap.put(table, optional));
                    }
                }
            }
        }
    }
    
    private static void appendRemainTables(final SchemaBuilderMaterials materials, final Map<String, TableMetaData> actualTableMetaMap) throws SQLException {
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (!actualTableMetaMap.containsKey(table)) {
                        TableMetaData metaData = TableMetaDataBuilder.decorate(table, actualTableMetaMap.get(table), materials.getRules());
                        actualTableMetaMap.put(table, metaData);
                    }
                }
            }
        }
    }
}
