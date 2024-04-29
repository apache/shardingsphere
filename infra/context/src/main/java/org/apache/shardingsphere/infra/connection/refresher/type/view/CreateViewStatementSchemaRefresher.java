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

package org.apache.shardingsphere.infra.connection.refresher.type.view;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.connection.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for create view statement.
 */
public final class CreateViewStatementSchemaRefresher implements MetaDataRefresher<CreateViewStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DatabaseType databaseType, final CreateViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = TableRefreshUtils.getTableName(databaseType, sqlStatement.getView().getTableName().getIdentifier());
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        if (TableRefreshUtils.isSingleTable(viewName, database)) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceNames.iterator().next(), schemaName, viewName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(
                database.getProtocolType(), database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemaMap = GenericSchemaBuilder.build(Collections.singletonList(viewName), material);
        Optional<ShardingSphereTable> actualTableMetaData = Optional.ofNullable(schemaMap.get(schemaName)).map(optional -> optional.getTable(viewName));
        if (actualTableMetaData.isPresent()) {
            AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), schemaName, logicDataSourceNames);
            alterSchemaMetaDataPOJO.getAlteredTables().add(actualTableMetaData.get());
            alterSchemaMetaDataPOJO.getAlteredViews().add(new ShardingSphereView(viewName, sqlStatement.getViewDefinition()));
            modeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        }
    }
    
    @Override
    public Class<CreateViewStatement> getType() {
        return CreateViewStatement.class;
    }
}
