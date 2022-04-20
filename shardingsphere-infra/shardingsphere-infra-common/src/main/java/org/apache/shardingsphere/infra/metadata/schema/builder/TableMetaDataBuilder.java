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
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.SchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table meta data builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataBuilder {
    
    static {
        ShardingSphereServiceLoader.register(RuleBasedSchemaMetaDataBuilder.class);
    }
    
    /**
     * Load table metadata.
     *
     * @param tableNames table name collection
     * @param materials schema builder materials
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, SchemaMetaData> load(final Collection<String> tableNames, final SchemaBuilderMaterials materials) throws SQLException {
        Collection<SchemaMetaData> schemaMetaData = new LinkedList<>();
        for (Entry<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> entry : OrderedSPIRegistry.getRegisteredServices(RuleBasedSchemaMetaDataBuilder.class, materials.getRules()).entrySet()) {
            if (entry.getKey() instanceof TableContainedRule) {
                TableContainedRule rule = (TableContainedRule) entry.getKey();
                RuleBasedSchemaMetaDataBuilder<TableContainedRule> builder = entry.getValue();
                Collection<String> ruleTables = rule.getTables();
                Collection<String> needLoadTables = tableNames.stream().filter(ruleTables::contains)
                        .filter(each -> schemaMetaData.stream().noneMatch(schema -> schema.getTables().containsKey(each))).collect(Collectors.toList());
                if (!needLoadTables.isEmpty()) {
                    schemaMetaData.addAll(builder.build(needLoadTables, rule, materials));
                }
            }
        }
        return schemaMetaData.stream().collect(Collectors.toMap(SchemaMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
}
