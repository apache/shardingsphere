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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    /**
     * Build ShardingSphere schema.
     * 
     * @param materials schema builder materials
     * @return ShardingSphere schema
     * @throws SQLException SQL exception
     */
    public static ShardingSphereSchema build(final SchemaBuilderMaterials materials) throws SQLException {
        ShardingSphereSchema result = new ShardingSphereSchema();
        for (ShardingSphereRule rule : materials.getRules()) {
            if (rule instanceof TableContainedRule) {
                for (String table : ((TableContainedRule) rule).getTables()) {
                    if (!result.containsTable(table)) {
                        TableMetaDataBuilder.build(table, materials).ifPresent(optional -> result.put(table, optional));
                    }
                }
            }
        }
        appendRemainTables(materials, result);
        return result;
    }
    
    private static void appendRemainTables(final SchemaBuilderMaterials materials, final ShardingSphereSchema schema) throws SQLException {
        Collection<String> tableNames = new LinkedHashSet<>();
        for (Entry<String, DataSource> entry: materials.getDataSourceMap().entrySet()) {
            tableNames.addAll(SchemaMetaDataLoader.loadAllTableNames(entry.getValue(), materials.getDatabaseType()));
        }
        tableNames.removeAll(getExistedTables(materials.getRules(), schema));
        for (String each : tableNames) {
            schema.put(each, new TableMetaData());
        }
    }
    
    private static Collection<String> getExistedTables(final Collection<ShardingSphereRule> rules, final ShardingSphereSchema schema) {
        Collection<String> result = new LinkedHashSet<>();
        for (ShardingSphereRule each : rules) {
            if (each instanceof TableContainedRule) {
                result.addAll(((TableContainedRule) each).getTables());
            }
        }
        result.addAll(schema.getAllTableNames());
        return result;
    }
}
