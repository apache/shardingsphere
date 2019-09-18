package info.avalon566.shardingscaling.sync.core;

import info.avalon566.shardingscaling.sync.jdbc.DataRecord;
import lombok.var;

import java.util.HashMap;

/**
 * @author avalon566
 */
public class DispatcherChannel implements Channel {

    private final int channelNumber;

    /**
     * 通道Id : 通道
     */
    private final HashMap<String, MemoryChannel> channels = new HashMap<>();

    /**
     * 线程Id : 通道Id
     */
    private final HashMap<String, String> channelAssignment = new HashMap<>();

    public DispatcherChannel(int channelNumber) {
        this.channelNumber = channelNumber;
        for (int i = 0; i < channelNumber; i++) {
            channels.put(Integer.toString(i), new MemoryChannel());
        }
    }

    @Override
    public void pushRecord(Record record) {
        if (FinishedRecord.class.equals(record.getClass())) {
            // 广播事件
            channels.forEach((k, v) -> v.pushRecord(record));
        } else if (DataRecord.class.equals(record.getClass())) {
            // 表名哈希
            var dataRecord = (DataRecord) record;
            var index = Integer.toString(dataRecord.getTableName().hashCode() % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }

    @Override
    public Record popRecord() {
        var threadId = Long.toString(Thread.currentThread().getId());
        checkAssignment(threadId);
        return channels.get(channelAssignment.get(threadId)).popRecord();
    }

    private void checkAssignment(String threadId) {
        if (!channelAssignment.containsKey(threadId)) {
            synchronized (this) {
                channels.forEach((channelId, channel) -> {
                    if (!channelAssignment.containsValue(channelId)) {
                        channelAssignment.put(threadId, channelId);
                    }
                });
            }
        }
    }
}
