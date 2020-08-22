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

package org.apache.shardingsphere.cluster.heartbeat.detect;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Heart beat detect.
 */
@Slf4j
public final class HeartbeatDetect extends AbstractHeartbeatDetect {
    
    private final String sql;
    
    private final String schemaName;
    
    private final String dataSourceName;
    
    private final DataSource dataSource;
    
    private final boolean dataSourceDisabled;
    
    public HeartbeatDetect(final String schemaName, final String dataSourceName, final DataSource dataSource,
                           final HeartbeatConfiguration configuration, final Boolean dataSourceDisabled) {
        super(configuration.isRetryEnable(), configuration.getRetryMaximum(), configuration.getRetryInterval(), !dataSourceDisabled);
        sql = configuration.getSql();
        this.schemaName = schemaName;
        this.dataSourceName = dataSourceName;
        this.dataSource = dataSource;
        this.dataSourceDisabled = dataSourceDisabled;
    }
    
    @Override
    protected boolean detect() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet result = preparedStatement.executeQuery()) {
                return Objects.nonNull(result) && result.next();
            }
        } catch (final SQLException ex) {
            log.error("Heart beat detect error", ex);
        }
        return Boolean.FALSE;
    }
    
    @Override
    protected Map<String, HeartbeatResult> buildResult(final boolean heartbeatResult) {
        Map<String, HeartbeatResult> result = new HashMap<>(1, 1);
        result.put(schemaName, new HeartbeatResult(dataSourceName, heartbeatResult, System.currentTimeMillis(), dataSourceDisabled));
        return result;
    }
}
