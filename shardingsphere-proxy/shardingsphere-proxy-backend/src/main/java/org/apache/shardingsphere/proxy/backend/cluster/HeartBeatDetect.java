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

package org.apache.shardingsphere.proxy.backend.cluster;

import org.apache.shardingsphere.cluster.configuration.config.HeartBeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartBeatResult;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Heart beat detect.
 */
public final class HeartBeatDetect extends AbstractHeartBeatDetect{
    
    private String sql;
    
    private String schemaName;
    
    private String dataSourceName;
    
    private DataSource dataSource;
    
    public HeartBeatDetect(final String schemaName, final String dataSourceName, final DataSource dataSource, final HeartBeatConfiguration configuration) {
        super(configuration.getRetryEnable(), configuration.getRetryMaximum(), configuration.getRetryInterval());
        this.sql = configuration.getSql();
        this.schemaName = schemaName;
        this.dataSourceName = dataSourceName;
        this.dataSource = dataSource;
    }
    
    @Override
    protected Boolean detect() {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql);
            ResultSet result = preparedStatement.executeQuery();
            return Objects.nonNull(result) && result.next();
        } catch (SQLException ex) {
        
        }
        return Boolean.FALSE;
    }
    
    @Override
    protected Map<String, HeartBeatResult> buildResult(final Boolean result) {
        Map<String, HeartBeatResult> heartBeatResultMap = new HashMap<>(1, 1);
        heartBeatResultMap.put(schemaName, new HeartBeatResult(dataSourceName, result, System.currentTimeMillis()));
        return heartBeatResultMap;
    }
}
