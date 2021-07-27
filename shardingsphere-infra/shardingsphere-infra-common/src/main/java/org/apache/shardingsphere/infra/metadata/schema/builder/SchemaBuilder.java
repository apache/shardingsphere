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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public static Map<TableMetaData, TableMetaData> build(final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, TableMetaData> actualTableMetaMap = buildActualTableMetaDataMap(materials);
        Map<String, TableMetaData> logicTableMetaMap = buildLogicTableMetaDataMap(materials, actualTableMetaMap);
        Map<TableMetaData, TableMetaData> tableMetaDataMap = new HashMap<>(actualTableMetaMap.size(), 1);
        for (Map.Entry<String, TableMetaData> entry : actualTableMetaMap.entrySet()) {
            tableMetaDataMap.put(entry.getValue(), logicTableMetaMap.getOrDefault(entry.getKey(), entry.getValue()));
        }
        return tableMetaDataMap;
    }

    private static Map<String, TableMetaData> buildActualTableMetaDataMap(final SchemaBuilderMaterials materials) throws SQLException {
        Map<String, Collection<DataNode>> logicTable2DataNodes = getLogicTable2DataNodes(materials);

        Optional<DialectTableMetaDataLoader> dialectLoader = SchemaBuilderWithDialectLoader.findDialectTableMetaDataLoader(materials);
        Map<String, TableMetaData> result;
        if (dialectLoader.isPresent()) {
            result = SchemaBuilderWithDialectLoader.build(dialectLoader.get(), EXECUTOR_SERVICE, materials, logicTable2DataNodes);
        } else {
            result = SchemaBuilderWithDefaultLoader.build(EXECUTOR_SERVICE, materials, logicTable2DataNodes);
        }
        return result;
    }

    private static Map<String, Collection<DataNode>> getLogicTable2DataNodes(final SchemaBuilderMaterials materials) {
        List<DataNodeContainedRule> dataNodeContainedRuleList = materials.getRules().stream()
                .filter(each -> each instanceof DataNodeContainedRule)
                .map(each -> (DataNodeContainedRule) each)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataNodeContainedRuleList)) {
            return Collections.emptyMap();
        }
        Map<String, Collection<DataNode>> logicTable2DataNodes = dataNodeContainedRuleList.stream()
                .flatMap(each -> each.getAllDataNodes().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                    Collection<DataNode> result = new LinkedList<>(a);
                    result.addAll(b);
                    return result;
                }));

        Map<String, Collection<String>> dataSourceContainedMap = materials.getRules().stream()
                .filter(each -> each instanceof DataSourceContainedRule)
                .flatMap(each -> ((DataSourceContainedRule) each).getDataSourceMapper().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                    Collection<String> result = new ArrayList<>(a.size() + b.size());
                    result.addAll(a);
                    result.addAll(b);
                    return result;
                }));
        if (MapUtils.isEmpty(dataSourceContainedMap)) {
            return logicTable2DataNodes;
        }

        Map<String, Collection<DataNode>> replaceResult = new HashMap<>(logicTable2DataNodes.size(), 1);
        for (Map.Entry<String, Collection<DataNode>> entry : logicTable2DataNodes.entrySet()) {
            Collection<DataNode> dataNodes = entry.getValue();
            Collection<DataNode> newDataNodeList = new LinkedList<>();
            for (DataNode each : dataNodes) {
                Collection<String> toReplaceDataSourceNames = dataSourceContainedMap.get(each.getDataSourceName());
                if (CollectionUtils.isEmpty(toReplaceDataSourceNames)) {
                    newDataNodeList.add(each);
                } else {
                    for (String newDataSourceName : toReplaceDataSourceNames) {
                        newDataNodeList.add(new DataNode(newDataSourceName, each.getTableName()));
                    }
                }
            }
            replaceResult.put(entry.getKey(), newDataNodeList);
        }
        return replaceResult;
    }

    private static Map<String, TableMetaData> buildLogicTableMetaDataMap(final SchemaBuilderMaterials materials,
            final Map<String, TableMetaData> tables) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(materials.getRules().size(), 1);

        Collection<String> ruleLogicTables = materials.getRules().stream()
                .filter(rule -> rule instanceof TableContainedRule)
                .flatMap(rule -> ((TableContainedRule) rule).getTables().stream())
                .collect(Collectors.toSet());
        for (String table : ruleLogicTables) {
            TableMetaData tableMetaData = tables.get(table);
            if (null != tableMetaData) {
                result.put(table, TableMetaDataBuilder.decorate(table, tableMetaData, materials.getRules()));
            }
        }
        return result;
    }
}
