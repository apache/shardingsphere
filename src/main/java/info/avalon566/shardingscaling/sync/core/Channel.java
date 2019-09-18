package info.avalon566.shardingscaling.sync.core;

/**
 * @author avalon566
 */
public interface Channel {
    void pushRecord(Record dataRecord);

    Record popRecord();
}