package info.avalon566.shardingscaling.sync.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author avalon566
 */
public class MemoryChannel implements Channel {

    public static final int PUSH_TIMEOUT = 1000;
    private final BlockingQueue<Record> queue = new ArrayBlockingQueue<>(1000);

    @Override
    public void pushRecord(Record dataRecord) {
        try {
            if (!queue.offer(dataRecord, PUSH_TIMEOUT, TimeUnit.HOURS)) {
                throw new RuntimeException();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Record popRecord() {
        try {
            return queue.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }
};