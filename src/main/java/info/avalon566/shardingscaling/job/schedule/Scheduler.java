package info.avalon566.shardingscaling.job.schedule;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;

import java.util.List;

/**
 * @author avalon566
 */
public interface Scheduler {

    Reporter schedule(List<SyncConfiguration> configs);
}
