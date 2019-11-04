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

import info.avalon566.shardingscaling.core.job.MigrateProgress;
import info.avalon566.shardingscaling.core.job.ScalingController;
import info.avalon566.shardingscaling.config.RuleConfiguration;
import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.config.SyncType;
import info.avalon566.shardingscaling.core.config.DataSourceConfiguration;
import info.avalon566.shardingscaling.core.config.JdbcDataSourceConfiguration;
import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import info.avalon566.shardingscaling.utils.YamlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final String INPUT_SHARDING_CONFIG = "input-sharding-config";

    private static final String OUTPUT_JDBC_URL = "output-jdbc-url";

    private static final String OUTPUT_JDBC_USERNAME = "output-jdbc-username";

    private static final String OUTPUT_JDBC_PASSWORD = "output-jdbc-password";
    
    /**
     * Main entry.
     *
     * @param args running args
     */
    public static void main(final String[] args) {
        log.info("ShardingScaling Startup");
        if ("scaling".equals(args[0])) {
            try {
                CommandLine commandLine = parseCommand(args);
                List<SyncConfiguration> syncConfigurations = toSyncConfigurations(commandLine);
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
            } catch (FileNotFoundException | ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static CommandLine parseCommand(final String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(INPUT_SHARDING_CONFIG).desc("shardingsphere 路由配置文件").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_URL).desc("输出 jdbc url").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_USERNAME).desc("输出 jdbc 用户名").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_PASSWORD).desc("输出 jdbc 密码").build());
        return new DefaultParser().parse(options, args);
    }

    private static List<SyncConfiguration> toSyncConfigurations(final CommandLine commandLine) throws FileNotFoundException {
        RuleConfiguration ruleConfig = loadRuleConfiguration(commandLine.getOptionValue(INPUT_SHARDING_CONFIG));
        List<SyncConfiguration> syncConfigurations = new ArrayList<SyncConfiguration>(ruleConfig.getDataSources().size());
        for (Map.Entry<String, RuleConfiguration.YamlDataSourceParameter> entry : ruleConfig.getDataSources().entrySet()) {
            RdbmsConfiguration readerConfiguration = new RdbmsConfiguration();
            DataSourceConfiguration readerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                    entry.getValue().getUrl(),
                    entry.getValue().getUsername(),
                    entry.getValue().getPassword());
            readerConfiguration.setDataSourceConfiguration(readerDataSourceConfiguration);
            RdbmsConfiguration writerConfiguration = new RdbmsConfiguration();
            DataSourceConfiguration writerDataSourceConfiguration = new JdbcDataSourceConfiguration(
                    commandLine.getOptionValue(OUTPUT_JDBC_URL),
                    commandLine.getOptionValue(OUTPUT_JDBC_USERNAME),
                    commandLine.getOptionValue(OUTPUT_JDBC_PASSWORD));
            writerConfiguration.setDataSourceConfiguration(writerDataSourceConfiguration);
            syncConfigurations.add(new SyncConfiguration(SyncType.NONE, 3, readerConfiguration, writerConfiguration));
        }
        return syncConfigurations;
    }

    private static RuleConfiguration loadRuleConfiguration(final String fileName) throws FileNotFoundException {
        if (fileName.startsWith("/")) {
            return YamlUtil.parse(fileName, RuleConfiguration.class);
        } else {
            String fullFileName = RuntimeUtil.getBasePath() + File.separator + fileName;
            return YamlUtil.parse(fullFileName, RuleConfiguration.class);
        }
    }
}
