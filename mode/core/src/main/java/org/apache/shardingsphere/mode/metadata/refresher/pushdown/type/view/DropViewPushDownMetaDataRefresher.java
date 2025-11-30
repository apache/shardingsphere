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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.view;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Drop view push down meta data refresher.
 */
public final class DropViewPushDownMetaDataRefresher implements PushDownMetaDataRefresher<DropViewStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final DropViewStatement sqlStatement, final ConfigurationProperties props) {
        Collection<String> droppedTables = new LinkedList<>();
        Collection<String> droppedViews = new LinkedList<>();
        for (SimpleTableSegment each : sqlStatement.getViews()) {
            String viewName = each.getTableName().getIdentifier().getValue();
            droppedTables.add(viewName);
            droppedViews.add(viewName);
        }
        metaDataManagerPersistService.dropTables(database, schemaName, droppedTables);
        metaDataManagerPersistService.dropViews(database, schemaName, droppedViews);
    }
    
    @Override
    public Class<DropViewStatement> getType() {
        return DropViewStatement.class;
    }
}
