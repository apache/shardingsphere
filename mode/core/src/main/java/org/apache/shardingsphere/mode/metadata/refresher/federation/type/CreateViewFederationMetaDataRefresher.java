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

package org.apache.shardingsphere.mode.metadata.refresher.federation.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.metadata.refresher.federation.FederationMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;

import java.util.Collections;

/**
 * Create view federation meta data refresher.
 */
public final class CreateViewFederationMetaDataRefresher implements FederationMetaDataRefresher<CreateViewStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService,
                        final DatabaseType databaseType, final ShardingSphereDatabase database, final String schemaName, final CreateViewStatement sqlStatement) {
        String viewName = TableRefreshUtils.getTableName(sqlStatement.getView().getTableName().getIdentifier(), databaseType);
        metaDataManagerPersistService.alterViews(database, schemaName, Collections.singleton(new ShardingSphereView(viewName, sqlStatement.getViewDefinition())));
    }
    
    @Override
    public Class<CreateViewStatement> getType() {
        return CreateViewStatement.class;
    }
}
