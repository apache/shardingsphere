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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

/**
 * Table refresh utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableRefreshUtils {
    
    /**
     * Get table name.
     *
     * @param tableIdentifierValue table identifier value
     * @param databaseType database type
     * @return table name
     */
    public static String getTableName(final IdentifierValue tableIdentifierValue, final DatabaseType databaseType) {
        return QuoteCharacter.NONE == tableIdentifierValue.getQuoteCharacter()
                ? new DatabaseTypeRegistry(databaseType).formatIdentifierPattern(tableIdentifierValue.getValue())
                : tableIdentifierValue.getValue();
    }
    
    /**
     * Get actual table name.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableIdentifierValue table identifier value
     * @param props configuration properties
     * @return actual table name
     */
    public static String getActualTableName(final ShardingSphereDatabase database, final String schemaName,
                                            final IdentifierValue tableIdentifierValue, final ConfigurationProperties props) {
        return getActualObjectName(database, schemaName, tableIdentifierValue, props, IdentifierScope.TABLE,
                schema -> schema.getAllTables().stream().map(ShardingSphereTable::getName));
    }
    
    /**
     * Get actual table names.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableIdentifierValues table identifier values
     * @param props configuration properties
     * @return actual table names
     */
    public static Collection<String> getActualTableNames(final ShardingSphereDatabase database, final String schemaName,
                                                         final Collection<IdentifierValue> tableIdentifierValues, final ConfigurationProperties props) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : tableIdentifierValues) {
            String actualTableName = getActualTableName(database, schemaName, each, props);
            if (null != actualTableName) {
                result.add(actualTableName);
            }
        }
        return result;
    }
    
    /**
     * Get actual view names.
     *
     * @param database database
     * @param schemaName schema name
     * @param viewIdentifierValues view identifier values
     * @param props configuration properties
     * @return actual view names
     */
    public static Collection<String> getActualViewNames(final ShardingSphereDatabase database, final String schemaName,
                                                        final Collection<IdentifierValue> viewIdentifierValues, final ConfigurationProperties props) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : viewIdentifierValues) {
            String actualViewName = getActualObjectName(database, schemaName, each, props, IdentifierScope.VIEW,
                    schema -> schema.getAllViews().stream().map(ShardingSphereView::getName));
            if (null != actualViewName) {
                result.add(actualViewName);
            }
        }
        return result;
    }
    
    /**
     * Judge whether to need refresh.
     *
     * @param tableName table name
     * @param database database
     * @return need to refresh or not
     */
    public static boolean isSingleTable(final String tableName, final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class).stream().noneMatch(each -> each.getDistributedTableNames().contains(tableName));
    }
    
    /**
     * Judge whether to need refresh.
     *
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableNames table names
     * @return need to refresh or not
     */
    public static boolean isNeedRefresh(final RuleMetaData ruleMetaData, final String schemaName, final Collection<String> tableNames) {
        return tableNames.stream().anyMatch(each -> isNeedRefresh(ruleMetaData, schemaName, each));
    }
    
    /**
     * Judge whether the rule need to be refreshed.
     *
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableName table name
     * @return whether the rule need to be refreshed
     */
    public static boolean isNeedRefresh(final RuleMetaData ruleMetaData, final String schemaName, final String tableName) {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        for (ShardingSphereRule each : ruleMetaData.getRules()) {
            each.getAttributes().findAttribute(MutableDataNodeRuleAttribute.class).ifPresent(optional -> rules.add(each));
        }
        if (rules.isEmpty()) {
            return false;
        }
        ShardingSphereRule rule = rules.iterator().next();
        RuleConfiguration ruleConfig = rule.getConfiguration();
        if (!(ruleConfig instanceof SingleRuleConfiguration)) {
            return false;
        }
        Collection<String> tablesConfig = ((SingleRuleConfiguration) ruleConfig).getTables();
        if (tablesConfig.contains(SingleTableConstants.ALL_TABLES) || tablesConfig.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return false;
        }
        Optional<DataNode> dataNode = rule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(schemaName, tableName);
        if (!dataNode.isPresent()) {
            return false;
        }
        DataNode actualNode = dataNode.get();
        return !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), SingleTableConstants.ASTERISK))
                && !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), SingleTableConstants.ASTERISK, SingleTableConstants.ASTERISK))
                && !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), actualNode.getSchemaName(), SingleTableConstants.ASTERISK));
    }
    
    private static String joinDataNodeSegments(final String... segments) {
        return Joiner.on(".").join(segments);
    }
    
    private static String getActualObjectName(final ShardingSphereDatabase database, final String schemaName,
                                              final IdentifierValue objectIdentifierValue, final ConfigurationProperties props,
                                              final IdentifierScope scope, final Function<ShardingSphereSchema, java.util.stream.Stream<String>> actualNameStream) {
        if (null == objectIdentifierValue || null == objectIdentifierValue.getValue()) {
            return null;
        }
        DatabaseIdentifierContext identifierContext = DatabaseIdentifierContextFactory.create(database.getProtocolType(), database.getResourceMetaData(), props);
        IdentifierCaseRule rule = identifierContext.getRule(scope);
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue(schemaName), props);
        ShardingSphereSchema schema = database.getSchema(actualSchemaName);
        if (null != schema) {
            Optional<String> matchedName = actualNameStream.apply(schema)
                    .filter(each -> rule.matches(each, objectIdentifierValue.getValue(), objectIdentifierValue.getQuoteCharacter())).findFirst();
            if (matchedName.isPresent()) {
                return matchedName.get();
            }
        }
        return QuoteCharacter.NONE == objectIdentifierValue.getQuoteCharacter() && LookupMode.NORMALIZED == rule.getLookupMode(objectIdentifierValue.getQuoteCharacter())
                ? rule.normalize(objectIdentifierValue.getValue())
                : objectIdentifierValue.getValue();
    }
}
