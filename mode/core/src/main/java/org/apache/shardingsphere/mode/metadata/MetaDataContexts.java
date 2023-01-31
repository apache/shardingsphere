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

package org.apache.shardingsphere.mode.metadata;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.data.builder.ShardingSphereDataBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Meta data contexts.
 */
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final MetaDataPersistService persistService;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereData shardingSphereData;
    
    public MetaDataContexts(final MetaDataPersistService persistService, final ShardingSphereMetaData metaData) {
        this.persistService = persistService;
        this.metaData = metaData;
        this.shardingSphereData = initShardingSphereData(metaData);
    }
    
    private ShardingSphereData initShardingSphereData(final ShardingSphereMetaData metaData) {
        if (metaData.getDatabases().isEmpty()) {
            return new ShardingSphereData();
        }
        ShardingSphereData result = Optional.ofNullable(metaData.getDatabases().values().iterator().next().getProtocolType())
                // TODO can `protocolType instanceof OpenGaussDatabaseType ? "PostgreSQL" : protocolType.getType()` replace to trunk database type?
                .flatMap(protocolType -> TypedSPILoader.findService(ShardingSphereDataBuilder.class, protocolType instanceof OpenGaussDatabaseType ? "PostgreSQL" : protocolType.getType())
                        .map(builder -> builder.build(metaData)))
                .orElseGet(ShardingSphereData::new);
        Optional<ShardingSphereData> loadedShardingSphereData = Optional.ofNullable(persistService.getShardingSphereDataPersistService())
                .flatMap(shardingSphereDataPersistService -> shardingSphereDataPersistService.load(metaData));
        loadedShardingSphereData.ifPresent(optional -> useLoadedToReplaceInit(result, optional));
        refreshRootLogger(metaData.getProps().getProps());
        return result;
    }
    
    private void useLoadedToReplaceInit(final ShardingSphereData initShardingSphereData, final ShardingSphereData loadedShardingSphereData) {
        for (Entry<String, ShardingSphereDatabaseData> entry : initShardingSphereData.getDatabaseData().entrySet()) {
            if (loadedShardingSphereData.getDatabaseData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitByDatabaseData(entry.getValue(), loadedShardingSphereData.getDatabaseData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitByDatabaseData(final ShardingSphereDatabaseData initDatabaseData, final ShardingSphereDatabaseData loadedDatabaseData) {
        for (Entry<String, ShardingSphereSchemaData> entry : initDatabaseData.getSchemaData().entrySet()) {
            if (loadedDatabaseData.getSchemaData().containsKey(entry.getKey())) {
                useLoadedToReplaceInitBySchemaData(entry.getValue(), loadedDatabaseData.getSchemaData().get(entry.getKey()));
            }
        }
    }
    
    private void useLoadedToReplaceInitBySchemaData(final ShardingSphereSchemaData initSchemaData, final ShardingSphereSchemaData loadedSchemaData) {
        for (Entry<String, ShardingSphereTableData> entry : initSchemaData.getTableData().entrySet()) {
            if (loadedSchemaData.getTableData().containsKey(entry.getKey())) {
                entry.setValue(loadedSchemaData.getTableData().get(entry.getKey()));
            }
        }
    }
    
    private void refreshRootLogger(final Properties props) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        renewRootLoggerLevel(rootLogger, props);
    }
    
    private void renewRootLoggerLevel(final Logger rootLogger, final Properties props) {
        rootLogger.setLevel(Level.valueOf(props.getOrDefault(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getKey(), ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getDefaultValue()).toString()));
    }
    
    @Override
    public void close() {
        persistService.getRepository().close();
        metaData.getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource);
        metaData.getDatabases().values().forEach(each -> each.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResource));
    }
}
