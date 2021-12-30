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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Schema refresher for drop view statement.
 */
public final class DropViewStatementSchemaRefresher implements MetaDataRefresher<DropViewStatement> {
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Map<String, OptimizerPlannerContext> optimizerPlanners, 
                        final Collection<String> logicDataSourceNames, final DropViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        SchemaAlteredEvent event = new SchemaAlteredEvent(schemaMetaData.getName());
        sqlStatement.getViews().forEach(each -> {
            schemaMetaData.getSchema().remove(each.getTableName().getIdentifier().getValue());
            event.getDroppedTables().add(each.getTableName().getIdentifier().getValue());
        });
        Collection<MutableDataNodeRule> rules = schemaMetaData.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (SimpleTableSegment each : sqlStatement.getViews()) {
            rules.forEach(rule -> rule.remove(each.getTableName().getIdentifier().getValue()));
        }
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    @Override
    public String getType() {
        return DropViewStatement.class.getCanonicalName();
    }
}
