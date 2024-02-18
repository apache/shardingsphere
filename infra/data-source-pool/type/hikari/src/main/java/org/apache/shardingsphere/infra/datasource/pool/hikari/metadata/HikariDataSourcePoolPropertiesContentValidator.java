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

package org.apache.shardingsphere.infra.datasource.pool.hikari.metadata;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.datasource.pool.props.validator.DataSourcePoolPropertiesContentValidator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Data source pool properties content validator of HikariCP.
 */
public final class HikariDataSourcePoolPropertiesContentValidator implements DataSourcePoolPropertiesContentValidator {
    
    private static final long MIN_CONNECTION_TIMEOUT_MILLISECONDS = 250L;
    
    private static final long MIN_LIFETIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(30L);
    
    private static final long MIN_KEEP_ALIVE_TIME_MILLISECONDS = TimeUnit.SECONDS.toMillis(30L);
    
    @Override
    public void validate(final DataSourcePoolProperties props) {
        Map<String, Object> allLocalProps = props.getAllLocalProperties();
        validateConnectionTimeout(allLocalProps);
        validateIdleTimeout(allLocalProps);
        validateMaxLifetime(allLocalProps);
        validateMaximumPoolSize(allLocalProps);
        validateMinimumIdle(allLocalProps);
        validateKeepAliveTime(allLocalProps);
    }
    
    private void validateConnectionTimeout(final Map<String, Object> allLocalProps) {
        if (isExisted(allLocalProps, "connectionTimeout")) {
            long connectionTimeout = Long.parseLong(allLocalProps.get("connectionTimeout").toString());
            Preconditions.checkState(connectionTimeout >= MIN_CONNECTION_TIMEOUT_MILLISECONDS, "connectionTimeout can not less than %s ms.", MIN_CONNECTION_TIMEOUT_MILLISECONDS);
        }
    }
    
    private void validateIdleTimeout(final Map<String, Object> allLocalProps) {
        if (isExisted(allLocalProps, "idleTimeout")) {
            long idleTimeout = Long.parseLong(allLocalProps.get("idleTimeout").toString());
            Preconditions.checkState(idleTimeout >= 0, "idleTimeout can not be negative.");
        }
    }
    
    private void validateMaxLifetime(final Map<String, Object> allLocalProps) {
        if (isExisted(allLocalProps, "maxLifetime")) {
            long maxLifetime = Long.parseLong(allLocalProps.get("maxLifetime").toString());
            Preconditions.checkState(maxLifetime >= MIN_LIFETIME_MILLISECONDS, "maxLifetime can not less than %s ms.", MIN_LIFETIME_MILLISECONDS);
        }
    }
    
    private void validateMaximumPoolSize(final Map<String, Object> allLocalProps) {
        if (isExisted(allLocalProps, "maximumPoolSize")) {
            int maximumPoolSize = Integer.parseInt(allLocalProps.get("maximumPoolSize").toString());
            Preconditions.checkState(maximumPoolSize >= 1, "maxPoolSize can not less than 1.");
        }
    }
    
    private void validateMinimumIdle(final Map<String, Object> allLocalProps) {
        if (isExisted(allLocalProps, "minimumIdle")) {
            int minimumIdle = Integer.parseInt(allLocalProps.get("minimumIdle").toString());
            Preconditions.checkState(minimumIdle >= 0, "minimumIdle can not be negative.");
        }
    }
    
    private void validateKeepAliveTime(final Map<String, Object> allLocalProps) {
        if (!isExisted(allLocalProps, "keepaliveTime")) {
            return;
        }
        int keepAliveTime = Integer.parseInt(allLocalProps.get("keepaliveTime").toString());
        if (0 == keepAliveTime) {
            return;
        }
        Preconditions.checkState(keepAliveTime >= MIN_KEEP_ALIVE_TIME_MILLISECONDS, "keepaliveTime can not be less than %s ms.", MIN_KEEP_ALIVE_TIME_MILLISECONDS);
    }
    
    private boolean isExisted(final Map<String, Object> allLocalProps, final String key) {
        return allLocalProps.containsKey(key) && null != allLocalProps.get(key);
    }
    
    @Override
    public Object getType() {
        return "com.zaxxer.hikari.HikariDataSource";
    }
}
