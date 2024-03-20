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

package org.apache.shardingsphere.sqlfederation.executor.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.sqlfederation.executor.constant.EnumerableConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Statistics assemble utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsAssembleUtils {
    
    /**
     * Assemble table data.
     * 
     * @param table table
     * @param metaData meta data
     * @return ShardingSphere table data
     */
    public static ShardingSphereTableData assembleTableData(final ShardingSphereTable table, final ShardingSphereMetaData metaData) {
        // TODO move this logic to ShardingSphere statistics
        ShardingSphereTableData result = new ShardingSphereTableData(table.getName());
        if (EnumerableConstants.PG_DATABASE.equalsIgnoreCase(table.getName())) {
            assembleOpenGaussDatabaseData(result, metaData.getDatabases().values());
        } else if (EnumerableConstants.PG_TABLES.equalsIgnoreCase(table.getName())) {
            for (ShardingSphereDatabase each : metaData.getDatabases().values()) {
                assembleOpenGaussTableData(result, each.getSchemas());
            }
        } else if (EnumerableConstants.PG_ROLES.equalsIgnoreCase(table.getName())) {
            assembleOpenGaussRoleData(result, metaData);
        }
        return result;
    }
    
    private static void assembleOpenGaussDatabaseData(final ShardingSphereTableData tableData, final Collection<ShardingSphereDatabase> databases) {
        for (ShardingSphereDatabase each : databases) {
            Object[] rows = new Object[15];
            rows[0] = each.getName();
            rows[11] = EnumerableConstants.DAT_COMPATIBILITY;
            tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
        }
    }
    
    private static void assembleOpenGaussTableData(final ShardingSphereTableData tableData, final Map<String, ShardingSphereSchema> schemas) {
        for (Map.Entry<String, ShardingSphereSchema> entry : schemas.entrySet()) {
            for (String each : entry.getValue().getAllTableNames()) {
                Object[] rows = new Object[10];
                rows[0] = entry.getKey();
                rows[1] = each;
                tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
            }
        }
    }
    
    private static void assembleOpenGaussRoleData(final ShardingSphereTableData tableData, final ShardingSphereMetaData metaData) {
        for (ShardingSphereUser each : metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class).getConfiguration().getUsers()) {
            Object[] rows = new Object[27];
            rows[0] = each.getGrantee().getUsername();
            tableData.getRows().add(new ShardingSphereRowData(Arrays.asList(rows)));
        }
    }
}
