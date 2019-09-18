package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.sync.core.SyncExecutor;
import info.avalon566.shardingscaling.sync.core.Writer;
import info.avalon566.shardingscaling.sync.mysql.MysqlBinlogReader;
import info.avalon566.shardingscaling.sync.mysql.MysqlWriter;
import lombok.var;

import java.util.ArrayList;

/**
 * @author avalon566
 */
public class RealtimeDataSyncer {

    private final SyncConfiguration syncConfiguration;

    private final MysqlBinlogReader mysqlBinlogReader;

    public RealtimeDataSyncer(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
        mysqlBinlogReader = new MysqlBinlogReader(syncConfiguration.getReaderConfiguration());
    }

    public void preRun() {
        mysqlBinlogReader.markPosition();
    }

    public void run() {
        var writers = new ArrayList<Writer>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            writers.add(new MysqlWriter(syncConfiguration.getWriterConfiguration()));
        }
        new SyncExecutor(mysqlBinlogReader, writers).run();
    }
}
