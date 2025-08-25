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
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Alter view federation meta data refresher.
 */
public final class AlterViewFederationMetaDataRefresher implements FederationMetaDataRefresher<AlterViewStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService,
                        final DatabaseType databaseType, final ShardingSphereDatabase database, final String schemaName, final AlterViewStatement sqlStatement) {
        String viewName = TableRefreshUtils.getTableName(sqlStatement.getView().getTableName().getIdentifier(), databaseType);
        Optional<SimpleTableSegment> renameView = sqlStatement.getRenameView();
        Collection<ShardingSphereView> alteredViews = new LinkedList<>();
        Collection<String> droppedViews = new LinkedList<>();
        if (renameView.isPresent()) {
            String renameViewName = renameView.get().getTableName().getIdentifier().getValue();
            String originalView = database.getSchema(schemaName).getView(viewName).getViewDefinition();
            alteredViews.add(new ShardingSphereView(renameViewName, originalView));
            droppedViews.add(viewName);
        }
        sqlStatement.getViewDefinition().ifPresent(optional -> alteredViews.add(new ShardingSphereView(viewName, optional)));
        metaDataManagerPersistService.alterViews(database, schemaName, alteredViews);
        metaDataManagerPersistService.dropViews(database, schemaName, droppedViews);
    }
    
    @Override
    public Class<AlterViewStatement> getType() {
        return AlterViewStatement.class;
    }
}
