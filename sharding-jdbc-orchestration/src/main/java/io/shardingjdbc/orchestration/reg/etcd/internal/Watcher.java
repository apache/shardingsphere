package io.shardingjdbc.orchestration.reg.etcd.internal;

/**
 * Watcher.
 * 
 * @author junxiong
 */
public interface Watcher {
    /**
     * get watch id.
     *
     * @return long
     */
    long getId();

    /**
     * get watch key.
     *
     * @return String
     */
    String getKey();

    /**
     * add watcher listener.
     *
     * @param watcherListener WatcherListener
     */
    void addWatcherListener(WatcherListener watcherListener);

    /**
     * cancel watcher.
     */
    void cancel();
}
