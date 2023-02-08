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

package org.apache.shardingsphere.single.metadata;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableMetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.RuleBasedSchemaMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.rule.SingleRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema meta data decorator for single.
 */
public final class SingleSchemaMetaDataDecorator implements RuleBasedSchemaMetaDataDecorator<SingleRule> {
    
    @Override
    public Map<String, SchemaMetaData> decorate(final Map<String, SchemaMetaData> schemaMetaDataMap, final SingleRule rule, final GenericSchemaBuilderMaterial material) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(schemaMetaDataMap.size(), 1);
        for (Entry<String, SchemaMetaData> entry : schemaMetaDataMap.entrySet()) {
            DatabaseType databaseType = material.getStorageTypes().get(entry.getKey());
            DataSource dataSource = material.getDataSourceMap().get(entry.getKey());
            TableMetaDataReviseEngine<SingleRule> tableMetaDataReviseEngine = new TableMetaDataReviseEngine<>(rule, databaseType, dataSource);
            Collection<TableMetaData> tables = entry.getValue().getTables().stream().map(tableMetaDataReviseEngine::revise).collect(Collectors.toList());
            result.put(entry.getKey(), new SchemaMetaData(entry.getKey(), tables));
        }
        return result;
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
