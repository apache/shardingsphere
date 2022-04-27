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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for drop index statement.
 */
public final class DropIndexStatementSchemaRefresher implements MetaDataRefresher<DropIndexStatement> {
    
    private static final String TYPE = DropIndexStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData metaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final String schemaName, final DropIndexStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        Multimap<String, IndexSegment> schemaIndexSegmentMap = getSchemaIndexSegmentMap(sqlStatement, schemaName);
        for (String each : schemaIndexSegmentMap.keySet()) {
            ShardingSphereSchema schema = metaData.getSchemaByName(each);
            Collection<TableMetaData> alteredTables = new LinkedList<>();
            for (String tableName : getLogicTableNames(schema, sqlStatement)) {
                if (!schema.containsTable(tableName)) {
                    continue;
                }
                TableMetaData tableMetaData = schema.get(tableName);
                schemaIndexSegmentMap.get(each).forEach(indexSegment -> tableMetaData.getIndexes().remove(indexSegment.getIndexName().getIdentifier().getValue()));
                alteredTables.add(tableMetaData);
            }
            post(metaData.getDatabaseName(), each, alteredTables);
        }
    }
    
    private Collection<String> getLogicTableNames(final ShardingSphereSchema schema, final DropIndexStatement sqlStatement) {
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(sqlStatement);
        if (simpleTableSegment.isPresent()) {
            return Collections.singletonList(simpleTableSegment.get().getTableName().getIdentifier().getValue());
        }
        return IndexMetaDataUtil.getTableNamesFromMetaData(schema, sqlStatement.getIndexes());
    }
    
    private Multimap<String, IndexSegment> getSchemaIndexSegmentMap(final DropIndexStatement dropIndexStatement, final String defaultSchemaName) {
        Multimap<String, IndexSegment> result = LinkedHashMultimap.create();
        for (IndexSegment each : dropIndexStatement.getIndexes()) {
            String schemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElseGet(() -> defaultSchemaName);
            result.put(schemaName, each);
        }
        return result;
    }
    
    private void post(final String databaseName, final String schemaName, final Collection<TableMetaData> alteredTables) {
        SchemaAlteredEvent event = new SchemaAlteredEvent(databaseName, schemaName);
        event.getAlteredTables().addAll(alteredTables);
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
