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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingRuleConstants;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Show readwrite-splitting read resources executor.
 */
@RequiredArgsConstructor
public final class ShowReadwriteSplittingReadResourcesExecutor extends AbstractShowExecutor {
    
    private static final String DELIMITER = "\\.";
    
    private static final String SCHEMA_NAME = "schema_name";
    
    private static final String RESOURCE = "resource";
    
    private static final String STATUS = "status";
    
    private static final String DISABLE = "disable";
    
    private static final String ENABLE = "enable";
    
    private final ShowReadwriteSplittingReadResourcesStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(
                new QueryHeader("", "", SCHEMA_NAME, SCHEMA_NAME, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", RESOURCE, RESOURCE, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false),
                new QueryHeader("", "", STATUS, STATUS, Types.VARCHAR, "VARCHAR", 64, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(schemaName);
        Collection<Object> notShownResourceRows = new LinkedHashSet<>();
        Collection<List<Object>> enableResourceRows = buildEnableResourceRows(schemaName, metaData, notShownResourceRows);
        Collection<List<Object>> disabledResourceRows = buildDisableResourceRows(schemaName, metaDataContexts.getMetaDataPersistService().orElse(null), notShownResourceRows);
        return new MultipleLocalDataMergedResult(mergeRows(enableResourceRows, disabledResourceRows, notShownResourceRows));
    }
    
    private Collection<List<Object>> buildEnableResourceRows(final String schemaName, final ShardingSphereMetaData metaData, final Collection<Object> notShownResourceRows) {
        LinkedList<String> configuredResourceRows = getConfiguredResourceRows(metaData);
        Collection<String> autoAwareResourceRows = getAutoAwareResourceRows(metaData, notShownResourceRows);
        return Stream.of(configuredResourceRows, autoAwareResourceRows).flatMap(Collection::stream).distinct()
                .map(each -> buildRow(schemaName, each, ENABLE)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private LinkedList<String> getConfiguredResourceRows(final ShardingSphereMetaData metaData) {
        Collection<ReadwriteSplittingRuleConfiguration> ruleConfiguration = metaData.getRuleMetaData().findRuleConfiguration(ReadwriteSplittingRuleConfiguration.class);
        return ruleConfiguration.stream().map(ReadwriteSplittingRuleConfiguration::getDataSources).flatMap(Collection::stream).filter(Objects::nonNull)
                .map(ReadwriteSplittingDataSourceRuleConfiguration::getReadDataSourceNames)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<String> getAutoAwareResourceRows(final ShardingSphereMetaData metaData, final Collection<Object> notShownResourceRows) {
        Map<String, Map<String, String>> autoAwareResourceData = getAutoAwareResourceData(metaData);
        return autoAwareResourceData.entrySet().stream().peek(entry -> notShownResourceRows.add(entry.getValue().get(ReadwriteSplittingRuleConstants.PRIMARY_DATA_SOURCE_NAME)))
                .map(entry -> entry.getValue().get(ReadwriteSplittingRuleConstants.REPLICA_DATA_SOURCE_NAMES)).filter(Objects::nonNull).map(this::deconstructString)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Map<String, Map<String, String>> getAutoAwareResourceData(final ShardingSphereMetaData metaData) {
        return metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof ExportableRule)
                .map(each -> ((ExportableRule) each).export().get(ReadwriteSplittingRuleConstants.AUTO_AWARE_DATA_SOURCE_KEY))
                .filter(Objects::nonNull).map(each -> (Map<String, Map<String, String>>) each)
                .map(Map::entrySet).flatMap(Collection::stream).filter(entry -> !entry.getValue().isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Collection<List<Object>> buildDisableResourceRows(final String schemaName, final MetaDataPersistService persistService, final Collection<Object> notShownResourceRows) {
        if (null == persistService || null == persistService.getRepository()) {
            return Collections.emptyList();
        }
        Collection<List<Object>> result = Collections.emptyList();
        List<String> instanceIds = persistService.getRepository().getChildrenKeys(StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE));
        if (!instanceIds.isEmpty()) {
            return instanceIds.stream().filter(Objects::nonNull).filter(each -> schemaName.equals(each.split(DELIMITER)[0])).map(each -> each.split(DELIMITER)[1])
                    .map(each -> buildRow(schemaName, each, DISABLE)).collect(Collectors.toCollection(LinkedList::new));
        }
        return result;
    }
    
    private Collection<List<Object>> mergeRows(final Collection<List<Object>> enableResourceRows, final Collection<List<Object>> disabledResourceRows, final Collection<Object> notShownResourceRows) {
        Collection<List<Object>> result = replaceDisableResourceRows(enableResourceRows, disabledResourceRows);
        return result.stream().filter(each -> !notShownResourceRows.contains(getResourceName(each))).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<List<Object>> replaceDisableResourceRows(final Collection<List<Object>> enableResourceRows, final Collection<List<Object>> disabledResourceRows) {
        Set<Object> disableResourceNames = disabledResourceRows.stream().map(this::getResourceName).collect(Collectors.toSet());
        Collection<List<Object>> result = enableResourceRows.stream().filter(each -> !disableResourceNames.contains(getResourceName(each))).collect(Collectors.toCollection(LinkedList::new));
        result.addAll(disabledResourceRows);
        return result;
    }
    
    private LinkedList<String> deconstructString(final String str) {
        return new LinkedList<>(Arrays.asList(str.split(",")));
    }
    
    private List<Object> buildRow(final String schemaName, final String resource, final String status) {
        return Arrays.asList(schemaName, resource, status);
    }
    
    private Object getResourceName(final List<Object> row) {
        return row.get(1);
    }
}
