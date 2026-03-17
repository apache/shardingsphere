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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Create view push down meta data refresher.
 */
public final class CreateViewPushDownMetaDataRefresher implements PushDownMetaDataRefresher<CreateViewStatement> {
    
    private final ViewLoader viewLoader;
    
    public CreateViewPushDownMetaDataRefresher() {
        viewLoader = CreateViewPushDownMetaDataRefresher::loadView;
    }
    
    CreateViewPushDownMetaDataRefresher(final ViewLoader viewLoader) {
        this.viewLoader = viewLoader;
    }
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final CreateViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = TableRefreshUtils.getTableName(sqlStatement.getView().getTableName().getIdentifier(), databaseType);
        ShardingSphereTable actualViewMetaData = viewLoader.load(database, logicDataSourceName, schemaName, viewName, props);
        metaDataManagerPersistService.alterTables(database, schemaName, Collections.singleton(actualViewMetaData));
    }
    
    private static ShardingSphereTable loadView(final ShardingSphereDatabase database, final String logicDataSourceName,
                                                final String schemaName, final String viewName, final ConfigurationProperties props) throws SQLException {
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        boolean singleTable = TableRefreshUtils.isSingleTable(viewName, database);
        if (singleTable) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, viewName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(Collections.singletonList(viewName), database.getProtocolType(), material);
        Optional<ShardingSphereTable> actualTableMetaData = Optional.ofNullable(schemas.get(schemaName)).map(optional -> optional.getTable(viewName));
        Preconditions.checkState(actualTableMetaData.isPresent(), "Load actual view metadata '%s' failed.", viewName);
        ShardingSphereTable result = actualTableMetaData.get();
        if (singleTable && !result.getName().equals(viewName)) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> {
                each.remove(schemaName, viewName);
                each.put(logicDataSourceName, schemaName, result.getName());
            });
        }
        return result;
    }
    
    @Override
    public Class<CreateViewStatement> getType() {
        return CreateViewStatement.class;
    }
    
    @FunctionalInterface
    interface ViewLoader {
        
        ShardingSphereTable load(ShardingSphereDatabase database, String logicDataSourceName, String schemaName, String viewName, ConfigurationProperties props) throws SQLException;
    }
}
