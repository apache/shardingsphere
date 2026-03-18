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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Table meta data refresher loader for push down refresh.
 */
public final class TableMetaDataRefresherLoader {
    
    /**
     * Load created table meta data.
     *
     * @param database database
     * @param logicDataSourceName logic data source name
     * @param schemaName schema name
     * @param tableIdentifierValue table identifier value
     * @param props configuration properties
     * @return loaded table meta data
     * @throws SQLException SQL exception
     */
    public ShardingSphereTable loadCreatedTable(final ShardingSphereDatabase database, final String logicDataSourceName,
                                                final String schemaName, final IdentifierValue tableIdentifierValue, final ConfigurationProperties props) throws SQLException {
        return loadTable(database, logicDataSourceName, schemaName, tableIdentifierValue, props, false);
    }
    
    /**
     * Load altered table meta data.
     *
     * @param database database
     * @param logicDataSourceName logic data source name
     * @param schemaName schema name
     * @param tableIdentifierValue table identifier value
     * @param props configuration properties
     * @return loaded table meta data
     * @throws SQLException SQL exception
     */
    public ShardingSphereTable loadAlteredTable(final ShardingSphereDatabase database, final String logicDataSourceName,
                                                final String schemaName, final IdentifierValue tableIdentifierValue, final ConfigurationProperties props) throws SQLException {
        return loadTable(database, logicDataSourceName, schemaName, tableIdentifierValue, props, true);
    }
    
    private ShardingSphereTable loadTable(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName,
                                          final IdentifierValue tableIdentifierValue, final ConfigurationProperties props, final boolean fallbackWhenMissing) throws SQLException {
        String candidateTableName = TableRefreshUtils.getTableLoadCandidateName(database, tableIdentifierValue, props);
        RuleMetaData ruleMetaData = new RuleMetaData(new LinkedList<>(database.getRuleMetaData().getRules()));
        boolean singleTable = TableRefreshUtils.isSingleTable(candidateTableName, database);
        if (singleTable) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, candidateTableName));
        }
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(), ruleMetaData.getRules(), props, schemaName);
        Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(Collections.singletonList(candidateTableName), database.getProtocolType(), material);
        ShardingSphereTable result = Optional.ofNullable(schemas.get(schemaName)).map(optional -> optional.getTable(candidateTableName))
                .orElseGet(() -> fallbackWhenMissing ? new ShardingSphereTable(candidateTableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()) : null);
        if (singleTable && null != result && !result.getName().equals(candidateTableName)) {
            ruleMetaData.getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> {
                each.remove(schemaName, candidateTableName);
                each.put(logicDataSourceName, schemaName, result.getName());
            });
        }
        return result;
    }
}
