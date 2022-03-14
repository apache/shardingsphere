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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Show readwrite-splitting read resources executor.
 */
public final class ShowReadwriteSplittingReadResourcesHandler extends QueryableRALBackendHandler<ShowReadwriteSplittingReadResourcesStatement, ShowReadwriteSplittingReadResourcesHandler> {
    
    private static final String DELIMITER = "\\.";
    
    private static final String RESOURCE = "resource";
    
    private static final String STATUS = "status";
    
    private static final String DELAY_TIME = "delay_time (ms)";
    
    private static final String DISABLED = "disabled";
    
    private static final String ENABLED = "enabled";
    
    private ConnectionSession connectionSession;
    
    @Override
    public ShowReadwriteSplittingReadResourcesHandler init(final HandlerParameter<ShowReadwriteSplittingReadResourcesStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(RESOURCE, STATUS, DELAY_TIME);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(schemaName);
        Collection<String> allReadResources = getAllReadResources(metaData);
        Collection<String> disabledResources = getDisabledReadResource(schemaName, metaDataContexts.getMetaDataPersistService().orElse(null));
        return buildRows(allReadResources, disabledResources);
    }
    
    private Collection<String> getAllReadResources(final ShardingSphereMetaData metaData) {
        Collection<String> configuredResources = getConfiguredReadResources(metaData);
        Collection<String> autoAwareResources = getAutoAwareReadResources(metaData);
        return Stream.of(configuredResources, autoAwareResources).flatMap(Collection::stream).distinct().collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<String> getConfiguredReadResources(final ShardingSphereMetaData metaData) {
        Collection<ReadwriteSplittingRuleConfiguration> ruleConfiguration = metaData.getRuleMetaData().findRuleConfiguration(ReadwriteSplittingRuleConfiguration.class);
        return ruleConfiguration.stream().map(ReadwriteSplittingRuleConfiguration::getDataSources).flatMap(Collection::stream).filter(Objects::nonNull)
                .map(ReadwriteSplittingDataSourceRuleConfiguration::getReadDataSourceNames).filter(Optional::isPresent)
                .map(each -> deconstructString(each.get())).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<String> getAutoAwareReadResources(final ShardingSphereMetaData metaData) {
        Map<String, Map<String, String>> autoAwareResourceData = getAutoAwareResourceData(metaData);
        return autoAwareResourceData.values().stream()
                .map(map -> map.get(ExportableConstants.REPLICA_DATA_SOURCE_NAMES)).filter(Objects::nonNull).map(this::deconstructString)
                .flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Map<String, Map<String, String>> getAutoAwareResourceData(final ShardingSphereMetaData metaData) {
        return metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof ExportableRule)
                .map(each -> ((ExportableRule) each).export(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE))
                .map(each -> (Map<String, Map<String, String>>) each.orElse(Collections.emptyMap()))
                .map(Map::entrySet).flatMap(Collection::stream).filter(entry -> !entry.getValue().isEmpty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    private Collection<String> getDisabledReadResource(final String schemaName, final MetaDataPersistService persistService) {
        if (null == persistService || null == persistService.getRepository()) {
            return Collections.emptyList();
        }
        //TODO API for getting disabled nodes needs to be adjusted
        List<String> disableResources = persistService.getRepository().getChildrenKeys(StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE));
        if (!disableResources.isEmpty()) {
            return disableResources.stream().filter(Objects::nonNull).filter(each -> schemaName.equals(each.split(DELIMITER)[0])).map(each -> each.split(DELIMITER)[1])
                    .collect(Collectors.toCollection(LinkedList::new));
        }
        return Collections.emptyList();
    }
    
    private Collection<List<Object>> buildRows(final Collection<String> allReadResources, final Collection<String> disabledResourceRows) {
        return allReadResources.stream().map(each -> buildRow(each, getDelayTime(), disabledResourceRows.contains(each))).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private String getDelayTime() {
        //TODO Need to implement api to get delay time
        return "";
    }
    
    private LinkedList<String> deconstructString(final String str) {
        return new LinkedList<>(Arrays.asList(str.split(",")));
    }
    
    private List<Object> buildRow(final String resource, final String delayTime, final boolean isDisabled) {
        return Arrays.asList(resource, isDisabled ? DISABLED : ENABLED, delayTime);
    }
}
