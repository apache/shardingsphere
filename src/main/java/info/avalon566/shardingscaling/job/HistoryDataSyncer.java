package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.EventType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.job.schedule.standalone.InProcessScheduler;
import info.avalon566.shardingscaling.sync.jdbc.DbMetaDataUtil;
import info.avalon566.shardingscaling.sync.mysql.MysqlReader;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
public class HistoryDataSyncer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDataSyncer.class);

    private final SyncConfiguration syncConfiguration;

    public HistoryDataSyncer(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    public void run() {
        var configs = split(syncConfiguration);
        var reporter = new InProcessScheduler().schedule(configs);
        waitSlicesFinished(configs, reporter);
    }

    private List<SyncConfiguration> split(SyncConfiguration syncConfiguration) {
        List<SyncConfiguration> syncConfigurations = new ArrayList<>();
        // split by table
        new DbMetaDataUtil(syncConfiguration.getReaderConfiguration()).getTableNames().forEach(tableName -> {
            var readerConfig = syncConfiguration.getReaderConfiguration().clone();
            readerConfig.setTableName(tableName);
            // split by primary key range
            new MysqlReader(readerConfig).split(syncConfiguration.getConcurrency()).forEach(sliceConfig ->
                    syncConfigurations.add(new SyncConfiguration(SyncType.TableSlice, syncConfiguration.getConcurrency(),
                            sliceConfig, syncConfiguration.getWriterConfiguration().clone())));
        });
        return syncConfigurations;
    }

    private void waitSlicesFinished(List<SyncConfiguration> syncConfigurations, Reporter reporter) {
        var counter = 0;
        while (true) {
            var event = reporter.consumeEvent();
            if (EventType.FINISHED == event.getEventType()) {
                counter++;
            }
            if (syncConfigurations.size() == counter) {
                LOGGER.info("history data sync finish");
                break;
            }
        }
    }
}
