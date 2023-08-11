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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.datasource.pool.props.DataSourcePoolProperties;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Data source pool properties validator of HikariCP.
 */
public final class HikariDataSourcePoolPropertiesValidator implements DataSourcePoolPropertiesValidator {
    
    private static final long MIN_CONNECTION_TIMEOUT_MILLISECONDS = 250L;
    
    private static final long MIN_LIFETIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(30L);
    
    private static final long MIN_KEEP_ALIVE_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(30L);
    
    @Override
    public void validate(final DataSourcePoolProperties dataSourceProps) {
        Map<String, Object> allLocalProperties = dataSourceProps.getAllLocalProperties();
        validateConnectionTimeout(allLocalProperties);
        validateIdleTimeout(allLocalProperties);
        validateMaxLifetime(allLocalProperties);
        validateMaximumPoolSize(allLocalProperties);
        validateMinimumIdle(allLocalProperties);
        validateKeepAliveTime(allLocalProperties);
    }
    
    private void validateConnectionTimeout(final Map<String, Object> allLocalProperties) {
        if (isExisted(allLocalProperties, "connectionTimeout")) {
            long connectionTimeout = Long.parseLong(allLocalProperties.get("connectionTimeout").toString());
            Preconditions.checkState(connectionTimeout >= MIN_CONNECTION_TIMEOUT_MILLISECONDS, "connectionTimeout can not less than %s ms.", MIN_CONNECTION_TIMEOUT_MILLISECONDS);
        }
    }
    
    private void validateIdleTimeout(final Map<String, Object> allLocalProperties) {
        if (isExisted(allLocalProperties, "idleTimeout")) {
            long idleTimeout = Long.parseLong(allLocalProperties.get("idleTimeout").toString());
            Preconditions.checkState(idleTimeout >= 0, "idleTimeout can not be negative.");
        }
    }
    
    private void validateMaxLifetime(final Map<String, Object> allLocalProperties) {
        if (isExisted(allLocalProperties, "maxLifetime")) {
            long maxLifetime = Long.parseLong(allLocalProperties.get("maxLifetime").toString());
            Preconditions.checkState(maxLifetime >= MIN_LIFETIME_MILLISECONDS, "maxLifetime can not less than %s ms.", MIN_LIFETIME_MILLISECONDS);
        }
    }
    
    private void validateMaximumPoolSize(final Map<String, Object> allLocalProperties) {
        if (isExisted(allLocalProperties, "maximumPoolSize")) {
            int maximumPoolSize = Integer.parseInt(allLocalProperties.get("maximumPoolSize").toString());
            Preconditions.checkState(maximumPoolSize >= 1, "maxPoolSize can not less than 1.");
        }
    }
    
    private void validateMinimumIdle(final Map<String, Object> allLocalProperties) {
        if (isExisted(allLocalProperties, "minimumIdle")) {
            int minimumIdle = Integer.parseInt(allLocalProperties.get("minimumIdle").toString());
            Preconditions.checkState(minimumIdle >= 0, "minimumIdle can not be negative.");
        }
    }
    
    private void validateKeepAliveTime(final Map<String, Object> allLocalProperties) {
        if (!isExisted(allLocalProperties, "keepaliveTime")) {
            return;
        }
        int keepAliveTime = Integer.parseInt(allLocalProperties.get("keepaliveTime").toString());
        if (0 == keepAliveTime) {
            return;
        }
        Preconditions.checkState(keepAliveTime >= MIN_KEEP_ALIVE_TIME_MILLISECONDS, "keepaliveTime can not be less than %s ms.", MIN_KEEP_ALIVE_TIME_MILLISECONDS);
    }
    
    private boolean isExisted(final Map<String, Object> allLocalProperties, final String key) {
        return allLocalProperties.containsKey(key) && null != allLocalProperties.get(key);
    }
}
