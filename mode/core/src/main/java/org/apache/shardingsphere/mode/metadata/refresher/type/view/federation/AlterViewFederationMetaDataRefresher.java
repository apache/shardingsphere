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

package org.apache.shardingsphere.mode.metadata.refresher.type.view.federation;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.mode.metadata.refresher.FederationMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.divided.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterViewStatement;

import java.util.Optional;

/**
 * Meta data refresher for alter view statement.
 */
public final class AlterViewFederationMetaDataRefresher implements FederationMetaDataRefresher<AlterViewStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String schemaName,
                        final DatabaseType databaseType, final AlterViewStatement sqlStatement) {
        String viewName = TableRefreshUtils.getTableName(databaseType, sqlStatement.getView().getTableName().getIdentifier());
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), schemaName);
        Optional<SimpleTableSegment> renameView = sqlStatement.getRenameView();
        if (renameView.isPresent()) {
            String renameViewName = renameView.get().getTableName().getIdentifier().getValue();
            String originalView = database.getSchema(schemaName).getView(viewName).getViewDefinition();
            alterSchemaMetaDataPOJO.getAlteredViews().add(new ShardingSphereView(renameViewName, originalView));
            alterSchemaMetaDataPOJO.getDroppedViews().add(viewName);
        }
        sqlStatement.getViewDefinition().ifPresent(optional -> alterSchemaMetaDataPOJO.getAlteredViews().add(new ShardingSphereView(viewName, optional)));
        metaDataManagerPersistService.alterSchemaMetaData(alterSchemaMetaDataPOJO);
    }
    
    @Override
    public Class<AlterViewStatement> getType() {
        return AlterViewStatement.class;
    }
}
