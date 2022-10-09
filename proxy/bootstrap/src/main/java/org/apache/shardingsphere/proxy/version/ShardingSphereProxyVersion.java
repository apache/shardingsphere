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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.state.DataSourceStateManager;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.protocol.DatabaseProtocolFrontendEngineFactory;

import javax.sql.DataSource;

import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere-Proxy version.
 */
@Slf4j
public final class ShardingSphereProxyVersion {
    
    /**
     * Set version.
     * 
     * @param contextManager context manager
     */
    public static void setVersion(final ContextManager contextManager) {
        CommonConstants.PROXY_VERSION.set(ShardingSphereProxyVersion.getProxyVersion());
        contextManager.getMetaDataContexts().getMetaData().getDatabases().keySet()
                .forEach(each -> setDatabaseVersion(each, DataSourceStateManager.getInstance().getEnabledDataSourceMap(each, contextManager.getDataSourceMap(each))));
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
    
    private static void setDatabaseVersion(final String databaseName, final Map<String, DataSource> dataSources) {
        Optional<DataSource> dataSource = dataSources.values().stream().findFirst();
        if (!dataSource.isPresent()) {
            return;
        }
        DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(dataSource.get());
        log.info("{}, database name is `{}`", databaseServerInfo, databaseName);
        DatabaseProtocolFrontendEngineFactory
                .newInstance(DatabaseTypeEngine.getTrunkDatabaseType(databaseServerInfo.getDatabaseName()))
                .setDatabaseVersion(databaseName, databaseServerInfo.getDatabaseVersion());
    }
}
