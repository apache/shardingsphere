package info.avalon566.shardingscaling.sync.core;

import lombok.AllArgsConstructor;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author avalon566
 */
@AllArgsConstructor
public class SyncExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncExecutor.class);

    private Future<?> readerFuture;
    private final List<Future<?>> writerFutures;
    private final Reader reader;
    private final List<Writer> writers;
    private final Channel channel;

    public SyncExecutor(Reader reader, List<Writer> writers) {
        this.reader = reader;
        this.writers = writers;
        this.channel = 1 == writers.size() ?
                new MemoryChannel() :
                new DispatcherChannel(writers.size());
        writerFutures = new ArrayList<>(writers.size());
    }

    public void run() {
        var pool1 = Executors.newSingleThreadExecutor();
        readerFuture = pool1.submit(() -> reader.read(channel));
        pool1.shutdown();
        var pool2 = Executors.newFixedThreadPool(writers.size());
        writers.forEach(writer -> {
            writerFutures.add(pool2.submit(() -> writer.write(channel)));
        });
        pool2.shutdown();
    }

    public void waitFinish() {
        writerFutures.forEach(writerFuture -> {
            try {
                writerFuture.get();
            } catch (Exception ex) {
                //TODO: shutdown reader and other writer
                throw new RuntimeException(ex);
            }
        });
    }
}
