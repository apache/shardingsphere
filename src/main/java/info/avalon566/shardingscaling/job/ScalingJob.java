package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.RuleConfiguration;
import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.Scheduler;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import info.avalon566.shardingscaling.utils.YamlUtil;
import lombok.var;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
public class ScalingJob {

    private static final String INPUT_SHARDING_CONFIG = "input-sharding-config";
    private static final String OUTPUT_JDBC_URL = "output-jdbc-url";
    private static final String OUTPUT_JDBC_USERNAME = "output-jdbc-username";
    private static final String OUTPUT_JDBC_PASSWORD = "output-jdbc-password";

    private final String[] args;
    private final Scheduler scheduler;

    public ScalingJob(String[] args, Scheduler scheduler) {
        this.args = args;
        this.scheduler = scheduler;
    }

    public void run() {
        try {
            var commandLine = parseCommand(args);
            var syncConfigurations = toSyncConfigurations(commandLine);
            scheduler.schedule(syncConfigurations);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private CommandLine parseCommand(String[] args) throws ParseException {
        var options = new Options();
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(INPUT_SHARDING_CONFIG).desc("shardingsphere 路由配置文件").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_URL).desc("输出 jdbc url").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_USERNAME).desc("输出 jdbc 用户名").build());
        options.addOption(Option.builder().required(true).hasArg(true).longOpt(OUTPUT_JDBC_PASSWORD).desc("输出 jdbc 密码").build());
        return new DefaultParser().parse(options, args);
    }

    private List<SyncConfiguration> toSyncConfigurations(CommandLine commandLine) throws FileNotFoundException {
        var ruleConfig = loadRuleConfiguration(commandLine.getOptionValue(INPUT_SHARDING_CONFIG));
        var syncConfigurations = new ArrayList<SyncConfiguration>(ruleConfig.getDataSources().size());
        ruleConfig.getDataSources().forEach((s, yamlDataSourceParameter) -> {
            var readerConfiguration = new RdbmsConfiguration();
            readerConfiguration.setJdbcUrl(yamlDataSourceParameter.getUrl());
            readerConfiguration.setUsername(yamlDataSourceParameter.getUsername());
            readerConfiguration.setPassword(yamlDataSourceParameter.getPassword());
            var writerConfiguration = new RdbmsConfiguration();
            writerConfiguration.setJdbcUrl(commandLine.getOptionValue(OUTPUT_JDBC_URL));
            writerConfiguration.setUsername(commandLine.getOptionValue(OUTPUT_JDBC_USERNAME));
            writerConfiguration.setPassword(commandLine.getOptionValue(OUTPUT_JDBC_PASSWORD));
            syncConfigurations.add(new SyncConfiguration(SyncType.Database, 3, readerConfiguration, writerConfiguration));
        });
        return syncConfigurations;
    }

    private RuleConfiguration loadRuleConfiguration(String fileName) throws FileNotFoundException {
        if(fileName.startsWith("/")) {
            return YamlUtil.parse(fileName, RuleConfiguration.class);
        } else {
            var fullFileName = RuntimeUtil.getBasePath() + File.separator + fileName;
            return YamlUtil.parse(fullFileName, RuleConfiguration.class);
        }
    }
}
