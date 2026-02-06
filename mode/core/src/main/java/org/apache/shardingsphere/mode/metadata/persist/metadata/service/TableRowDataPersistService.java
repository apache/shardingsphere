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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlRowStatisticsSwapper;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsDataNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsTableNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Table row data persist service.
 */
@RequiredArgsConstructor
public final class TableRowDataPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist table row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    public void persist(final String databaseName, final String schemaName, final String tableName, final Collection<YamlRowStatistics> rows) {
        if (rows.isEmpty()) {
            repository.persist(NodePathGenerator.toPath(new StatisticsTableNodePath(databaseName, schemaName, tableName.toLowerCase())), "");
        } else {
            rows.forEach(each -> repository.persist(NodePathGenerator.toPath(new StatisticsDataNodePath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey())),
                    YamlEngine.marshal(each)));
        }
    }
    
    /**
     * Delete table row data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    public void delete(final String databaseName, final String schemaName, final String tableName, final Collection<YamlRowStatistics> rows) {
        rows.forEach(each -> repository.delete(NodePathGenerator.toPath(new StatisticsDataNodePath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey()))));
    }
    
    /**
     * Load table statistics.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table
     * @return table statistics
     */
    public TableStatistics load(final String databaseName, final String schemaName, final ShardingSphereTable table) {
        TableStatistics result = new TableStatistics(table.getName());
        YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(new ArrayList<>(table.getAllColumns()));
        for (String each : repository.getChildrenKeys(NodePathGenerator.toPath(new StatisticsTableNodePath(databaseName, schemaName, table.getName().toLowerCase())))) {
            String yamlRow = repository.query(NodePathGenerator.toPath(new StatisticsDataNodePath(databaseName, schemaName, table.getName().toLowerCase(), each)));
            if (!Strings.isNullOrEmpty(yamlRow)) {
                result.getRows().add(swapper.swapToObject(YamlEngine.unmarshal(yamlRow, YamlRowStatistics.class)));
            }
        }
        return result;
    }
}
