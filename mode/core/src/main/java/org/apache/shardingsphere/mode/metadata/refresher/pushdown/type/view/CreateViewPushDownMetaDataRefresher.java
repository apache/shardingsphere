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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;

import java.sql.SQLException;
import java.util.Collections;

/**
 * Create view push down meta data refresher.
 */
public final class CreateViewPushDownMetaDataRefresher implements PushDownMetaDataRefresher<CreateViewStatement> {
    
    private final ViewMetaDataRefresherLoader metaDataLoader = new ViewMetaDataRefresherLoader();
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final CreateViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        ShardingSphereTable actualViewMetaData = metaDataLoader.loadCreatedView(database, logicDataSourceName, schemaName, sqlStatement.getView().getTableName().getIdentifier(), props);
        metaDataManagerPersistService.alterTables(database, schemaName, Collections.singleton(actualViewMetaData));
        metaDataManagerPersistService.alterViews(database, schemaName, Collections.singleton(new ShardingSphereView(actualViewMetaData.getName(), sqlStatement.getViewDefinition())));
        if (TableRefreshUtils.isSingleTable(actualViewMetaData.getName(), database) && TableRefreshUtils.isNeedRefresh(database.getRuleMetaData(), schemaName, actualViewMetaData.getName())) {
            metaDataManagerPersistService.alterSingleRuleConfiguration(database, database.getRuleMetaData());
        }
    }
    
    @Override
    public Class<CreateViewStatement> getType() {
        return CreateViewStatement.class;
    }
}
