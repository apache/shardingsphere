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

package info.avalon566.shardingscaling;

import com.google.gson.Gson;
import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.config.ScalingConfiguration;
import info.avalon566.shardingscaling.core.config.ScalingContext;
import info.avalon566.shardingscaling.core.config.RuleConfiguration;
import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.core.config.DataSourceConfiguration;
import info.avalon566.shardingscaling.core.config.JdbcDataSourceConfiguration;
import info.avalon566.shardingscaling.core.config.SyncType;
import info.avalon566.shardingscaling.core.job.MigrateProgress;
import info.avalon566.shardingscaling.core.job.ScalingController;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap of ShardingScaling.
 *
 * @author avalon566
 */
@Slf4j
public class Bootstrap {

    static {
        PropertyConfigurator.configure(RuntimeUtil.getBasePath() + "conf" + File.separator + "log4j.properties");
    }

    private static final String CONFIG_FILE = "/conf/config.json";

    /**
     * Main entry.
     *
     * @param args running args
     */
    public static void main(final String[] args) {
        log.info("ShardingScaling Startup");
        try {
            initConfig(CONFIG_FILE);
            List<SyncConfiguration> syncConfigurations = toSyncConfigurations();
            final ScalingController scalingController = new ScalingController(syncConfigurations);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException ex) {
                            break;
                        }
                        for (MigrateProgress progress : scalingController.getProgresses()) {
                            log.info(progress.getLogPosition().toString());
                        }
                    }
                }
            }).start();
            scalingController.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initConfig(final String configFile) throws IOException {
        Gson gson = new Gson();
        InputStream fileInputStream = Bootstrap.class.getResourceAsStream(configFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        ScalingConfiguration scalingConfiguration = gson.fromJson(inputStreamReader, ScalingConfiguration.class);
        log.info(gson.toJson(scalingConfiguration));
        ScalingContext.getInstance().init(scalingConfiguration.getRuleConfiguration(), scalingConfiguration.getServerConfiguration());
    }

    private static List<SyncConfiguration> toSyncConfigurations() throws FileNotFoundException {
        RuleConfiguration ruleConfig = ScalingContext.getInstance().getRuleConfiguration();
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
            syncConfigurations.add(new SyncConfiguration(SyncType.NONE, ScalingContext.getInstance().getServerConfiguration().getConcurrency(), readerConfiguration, writerConfiguration));
        }
        return syncConfigurations;
    }
}
