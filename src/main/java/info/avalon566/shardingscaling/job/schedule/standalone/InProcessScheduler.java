package info.avalon566.shardingscaling.job.schedule.standalone;

import info.avalon566.shardingscaling.job.DatabaseSyncJob;
import info.avalon566.shardingscaling.job.TableSliceSyncJob;
import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.job.schedule.Scheduler;
import lombok.var;

import java.util.List;

/**
 * @author avalon566
 */
public class InProcessScheduler implements Scheduler {

    @Override
    public Reporter schedule(List<SyncConfiguration> syncConfigurations) {
        var reporter = new InProcessReporter();
        syncConfigurations.forEach(syncConfiguration -> {
            if (SyncType.Database.equals(syncConfiguration.getSyncType())) {
                new DatabaseSyncJob(syncConfiguration).run();
            } else if (SyncType.TableSlice.equals(syncConfiguration.getSyncType())) {
                new TableSliceSyncJob(syncConfiguration, reporter).run();
            }
        });
        return reporter;
    }
}
