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

package org.apache.shardingsphere.infra.metadata.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.auth.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.auth.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.privilege.refresher.PrivilegeRefresher;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.spi.SchemaChangedNotifier;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Metadata refresh engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetadataRefreshEngine {
    
    /**
     * Refresh.
     *
     * @param sqlStatement sql statement
     * @param metaData metadata
     * @param materials materials
     * @throws SQLException sql exception
     */
    @SuppressWarnings("rawtypes")
    public static void refresh(final SQLStatement sqlStatement, final ShardingSphereMetaData metaData, final SchemaBuilderMaterials materials) throws SQLException {
        Optional<MetadataRefresher> metadataRefresher = SchemaRefresherFactory.newInstance(sqlStatement);
        if (metadataRefresher.isPresent()) {
            if (metadataRefresher.get() instanceof SchemaRefresher) {
                refreshSchema(sqlStatement, metaData, materials, (SchemaRefresher) metadataRefresher.get());
            }
            if (metadataRefresher.get() instanceof PrivilegeRefresher) {
                refreshPrivilege(materials.getAuth(), (PrivilegeRefresher) metadataRefresher.get());
            }
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void refreshSchema(final SQLStatement sqlStatement,
                                      final ShardingSphereMetaData metaData, final SchemaBuilderMaterials materials, final SchemaRefresher refresher) throws SQLException {
        ShardingSphereSchema schema = metaData.getSchema();
        refresher.refresh(metaData.getSchema(), materials.getRouteDataSourceNames(), sqlStatement, materials);
        OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(schema), SchemaChangedNotifier.class).values().forEach(each -> each.notify(metaData.getName(), schema));
    }
    
    private static void refreshPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final PrivilegeRefresher refresher) {
        privileges.forEach(refresher::refresh);
        // TODO :notify
    }
}
