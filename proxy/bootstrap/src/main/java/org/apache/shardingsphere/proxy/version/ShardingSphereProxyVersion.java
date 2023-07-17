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

package org.apache.shardingsphere.proxy.version;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.constant.DatabaseProtocolServerInfo;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;

import javax.sql.DataSource;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * ShardingSphere-Proxy version.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ShardingSphereProxyVersion {
    
    /**
     * Set version.
     * 
     * @param contextManager context manager
     */
    public static void setVersion(final ContextManager contextManager) {
        CommonConstants.PROXY_VERSION.set(ShardingSphereProxyVersion.getProxyVersion());
        contextManager.getMetaDataContexts().getMetaData().getDatabases().values().forEach(ShardingSphereProxyVersion::setDatabaseVersion);
    }
    
    private static String getProxyVersion() {
        String result = ShardingSphereVersion.VERSION;
        if (!ShardingSphereVersion.IS_SNAPSHOT || Strings.isNullOrEmpty(ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV)) {
            return result;
        }
        result += ShardingSphereVersion.BUILD_GIT_DIRTY ? "-dirty" : "";
        result += "-" + ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV;
        return result;
    }
    
    private static void setDatabaseVersion(final ShardingSphereDatabase database) {
        Optional<DataSource> dataSource = findDataSourceByProtocolType(database.getName(), database.getResourceMetaData(), database.getProtocolType());
        if (!dataSource.isPresent()) {
            return;
        }
        DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSource.get());
        log.info("{}, database name is `{}`", databaseServerInfo, database.getName());
        DatabaseProtocolServerInfo.setProtocolVersion(database.getName(), databaseServerInfo.getDatabaseVersion());
    }
    
    private static Optional<DataSource> findDataSourceByProtocolType(final String databaseName, final ShardingSphereResourceMetaData resourceMetaData, final DatabaseType protocolType) {
        Optional<String> dataSourceName = resourceMetaData.getStorageTypes().entrySet().stream().filter(entry -> entry.getValue().equals(protocolType)).map(Entry::getKey).findFirst();
        return dataSourceName.flatMap(optional -> Optional.ofNullable(DataSourceStateManager.getInstance().getEnabledDataSourceMap(databaseName, resourceMetaData.getDataSources()).get(optional)));
    }
}
