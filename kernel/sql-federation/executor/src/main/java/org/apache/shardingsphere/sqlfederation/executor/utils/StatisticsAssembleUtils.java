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
import org.apache.shardingsphere.infra.metadata.statistics.RowStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sqlfederation.executor.constant.EnumerableConstants;

import java.util.Arrays;
import java.util.Collection;

/**
 * Statistics assemble utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsAssembleUtils {
    
    /**
     * Assemble table statistics.
     *
     * @param table table
     * @param metaData meta data
     * @return table statistics
     */
    public static TableStatistics assembleTableStatistics(final ShardingSphereTable table, final ShardingSphereMetaData metaData) {
        // TODO move this logic to ShardingSphere statistics
        TableStatistics result = new TableStatistics(table.getName());
        if (EnumerableConstants.PG_DATABASE.equalsIgnoreCase(table.getName())) {
            assembleOpenGaussDatabaseData(result, metaData.getAllDatabases());
        } else if (EnumerableConstants.PG_TABLES.equalsIgnoreCase(table.getName())) {
            for (ShardingSphereDatabase each : metaData.getAllDatabases()) {
                assembleOpenGaussTableData(result, each.getAllSchemas());
            }
        } else if (EnumerableConstants.PG_ROLES.equalsIgnoreCase(table.getName())) {
            assembleOpenGaussRoleData(result, metaData);
        }
        return result;
    }
    
    private static void assembleOpenGaussDatabaseData(final TableStatistics tableStatistics, final Collection<ShardingSphereDatabase> databases) {
        for (ShardingSphereDatabase each : databases) {
            Object[] rows = new Object[15];
            rows[0] = each.getName();
            rows[11] = EnumerableConstants.DAT_COMPATIBILITY;
            tableStatistics.getRows().add(new RowStatistics(Arrays.asList(rows)));
        }
    }
    
    private static void assembleOpenGaussTableData(final TableStatistics tableStatistics, final Collection<ShardingSphereSchema> schemas) {
        for (ShardingSphereSchema schema : schemas) {
            for (ShardingSphereTable each : schema.getAllTables()) {
                Object[] rows = new Object[10];
                rows[0] = schema.getName();
                rows[1] = each.getName();
                tableStatistics.getRows().add(new RowStatistics(Arrays.asList(rows)));
            }
        }
    }
    
    private static void assembleOpenGaussRoleData(final TableStatistics tableStatistics, final ShardingSphereMetaData metaData) {
        for (Grantee each : metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class).getGrantees()) {
            Object[] rows = new Object[27];
            rows[0] = each.getUsername();
            tableStatistics.getRows().add(new RowStatistics(Arrays.asList(rows)));
        }
    }
}
