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
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Schema refresher for create view statement.
 */
public final class CreateViewStatementSchemaRefresher implements MetaDataRefresher<CreateViewStatement> {
    
    private static final String TYPE = CreateViewStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final CreateViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String viewName = sqlStatement.getView().getTableName().getIdentifier().getValue();
        TableMetaData tableMetaData = new TableMetaData();
        schemaMetaData.getDefaultSchema().put(viewName, tableMetaData);
        if (!containsInDataNodeContainedRule(viewName, schemaMetaData)) {
            schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class).forEach(each -> each.put(viewName, logicDataSourceNames.iterator().next()));
        }
        SchemaAlteredEvent event = new SchemaAlteredEvent(schemaMetaData.getName());
        event.getAlteredTables().add(tableMetaData);
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private boolean containsInDataNodeContainedRule(final String tableName, final ShardingSphereMetaData schemaMetaData) {
        return schemaMetaData.getRuleMetaData().findRules(DataNodeContainedRule.class).stream().anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
