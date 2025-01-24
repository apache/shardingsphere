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

package org.apache.shardingsphere.mode.metadata.refresher.metadata.federation;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.refresher.metadata.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

/**
 * Federation meta data refresh engine.
 */
@RequiredArgsConstructor
public final class FederationMetaDataRefreshEngine {
    
    private final MetaDataManagerPersistService metaDataManagerPersistService;
    
    private final ShardingSphereDatabase database;
    
    /**
     * Whether to need refresh meta data.
     *
     * @param sqlStatementContext SQL statement context
     * @return is need refresh meta data or not
     */
    public boolean isNeedRefresh(final SQLStatementContext sqlStatementContext) {
        Class<?> sqlStatementClass = sqlStatementContext.getSqlStatement().getClass().getSuperclass();
        return TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatementClass).isPresent();
    }
    
    /**
     * Refresh federation meta data.
     *
     * @param sqlStatementContext SQL statement context
     */
    @SuppressWarnings("unchecked")
    public void refresh(final SQLStatementContext sqlStatementContext) {
        Class<?> sqlStatementClass = sqlStatementContext.getSqlStatement().getClass().getSuperclass();
        TypedSPILoader.findService(FederationMetaDataRefresher.class, sqlStatementClass).ifPresent(
                optional -> optional.refresh(metaDataManagerPersistService, database, SchemaRefreshUtils.getSchemaName(database, sqlStatementContext), sqlStatementContext.getSqlStatement()));
    }
}
