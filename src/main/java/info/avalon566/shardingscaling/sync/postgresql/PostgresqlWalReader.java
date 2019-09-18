package info.avalon566.shardingscaling.sync.postgresql;

import info.avalon566.shardingscaling.sync.core.AbstractRunner;
import info.avalon566.shardingscaling.sync.core.Channel;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.core.Reader;
import lombok.Setter;

import java.util.List;

/**
 * @author avalon566
 */
public class PostgresqlWalReader extends AbstractRunner implements Reader {

    @Setter
    private Channel channel;

    @Override
    public void run() {
        //TODO:
        read(channel);
    }

    @Override
    public void read(Channel channel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RdbmsConfiguration> split(int concurrency) {
        throw new UnsupportedOperationException();
    }
}