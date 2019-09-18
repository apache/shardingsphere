package info.avalon566.shardingscaling.sync.core;

/**
 * @author avalon566
 */
public abstract class AbstractRunner implements Runner {

    protected boolean running = false;

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }
}
