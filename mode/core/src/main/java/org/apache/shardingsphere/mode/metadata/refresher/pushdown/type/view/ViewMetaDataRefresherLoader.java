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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * View meta data refresher loader for push down refresh.
 */
public final class ViewMetaDataRefresherLoader {
    
    /**
     * Load created view meta data.
     *
     * @param database database
     * @param logicDataSourceName logic data source name
     * @param schemaName schema name
     * @param viewIdentifierValue view identifier value
     * @param props configuration properties
     * @return loaded view table meta data
     * @throws SQLException SQL exception
     */
    public ShardingSphereTable loadCreatedView(final ShardingSphereDatabase database, final String logicDataSourceName,
                                               final String schemaName, final IdentifierValue viewIdentifierValue, final ConfigurationProperties props) throws SQLException {
        String candidateViewName = TableRefreshUtils.getViewLoadCandidateName(database, viewIdentifierValue, props);
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        boolean singleTable = TableRefreshUtils.isSingleTable(candidateViewName, database);
        if (singleTable) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, candidateViewName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(Collections.singletonList(candidateViewName), database.getProtocolType(), material);
        Optional<ShardingSphereTable> actualTableMetaData = Optional.ofNullable(schemas.get(schemaName)).map(optional -> optional.getTable(candidateViewName));
        Preconditions.checkState(actualTableMetaData.isPresent(), "Load actual view metadata '%s' failed.", candidateViewName);
        ShardingSphereTable result = actualTableMetaData.get();
        if (singleTable && !result.getName().equals(candidateViewName)) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> {
                each.remove(schemaName, candidateViewName);
                each.put(logicDataSourceName, schemaName, result.getName());
            });
        }
        return result;
    }
    
    /**
     * Load altered view meta data.
     *
     * @param database database
     * @param logicDataSourceName logic data source name
     * @param schemaName schema name
     * @param viewIdentifierValue view identifier value
     * @param viewDefinition view definition
     * @param props configuration properties
     * @return loaded view schema meta data
     * @throws SQLException SQL exception
     */
    public ShardingSphereSchema loadAlteredView(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName,
                                                final IdentifierValue viewIdentifierValue, final String viewDefinition, final ConfigurationProperties props) throws SQLException {
        String candidateViewName = TableRefreshUtils.getViewLoadCandidateName(database, viewIdentifierValue, props);
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        boolean singleTable = TableRefreshUtils.isSingleTable(candidateViewName, database);
        if (singleTable) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, candidateViewName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(Collections.singletonList(candidateViewName), database.getProtocolType(), material);
        Optional<ShardingSphereTable> actualViewMetaData = Optional.ofNullable(schemas.get(schemaName)).map(optional -> optional.getTable(candidateViewName));
        ShardingSphereSchema result = new ShardingSphereSchema(schemaName, database.getProtocolType());
        actualViewMetaData.ifPresent(optional -> {
            result.putTable(optional);
            if (singleTable && !optional.getName().equals(candidateViewName)) {
                ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> {
                    each.remove(schemaName, candidateViewName);
                    each.put(logicDataSourceName, schemaName, optional.getName());
                });
            }
            result.putView(new ShardingSphereView(optional.getName(), viewDefinition));
        });
        if (!actualViewMetaData.isPresent()) {
            result.putView(new ShardingSphereView(candidateViewName, viewDefinition));
        }
        return result;
    }
}
