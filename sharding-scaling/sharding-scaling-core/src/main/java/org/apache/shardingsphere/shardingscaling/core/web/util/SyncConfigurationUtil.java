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

package org.apache.shardingsphere.shardingscaling.core.web.util;

import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * SyncConfiguration Util.
 *
 * @author ssxlulu
 */
public class SyncConfigurationUtil {

    /**
     * Split ScalingConfiguration to SyncConfigurations.
     *
     * @param scalingConfiguration ScalingConfiguration
     * @return List of SyncConfigurations
     */
    public static List<SyncConfiguration> toSyncConfigurations(final ScalingConfiguration scalingConfiguration) {
        RuleConfiguration ruleConfig = scalingConfiguration.getRuleConfiguration();
        List<SyncConfiguration> syncConfigurations = new ArrayList<SyncConfiguration>(ruleConfig.getDataSources().size());
        for (RuleConfiguration.YamlDataSourceParameter entry : ruleConfig.getDataSources()) {
            RdbmsConfiguration readerConfiguration = new RdbmsConfiguration();
            DataSourceConfiguration readerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                    entry.getUrl(),
                    entry.getUsername(),
                    entry.getPassword());
            readerConfiguration.setDataSourceConfiguration(readerDataSourceConfiguration);
            RdbmsConfiguration writerConfiguration = new RdbmsConfiguration();
            DataSourceConfiguration writerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                    ruleConfig.getDestinationDataSources().getUrl(),
                    ruleConfig.getDestinationDataSources().getUsername(),
                    ruleConfig.getDestinationDataSources().getPassword());
            writerConfiguration.setDataSourceConfiguration(writerDataSourceConfiguration);
            syncConfigurations.add(new SyncConfiguration(scalingConfiguration.getJobConfiguration().getConcurrency(), readerConfiguration, writerConfiguration));
        }
        return syncConfigurations;
    }
}
