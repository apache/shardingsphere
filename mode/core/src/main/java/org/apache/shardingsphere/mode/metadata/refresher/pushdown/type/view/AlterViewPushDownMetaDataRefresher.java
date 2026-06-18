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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Alter view push down meta data refresher.
 */
public final class AlterViewPushDownMetaDataRefresher implements PushDownMetaDataRefresher<AlterViewStatement> {
    
    private final ViewMetaDataRefresherLoader metaDataLoader = new ViewMetaDataRefresherLoader();
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final AlterViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String actualViewName = TableRefreshUtils.getActualViewName(database, schemaName, sqlStatement.getView().getTableName().getIdentifier(), props);
        Collection<ShardingSphereTable> alteredTables = new LinkedList<>();
        Collection<ShardingSphereView> alteredViews = new LinkedList<>();
        Collection<String> droppedTables = new LinkedList<>();
        Collection<String> droppedViews = new LinkedList<>();
        Optional<SimpleTableSegment> renameView = sqlStatement.getRenameView();
        if (renameView.isPresent()) {
            String originalView = database.getSchema(schemaName).getView(actualViewName).getViewDefinition();
            ShardingSphereSchema schema = metaDataLoader.loadAlteredView(database, logicDataSourceName, schemaName, renameView.get().getTableName().getIdentifier(), originalView, props);
            alteredTables.add(schema.getAllTables().iterator().next());
            alteredViews.add(schema.getAllViews().iterator().next());
            droppedTables.add(actualViewName);
            droppedViews.add(actualViewName);
        }
        Optional<String> viewDefinition = sqlStatement.getViewDefinition();
        if (viewDefinition.isPresent()) {
            ShardingSphereSchema schema = metaDataLoader.loadAlteredView(database, logicDataSourceName, schemaName, sqlStatement.getView().getTableName().getIdentifier(), viewDefinition.get(), props);
            alteredTables.add(schema.getAllTables().iterator().next());
            alteredViews.add(schema.getAllViews().iterator().next());
        }
        metaDataManagerPersistService.alterTables(database, schemaName, alteredTables);
        metaDataManagerPersistService.alterViews(database, schemaName, alteredViews);
        metaDataManagerPersistService.dropTables(database, schemaName, droppedTables);
        metaDataManagerPersistService.dropViews(database, schemaName, droppedViews);
    }
    
    @Override
    public Class<AlterViewStatement> getType() {
        return AlterViewStatement.class;
    }
}
