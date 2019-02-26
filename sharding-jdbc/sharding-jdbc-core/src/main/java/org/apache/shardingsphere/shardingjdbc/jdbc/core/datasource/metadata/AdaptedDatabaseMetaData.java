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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata;

import org.apache.shardingsphere.core.rule.ShardingRule;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Adapted database meta data.
 *
 * @author zhangliang
 */
public abstract class AdaptedDatabaseMetaData extends ResultSetReturnedDatabaseMetaData {
    
    public AdaptedDatabaseMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule) {
        super(dataSourceMap, shardingRule);
    }
    
    @Override
    public final boolean ownInsertsAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean ownUpdatesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean ownDeletesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersInsertsAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersUpdatesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean othersDeletesAreVisible(final int type) {
        return true;
    }
    
    @Override
    public final boolean insertsAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean updatesAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean deletesAreDetected(final int type) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetType(final int type) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetConcurrency(final int type, final int concurrency) {
        return true;
    }
    
    @Override
    public final boolean supportsResultSetHoldability(final int holdability) {
        return true;
    }
    
    @Override
    public final boolean supportsTransactionIsolationLevel(final int level) {
        return true;
    }
}
