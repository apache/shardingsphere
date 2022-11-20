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

package org.apache.shardingsphere.infra.datasource.pool.metadata.type.hikari;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Objects;

/**
 * Hikari data source pool properties validator.
 */
public final class HikariDataSourcePoolPropertiesValidator implements DataSourcePoolPropertiesValidator {
    
    private static final long SOFT_TIMEOUT_FLOOR = Long.getLong("com.zaxxer.hikari.timeoutMs.floor", 250L);
    
    @Override
    public void validateProperties(final DataSourceProperties dataSourceProps) throws IllegalArgumentException {
        validateConnectionTimeout(dataSourceProps);
        validateIdleTimeout(dataSourceProps);
        validateMaximumPoolSize(dataSourceProps);
        validateMinimumIdle(dataSourceProps);
        validateValidationTimeout(dataSourceProps);
    }
    
    private void validateConnectionTimeout(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "connectionTimeout")) {
            return;
        }
        long connectionTimeout = Long.parseLong(dataSourceProps.getAllLocalProperties().get("connectionTimeout").toString());
        ShardingSpherePreconditions.checkState(connectionTimeout >= SOFT_TIMEOUT_FLOOR,
                () -> new IllegalArgumentException(String.format("connectionTimeout cannot be less than %sms", SOFT_TIMEOUT_FLOOR)));
    }
    
    private void validateIdleTimeout(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "idleTimeout")) {
            return;
        }
        long idleTimeout = Long.parseLong(dataSourceProps.getAllLocalProperties().get("idleTimeout").toString());
        ShardingSpherePreconditions.checkState(idleTimeout >= 0, () -> new IllegalArgumentException("idleTimeout cannot be negative"));
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
    
    private void validateValidationTimeout(final DataSourceProperties dataSourceProps) {
        if (!checkValueExist(dataSourceProps, "validationTimeout")) {
            return;
        }
        long validationTimeout = Long.parseLong(dataSourceProps.getAllLocalProperties().get("validationTimeout").toString());
        ShardingSpherePreconditions.checkState(validationTimeout >= SOFT_TIMEOUT_FLOOR,
                () -> new IllegalArgumentException(String.format("validationTimeout cannot be less than %sms", SOFT_TIMEOUT_FLOOR)));
    }
    
    private boolean checkValueExist(final DataSourceProperties dataSourceProps, final String key) {
        return dataSourceProps.getAllLocalProperties().containsKey(key) && Objects.nonNull(dataSourceProps.getAllLocalProperties().get(key));
    }
}
