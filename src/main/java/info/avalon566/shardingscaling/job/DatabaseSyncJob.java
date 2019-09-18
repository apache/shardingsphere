package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author avalon566
 */
public class DatabaseSyncJob {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseSyncJob.class);

    private final SyncConfiguration syncConfiguration;

    private final HistoryDataSyncer historyDataSyncer;

    private final RealtimeDataSyncer realtimeDataSyncer;

    public DatabaseSyncJob(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
        this.historyDataSyncer = new HistoryDataSyncer(syncConfiguration);
        this.realtimeDataSyncer = new RealtimeDataSyncer(syncConfiguration);
    }

    public void run() {
        realtimeDataSyncer.preRun();
        historyDataSyncer.run();
        realtimeDataSyncer.run();
    }
}
