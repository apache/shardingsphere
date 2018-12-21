/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.xa.convert.swap.impl;

import io.shardingsphere.transaction.xa.convert.swap.AdvancedMapUpdater;
import io.shardingsphere.transaction.xa.convert.swap.DataSourceSwapperAdapter;

import java.util.Collection;
import java.util.Collections;

/**
 * Druid parameter swapper.
 *
 * @author zhaojun
 */
public final class DruidParameterSwapper extends DataSourceSwapperAdapter {
    
    private static final String DRUID_CLASS_NAME = "com.alibaba.druid.pool.DruidDataSource";
    
    @Override
    protected void convertProperties(final AdvancedMapUpdater<String, Object> updater) {
        updater.transfer("maxActive", "maxPoolSize");
        updater.transfer("minIdle", "minPoolSize");
        updater.transfer("maxWait", "connectionTimeoutMilliseconds");
        updater.transfer("minEvictableIdleTimeMillis", "idleTimeoutMilliseconds");
        updater.transfer("timeBetweenEvictionRunsMillis", "maintenanceIntervalMilliseconds");
    }
    
    @Override
    public Collection<String> getDataSourceClassNames() {
        return Collections.singleton(DRUID_CLASS_NAME);
    }
}
