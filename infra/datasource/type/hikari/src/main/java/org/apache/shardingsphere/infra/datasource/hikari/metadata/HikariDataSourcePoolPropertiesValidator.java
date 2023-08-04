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

package org.apache.shardingsphere.infra.datasource.hikari.metadata;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.concurrent.TimeUnit;

/**
 * Hikari data source pool properties validator.
 */
public final class HikariDataSourcePoolPropertiesValidator implements DataSourcePoolPropertiesValidator {
    
    private static final long CONNECTION_TIMEOUT_FLOOR = 250L;
    
    private static final long MAX_LIFETIME_FLOOR = TimeUnit.SECONDS.toMillis(30);
    
    private static final long KEEP_ALIVE_TIME_FLOOR = TimeUnit.SECONDS.toMillis(30);
    
    @Override
    public void validateProperties(final DataSourceProperties dataSourceProps) {
        validateConnectionTimeout(dataSourceProps);
        validateIdleTimeout(dataSourceProps);
        validateMaxLifetime(dataSourceProps);
        validateMaximumPoolSize(dataSourceProps);
        validateMinimumIdle(dataSourceProps);
        validateKeepAliveTime(dataSourceProps);
    }
    
    private void validateConnectionTimeout(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "connectionTimeout")) {
            return;
        }
        long connectionTimeout = Long.parseLong(dataSourceProps.getAllLocalProperties().get("connectionTimeout").toString());
        ShardingSpherePreconditions.checkState(connectionTimeout >= CONNECTION_TIMEOUT_FLOOR,
                () -> new IllegalArgumentException(String.format("connectionTimeout cannot be less than %sms", CONNECTION_TIMEOUT_FLOOR)));
    }
    
    private void validateIdleTimeout(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "idleTimeout")) {
            return;
        }
        long idleTimeout = Long.parseLong(dataSourceProps.getAllLocalProperties().get("idleTimeout").toString());
        ShardingSpherePreconditions.checkState(idleTimeout >= 0, () -> new IllegalArgumentException("idleTimeout cannot be negative"));
    }
    
    private void validateMaxLifetime(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "maxLifetime")) {
            return;
        }
        long maxLifetime = Long.parseLong(dataSourceProps.getAllLocalProperties().get("maxLifetime").toString());
        ShardingSpherePreconditions.checkState(maxLifetime >= MAX_LIFETIME_FLOOR, () -> new IllegalArgumentException(String.format("maxLifetime cannot be less than %sms", MAX_LIFETIME_FLOOR)));
    }
    
    private void validateMaximumPoolSize(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "maximumPoolSize")) {
            return;
        }
        int maximumPoolSize = Integer.parseInt(dataSourceProps.getAllLocalProperties().get("maximumPoolSize").toString());
        ShardingSpherePreconditions.checkState(maximumPoolSize >= 1, () -> new IllegalArgumentException("maxPoolSize cannot be less than 1"));
    }
    
    private void validateMinimumIdle(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "minimumIdle")) {
            return;
        }
        int minimumIdle = Integer.parseInt(dataSourceProps.getAllLocalProperties().get("minimumIdle").toString());
        ShardingSpherePreconditions.checkState(minimumIdle >= 0, () -> new IllegalArgumentException("minimumIdle cannot be negative"));
    }
    
    private void validateKeepAliveTime(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "keepaliveTime")) {
            return;
        }
        int keepAliveTime = Integer.parseInt(dataSourceProps.getAllLocalProperties().get("keepaliveTime").toString());
        if (keepAliveTime == 0) {
            return;
        }
        ShardingSpherePreconditions.checkState(keepAliveTime >= KEEP_ALIVE_TIME_FLOOR,
                () -> new IllegalArgumentException(String.format("keepaliveTime cannot be less than %sms", KEEP_ALIVE_TIME_FLOOR)));
    }
    
    private boolean checkValueExist(final DataSourceProperties dataSourceProps, final String key) {
        return dataSourceProps.getAllLocalProperties().containsKey(key) && null != dataSourceProps.getAllLocalProperties().get(key);
    }
}
