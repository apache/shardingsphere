package info.avalon566.shardingscaling.job.config;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author avalon566
 */
@Data
@AllArgsConstructor
public class SyncConfiguration {

    private SyncType syncType;

    /**
     * 单表写入并发度
     */
    private int concurrency;

    private RdbmsConfiguration readerConfiguration;

    private RdbmsConfiguration writerConfiguration;
}