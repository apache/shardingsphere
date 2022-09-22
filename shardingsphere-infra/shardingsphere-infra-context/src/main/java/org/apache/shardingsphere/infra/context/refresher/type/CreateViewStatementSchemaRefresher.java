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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for create view statement.
 */
public final class CreateViewStatementSchemaRefresher implements MetaDataRefresher<CreateViewStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final CreateViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = sqlStatement.getView().getTableName().getIdentifier().getValue();
        String viewDefinition = sqlStatement.getViewSQL().substring(sqlStatement.getViewSQL().substring(0, sqlStatement.getViewSQL().indexOf(" as ")).length() + 4).trim();
        if (!containsInImmutableDataNodeContainedRule(viewName, database)) {
            database.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(logicDataSourceNames.iterator().next(), schemaName, viewName));
        }
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(database.getProtocolType(),
                database.getResource().getDatabaseType(), database.getResource().getDataSources(), database.getRuleMetaData().getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemaMap = GenericSchemaBuilder.build(Collections.singletonList(viewName), materials);
        Optional<ShardingSphereTable> actualTableMetaData = Optional.ofNullable(schemaMap.get(schemaName)).map(optional -> optional.getTable(viewName));
        if (actualTableMetaData.isPresent()) {
            ShardingSphereView view = new ShardingSphereView(viewName, viewDefinition);
            database.getSchema(schemaName).putTable(viewName, actualTableMetaData.get());
            database.getSchema(schemaName).putView(viewName, view);
            SchemaAlteredEvent event = new SchemaAlteredEvent(database.getName(), schemaName);
            event.getAlteredTables().add(actualTableMetaData.get());
            event.getAlteredViews().add(view);
            return Optional.of(event);
        }
        return Optional.empty();
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String tableName, final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return CreateViewStatement.class.getName();
    }
}
