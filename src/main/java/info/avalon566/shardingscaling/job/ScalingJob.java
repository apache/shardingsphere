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

package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.RuleConfiguration;
import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.Scheduler;
import info.avalon566.shardingscaling.sync.core.DataSourceConfiguration;
import info.avalon566.shardingscaling.sync.core.JdbcDataSourceConfiguration;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import info.avalon566.shardingscaling.utils.YamlUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scaling job.
 *
 * @author avalon566
 */
public class ScalingJob {

    private static final String INPUT_SHARDING_CONFIG = "input-sharding-config";

    private static final String OUTPUT_JDBC_URL = "output-jdbc-url";

    private static final String OUTPUT_JDBC_USERNAME = "output-jdbc-username";

    private static final String OUTPUT_JDBC_PASSWORD = "output-jdbc-password";

    private final String[] args;

    private final Scheduler scheduler;

    public ScalingJob(final String[] args, final Scheduler scheduler) {
        this.args = args;
        this.scheduler = scheduler;
    }

    /**
     * Run.
     */
    public void run() {
        try {
            CommandLine commandLine = parseCommand(args);
            List<SyncConfiguration> syncConfigurations = toSyncConfigurations(commandLine);
            scheduler.schedule(syncConfigurations);
        } catch (FileNotFoundException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandLine parseCommand(final String[] args) throws ParseException {
        Options options = new Options();
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(INPUT_SHARDING_CONFIG).desc("shardingsphere 路由配置文件").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_URL).desc("输出 jdbc url").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_USERNAME).desc("输出 jdbc 用户名").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_PASSWORD).desc("输出 jdbc 密码").build());
        return new DefaultParser().parse(options, args);
    }

    private List<SyncConfiguration> toSyncConfigurations(final CommandLine commandLine) throws FileNotFoundException {
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
            syncConfigurations.add(new SyncConfiguration(SyncType.Database, 3, readerConfiguration, writerConfiguration));
        }
        return syncConfigurations;
    }

    private RuleConfiguration loadRuleConfiguration(final String fileName) throws FileNotFoundException {
        if (fileName.startsWith("/")) {
            return YamlUtil.parse(fileName, RuleConfiguration.class);
        } else {
            String fullFileName = RuntimeUtil.getBasePath() + File.separator + fileName;
            return YamlUtil.parse(fullFileName, RuleConfiguration.class);
        }
    }
}
