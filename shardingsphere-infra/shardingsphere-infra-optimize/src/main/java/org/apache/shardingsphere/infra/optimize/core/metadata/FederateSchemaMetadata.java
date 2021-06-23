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

package org.apache.shardingsphere.infra.optimize.core.metadata;

import lombok.Getter;
import lombok.Synchronized;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;


/**
 * Federate schema metadata.
 *
 */
@Getter
public final class FederateSchemaMetadata {
    
    private final String name;
    
    private final Map<String, FederateTableMetadata> tables = new LinkedHashMap<>();
    
    /**
     * Please fix me.
     * @deprecated Remove this constructor.
     */
    @Deprecated
    public FederateSchemaMetadata(final String name, final ShardingSphereMetaData metaData) throws SQLException {
        this.name = name;
        initTables(metaData);
    }
    
    public FederateSchemaMetadata(final String name, final Map<String, TableMetaData> metaData) {
        this.name = name;
        for (Entry<String, TableMetaData> entry : metaData.entrySet()) {
            tables.put(entry.getKey(), new FederateTableMetadata(entry.getKey(), entry.getValue()));
        }
    }
    
    private void initTables(final ShardingSphereMetaData metaData) throws SQLException {
        Collection<DataNodeContainedRule> dataNodeRules = getDataNodeContainedRules(metaData);
        Map<String, Collection<DataNode>> tableDataNodes = getTableDataNodes(dataNodeRules);
        Map<String, Collection<String>> dataSourceRules = getDataSourceRules(metaData);
        for (Entry<String, Collection<DataNode>> entry : tableDataNodes.entrySet()) {
            tables.put(entry.getKey(),
                    new FederateTableMetadata(entry.getKey(), metaData.getResource().getDataSources(), dataSourceRules, entry.getValue(), metaData.getResource().getDatabaseType()));
        }
    }
    
    private Collection<DataNodeContainedRule> getDataNodeContainedRules(final ShardingSphereMetaData metaData) {
        Collection<DataNodeContainedRule> result = new LinkedList<>();
        for (ShardingSphereRule each : metaData.getRuleMetaData().getRules()) {
            if (each instanceof DataNodeContainedRule) {
                result.add((DataNodeContainedRule) each);
            }
        }
        return result;
    }
    
    private Map<String, Collection<String>> getDataSourceRules(final ShardingSphereMetaData metaData) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (ShardingSphereRule each : metaData.getRuleMetaData().getRules()) {
            if (each instanceof DataSourceContainedRule) {
                result.putAll(((DataSourceContainedRule) each).getDataSourceMapper());
            }
        }
        return result;
    }
    
    private Map<String, Collection<DataNode>> getTableDataNodes(final Collection<DataNodeContainedRule> dataNodeRules) {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        for (DataNodeContainedRule each : dataNodeRules) {
            result.putAll(each.getAllDataNodes());
        }
        return result;
    }
    
    /**
     * Renew.
     * @param tableName table name
     * @param metaData meta data
     */
    @Synchronized
    public void renew(final String tableName, final TableMetaData metaData) {
        tables.put(tableName, new FederateTableMetadata(tableName, metaData));
    }
    
    /**
     * Remove.
     * @param tableName table name
     */
    @Synchronized
    public void remove(final String tableName) {
        tables.remove(tableName);
    }
}
