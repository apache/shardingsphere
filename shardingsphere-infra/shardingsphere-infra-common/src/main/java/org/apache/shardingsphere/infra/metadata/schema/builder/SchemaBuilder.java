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
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {
    
    /**
     * Build kernel schema.
     *
     * @param tableMetaDataList table meta data list
     * @param rules sharding sphere rule
     * @return ShardingSphere schema
     */
    public static ShardingSphereSchema buildKernelSchema(final Collection<TableMetaData> tableMetaDataList, final Collection<ShardingSphereRule> rules) {
        return buildSchema(tableMetaDataList, each -> TableMetaDataBuilder.decorateKernelTableMetaData(each, rules));
    }
    
    /**
     * Build federation schema.
     *
     * @param tableMetaDataList table meta data list
     * @param rules sharding sphere rule
     * @return ShardingSphere schema
     */
    public static ShardingSphereSchema buildFederationSchema(final Collection<TableMetaData> tableMetaDataList, final Collection<ShardingSphereRule> rules) {
        return buildSchema(tableMetaDataList, each -> TableMetaDataBuilder.decorateFederationTableMetaData(each, rules));
    }
    
    private static ShardingSphereSchema buildSchema(final Collection<TableMetaData> tableMetaDataList, final Function<TableMetaData, TableMetaData> mapper) {
        Map<String, TableMetaData> tableMetaDataMap = tableMetaDataList.stream().map(mapper)
                .collect(Collectors.toMap(TableMetaData::getName, Function.identity(), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        return new ShardingSphereSchema(tableMetaDataMap);
    }
}
