package info.avalon566.shardingscaling.sync.core;

/**
 * @author avalon566
 */
public interface Writer extends Runner {

    void setChannel(Channel channel);

    void write(Channel channel);
}