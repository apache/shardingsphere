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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.database.DatabaseServerInfo;
import org.apache.shardingsphere.proxy.frontend.protocol.DatabaseProtocolFrontendEngineFactory;

import javax.sql.DataSource;
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
        Optional<DataSource> sampleDataSource = findSampleBackendDataSource(contextManager);
        if (!sampleDataSource.isPresent()) {
            return;
        }
        DatabaseServerInfo databaseServerInfo = new DatabaseServerInfo(sampleDataSource.get());
        log.info(databaseServerInfo.toString());
        DatabaseProtocolFrontendEngineFactory.newInstance(DatabaseTypeRegistry.getTrunkDatabaseType(databaseServerInfo.getDatabaseName())).setDatabaseVersion(databaseServerInfo.getDatabaseVersion());
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
    
    private static Optional<DataSource> findSampleBackendDataSource(final ContextManager contextManager) {
        Optional<ShardingSphereMetaData> metaData = contextManager.getMetaDataContexts().getMetaDataMap().values().stream().filter(ShardingSphereMetaData::isComplete).findFirst();
        return metaData.flatMap(optional -> optional.getResource().getDataSources().values().stream().findFirst());
    }
}
