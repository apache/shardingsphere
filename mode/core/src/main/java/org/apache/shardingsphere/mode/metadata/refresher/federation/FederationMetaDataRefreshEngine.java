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

package org.apache.shardingsphere.mode.metadata.refresher.federation;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

import java.util.Optional;

/**
 * Federation meta data refresh engine.
 */
public final class FederationMetaDataRefreshEngine {
    
    private final SQLStatementContext sqlStatementContext;
    
    @SuppressWarnings("rawtypes")
    private final FederationMetaDataRefresher refresher;
    
    public FederationMetaDataRefreshEngine(final SQLStatementContext sqlStatementContext) {
        this.sqlStatementContext = sqlStatementContext;
        refresher = findFederationMetaDataRefresher().orElse(null);
    }
    
    @SuppressWarnings("rawtypes")
    private Optional<FederationMetaDataRefresher> findFederationMetaDataRefresher() {
        Optional<FederationMetaDataRefresher> refresher = TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass());
        return refresher.isPresent() ? refresher : TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatementContext.getSqlStatement().getClass().getSuperclass());
    }
    
    /**
     * Whether to need refresh meta data.
     *
     * @return is need refresh meta data or not
     */
    public boolean isNeedRefresh() {
        return null != refresher;
    }
    
    /**
     * Refresh federation meta data.
     *
     * @param metaDataManagerPersistService meta data manager persist service
     * @param database database
     */
    @SuppressWarnings("unchecked")
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database) {
        refresher.refresh(metaDataManagerPersistService,
                sqlStatementContext.getSqlStatement().getDatabaseType(), database, SchemaRefreshUtils.getSchemaName(database, sqlStatementContext), sqlStatementContext.getSqlStatement());
    }
}
