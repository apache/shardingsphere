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

package org.apache.shardingsphere.sharding.checker.sql.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Arrays;
import java.util.Collection;

/**
 * Sharding supported check utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSupportedCheckUtils {
    
    /**
     * Judge whether schema contains index or not.
     *
     * @param schema ShardingSphere schema
     * @param index index
     * @return whether schema contains index or not
     */
    public static boolean isSchemaContainsIndex(final ShardingSphereSchema schema, final IndexSegment index) {
        return schema.getAllTables().stream().anyMatch(each -> each.containsIndex(index.getIndexName().getIdentifier().getValue()));
    }
    
    /**
     * Judge whether sharding tables not binding with view.
     *
     * @param tableSegments table segments
     * @param shardingRule sharding rule
     * @param viewName view name
     * @return sharding tables not binding with view or not
     */
    public static boolean isShardingTablesNotBindingWithView(final Collection<SimpleTableSegment> tableSegments, final ShardingRule shardingRule, final String viewName) {
        for (SimpleTableSegment each : tableSegments) {
            String logicTable = each.getTableName().getIdentifier().getValue();
            if (shardingRule.isShardingTable(logicTable) && !shardingRule.isAllConfigBindingTables(Arrays.asList(viewName, logicTable))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge whether route unit and data node are different size.
     *
     * @param shardingRule sharding rule
     * @param routeContext route context
     * @param tableName table name
     * @return route unit and data node are different size or not
     */
    public static boolean isRouteUnitDataNodeDifferentSize(final ShardingRule shardingRule, final RouteContext routeContext, final String tableName) {
        return shardingRule.isShardingTable(tableName) && shardingRule.getShardingTable(tableName).getActualDataNodes().size() != routeContext.getRouteUnits().size();
    }
}
