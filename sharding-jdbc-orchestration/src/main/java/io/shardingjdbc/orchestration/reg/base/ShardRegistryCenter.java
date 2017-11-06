package io.shardingjdbc.orchestration.reg.base;

/**
 * Shard registry center
 *
 * @author junxiong
 */
public interface ShardRegistryCenter extends AutoCloseable {

    /**
     * Open shard registry center, setup event listeners.
     */
    void open();

    /**
     * register sharding-jdbc instance
     *
     * @param shardInstance sharding-jdbc instance
     */
    void registerShard(ShardInstance shardInstance);

    /**
     * set sharding-jdbc instance to a new state
     *
     * @param id         instance id
     * @param shardState shard state
     */
    void setShardState(String id, ShardState shardState);

    /**
     * retrieve sharding-jdbc instance state
     *
     * @param id instance id
     * @return shard state
     */
    ShardState getShardState(String id);

    /**
     * unregister sharding-jdbc instance
     *
     * @param id instance id
     */
    void unregisterShard(String id);
}
