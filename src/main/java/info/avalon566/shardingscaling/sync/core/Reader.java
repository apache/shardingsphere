package info.avalon566.shardingscaling.sync.core;

import java.util.List;

/**
 * @author avalon566
 */
public interface Reader extends Runner {

    void setChannel(Channel channel);

    void read(Channel channel);

    List<RdbmsConfiguration> split(int concurrency);
}