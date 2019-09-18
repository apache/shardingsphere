package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.schedule.Event;
import info.avalon566.shardingscaling.job.schedule.EventType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.sync.core.SyncExecutor;
import info.avalon566.shardingscaling.sync.mysql.MysqlReader;
import info.avalon566.shardingscaling.sync.mysql.MysqlWriter;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author avalon566
 */
public class TableSliceSyncJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableSliceSyncJob.class);

    private final SyncConfiguration syncConfiguration;

    private final Reporter reporter;

    public TableSliceSyncJob(SyncConfiguration syncConfiguration, Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
    }

    public void run() {
        var reader = new MysqlReader(syncConfiguration.getReaderConfiguration());
        var writer = new MysqlWriter(syncConfiguration.getWriterConfiguration());
        var executor = new SyncExecutor(reader, Arrays.asList(writer));
        executor.run();
        new Thread(() -> {
            try {
                executor.waitFinish();
                LOGGER.info("{} table slice sync finish", syncConfiguration.getReaderConfiguration().getTableName());
                reporter.report(new Event(EventType.FINISHED));
            } catch (Exception ex) {
                LOGGER.info("{} table slice sync exception exit", syncConfiguration.getReaderConfiguration().getTableName());
                reporter.report(new Event(EventType.FINISHED));
            }
        }).start();
    }
}
