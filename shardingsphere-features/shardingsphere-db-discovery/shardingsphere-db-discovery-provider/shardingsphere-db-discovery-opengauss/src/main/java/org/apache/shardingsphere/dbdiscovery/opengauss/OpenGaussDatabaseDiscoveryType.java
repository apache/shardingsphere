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

package org.apache.shardingsphere.dbdiscovery.opengauss;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobConfigurationAPIImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * OpenGauss data base discovery type.
 */
@Slf4j
public final class OpenGaussDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    private static final String DB_ROLE = "select local_role,db_state from pg_stat_get_stream_replications()";
    
    private static CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    private static final Map<String, ScheduleJobBootstrap> SCHEDULE_JOB_BOOTSTRAP_MAP = new HashMap<>(16, 1);
    
    private String oldPrimaryDataSource;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void checkDatabaseDiscoveryConfiguration(final String schemaName,
            final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.get(oldPrimaryDataSource).getConnection();
                Statement statement = connection.createStatement()) {
            checkRolePrimary(statement);
        }
    }
    
    private void checkRolePrimary(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
            while (resultSet.next()) {
                if (!"Primary".equals(resultSet.getString("local_role"))) {
                    throw new ShardingSphereConfigurationException("Instance is not Primary.");
                }
            }
        }
    }
    
    @Override
    public void updatePrimaryDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap,
            final Collection<String> disabledDataSourceNames, final String groupName) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(dataSourceMap);
        if (!disabledDataSourceNames.isEmpty()) {
            activeDataSourceMap.entrySet().removeIf(each -> disabledDataSourceNames.contains(each.getKey()));
        }
        String newPrimaryDataSource = determinePrimaryDataSource(activeDataSourceMap);
        if (newPrimaryDataSource.isEmpty()) {
            return;
        }
        if (!newPrimaryDataSource.equals(oldPrimaryDataSource)) {
            oldPrimaryDataSource = newPrimaryDataSource;
            ShardingSphereEventBus.getInstance()
                    .post(new PrimaryDataSourceChangedEvent(schemaName, groupName, newPrimaryDataSource));
        }
    }
    
    private String determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        String result = "";
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            try (Connection connection = entry.getValue().getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if (resultSet.getString("local_role").equals("Primary")
                            && resultSet.getString("db_state").equals("Normal")) {
                        return entry.getKey();
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find primary data source url", ex);
            }
        }
        return result;
    }
    
    @Override
    public void updateMemberState(final String schemaName, final Map<String, DataSource> dataSourceMap,
            final Collection<String> disabledDataSourceNames) {
        Map<String, DataSource> activeDataSourceMap = new HashMap<>(dataSourceMap);
        determineDisabledDataSource(schemaName, activeDataSourceMap);
    }
    
    private void determineDisabledDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            boolean disable = true;
            try (Connection connection = entry.getValue().getConnection();
                Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(DB_ROLE)) {
                if (resultSet.next()) {
                    if (resultSet.getString("local_role").equals("Standby") && resultSet.getString("db_state").equals("Normal")) {
                        disable = false;
                    }
                }
            } catch (final SQLException ex) {
                log.error("An exception occurred while find data source urls", ex);
            }
            ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, entry.getKey(), disable));
        }
    }
    
    @Override
    public void startPeriodicalUpdate(final String schemaName, final Map<String, DataSource> dataSourceMap,
            final Collection<String> disabledDataSourceNames, final String groupName) {
        if (null == coordinatorRegistryCenter) {
            ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(props.getProperty("zkServerLists"),
                    "opengauss-elasticjob");
            coordinatorRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
            coordinatorRegistryCenter.init();
        }
        if (null != SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName)) {
            SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName).shutdown();
        }
        SCHEDULE_JOB_BOOTSTRAP_MAP.put(groupName, new ScheduleJobBootstrap(coordinatorRegistryCenter,
                new OpenGaussHeartbeatJob(this, schemaName, dataSourceMap, disabledDataSourceNames, groupName),
                JobConfiguration.newBuilder("opengauss-" + groupName, 1).cron(props.getProperty("keepAliveCron")).build()));
        SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName).schedule();
    }
    
    @Override
    public String getPrimaryDataSource() {
        return oldPrimaryDataSource;
    }
    
    @Override
    public void updateProperties(final String groupName, final Properties props) {
        new JobConfigurationAPIImpl(coordinatorRegistryCenter)
                .updateJobConfiguration(createJobConfiguration("opengauss-" + groupName, props.getProperty("keepAliveCron")));
    }
    
    private JobConfigurationPOJO createJobConfiguration(final String jobName, final String cron) {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName(jobName);
        result.setCron(cron);
        result.setShardingTotalCount(1);
        return result;
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
