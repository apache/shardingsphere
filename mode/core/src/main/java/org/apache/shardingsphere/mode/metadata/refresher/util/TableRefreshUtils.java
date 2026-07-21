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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
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
     * Get table load candidate name.
     *
     * @param database database
     * @param tableIdentifierValue table identifier value
     * @return table load candidate name
     */
    public static String getTableLoadCandidateName(final ShardingSphereDatabase database, final IdentifierValue tableIdentifierValue) {
        return getLoadCandidateName(database, tableIdentifierValue, IdentifierScope.TABLE);
    }
    
    /**
     * Get view load candidate name.
     *
     * @param database database
     * @param viewIdentifierValue view identifier value
     * @return view load candidate name
     */
    public static String getViewLoadCandidateName(final ShardingSphereDatabase database, final IdentifierValue viewIdentifierValue) {
        return getLoadCandidateName(database, viewIdentifierValue, IdentifierScope.VIEW);
    }
    
    /**
     * Get actual table name.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableIdentifierValue table identifier value
     * @return actual table name
     */
    public static String getActualTableName(final ShardingSphereDatabase database, final String schemaName,
                                            final IdentifierValue tableIdentifierValue) {
        return getActualObjectName(database, schemaName, tableIdentifierValue, IdentifierScope.TABLE,
                schema -> schema.getAllTables().stream().map(ShardingSphereTable::getName));
    }
    
    /**
     * Get actual table names.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableIdentifierValues table identifier values
     * @return actual table names
     */
    public static Collection<String> getActualTableNames(final ShardingSphereDatabase database, final String schemaName,
                                                         final Collection<IdentifierValue> tableIdentifierValues) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : tableIdentifierValues) {
            String actualTableName = getActualTableName(database, schemaName, each);
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
     * @return actual view names
     */
    public static Collection<String> getActualViewNames(final ShardingSphereDatabase database, final String schemaName,
                                                        final Collection<IdentifierValue> viewIdentifierValues) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : viewIdentifierValues) {
            String actualViewName = getActualViewName(database, schemaName, each);
            if (null != actualViewName) {
                result.add(actualViewName);
            }
        }
        return result;
    }
    
    /**
     * Get actual view name.
     *
     * @param database database
     * @param schemaName schema name
     * @param viewIdentifierValue view identifier value
     * @return actual view name
     */
    public static String getActualViewName(final ShardingSphereDatabase database, final String schemaName,
                                           final IdentifierValue viewIdentifierValue) {
        return getActualObjectName(database, schemaName, viewIdentifierValue, IdentifierScope.VIEW,
                schema -> schema.getAllViews().stream().map(ShardingSphereView::getName));
    }
    
    /**
     * Get actual index name.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableName table name
     * @param indexIdentifierValue index identifier value
     * @return actual index name
     */
    public static String getActualIndexName(final ShardingSphereDatabase database, final String schemaName, final String tableName,
                                            final IdentifierValue indexIdentifierValue) {
        return getActualObjectName(database, schemaName, tableName, indexIdentifierValue, IdentifierScope.INDEX,
                ShardingSphereTable::getAllIndexes, ShardingSphereIndex::getName);
    }
    
    /**
     * Get actual column names.
     *
     * @param database database
     * @param schemaName schema name
     * @param tableName table name
     * @param columnIdentifierValues column identifier values
     * @return actual column names
     */
    public static Collection<String> getActualColumnNames(final ShardingSphereDatabase database, final String schemaName, final String tableName,
                                                          final Collection<IdentifierValue> columnIdentifierValues) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : columnIdentifierValues) {
            String actualColumnName = getActualObjectName(database, schemaName, tableName, each, IdentifierScope.COLUMN,
                    ShardingSphereTable::getAllColumns, ShardingSphereColumn::getName);
            if (null != actualColumnName) {
                result.add(actualColumnName);
            }
        }
        return result;
    }
    
    /**
     * Find actual table name by index name.
     *
     * @param database database
     * @param schemaName schema name
     * @param indexIdentifierValue index identifier value
     * @return actual table name
     */
    public static Optional<String> findActualTableNameByIndex(final ShardingSphereDatabase database, final String schemaName,
                                                              final IdentifierValue indexIdentifierValue) {
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue(schemaName));
        ShardingSphereSchema schema = database.getSchema(actualSchemaName);
        if (null == schema) {
            return Optional.empty();
        }
        return schema.getAllTables().stream()
                .filter(each -> each.getAllIndexes().stream().map(ShardingSphereIndex::getName)
                        .anyMatch(indexName -> database.getIdentifierContext().matchesMetaData(IdentifierScope.INDEX, indexName, indexIdentifierValue)))
                .map(ShardingSphereTable::getName)
                .findFirst();
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
    
    private static String getLoadCandidateName(final ShardingSphereDatabase database, final IdentifierValue identifierValue, final IdentifierScope scope) {
        return database.getIdentifierContext().normalizeStorage(scope, identifierValue);
    }
    
    private static String getActualObjectName(final ShardingSphereDatabase database, final String schemaName,
                                              final IdentifierValue objectIdentifierValue,
                                              final IdentifierScope scope, final Function<ShardingSphereSchema, java.util.stream.Stream<String>> actualNameStream) {
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue(schemaName));
        ShardingSphereSchema schema = database.getSchema(actualSchemaName);
        if (null != schema) {
            Optional<String> matchedName = actualNameStream.apply(schema)
                    .filter(each -> database.getIdentifierContext().matchesMetaData(scope, each, objectIdentifierValue)).findFirst();
            if (matchedName.isPresent()) {
                return matchedName.get();
            }
        }
        return database.getIdentifierContext().normalizeStorage(scope, objectIdentifierValue);
    }
    
    private static <T> String getActualObjectName(final ShardingSphereDatabase database, final String schemaName, final String tableName,
                                                  final IdentifierValue objectIdentifierValue, final IdentifierScope scope,
                                                  final Function<ShardingSphereTable, Collection<T>> actualObjects, final Function<T, String> actualNameMapper) {
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue(schemaName));
        ShardingSphereSchema schema = database.getSchema(actualSchemaName);
        if (null != schema) {
            String actualTableName = getActualTableName(database, actualSchemaName, new IdentifierValue(tableName));
            ShardingSphereTable table = schema.getTable(actualTableName);
            Optional<String> matchedName = getMatchedObjectName(table, objectIdentifierValue, database.getIdentifierContext(), scope, actualObjects, actualNameMapper);
            if (matchedName.isPresent()) {
                return matchedName.get();
            }
        }
        return database.getIdentifierContext().normalizeStorage(scope, objectIdentifierValue);
    }
    
    private static <T> Optional<String> getMatchedObjectName(final ShardingSphereTable table, final IdentifierValue objectIdentifierValue,
                                                             final DatabaseIdentifierContext identifierContext, final IdentifierScope scope,
                                                             final Function<ShardingSphereTable, Collection<T>> actualObjects, final Function<T, String> actualNameMapper) {
        if (null == table) {
            return Optional.empty();
        }
        return actualObjects.apply(table).stream().map(actualNameMapper)
                .filter(each -> identifierContext.matchesMetaData(scope, each, objectIdentifierValue)).findFirst();
    }
}
