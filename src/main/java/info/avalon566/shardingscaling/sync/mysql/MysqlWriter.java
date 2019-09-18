package info.avalon566.shardingscaling.sync.mysql;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.jdbc.AbstractJdbcWriter;

/**
 * @author avalon566
 */
public class MysqlWriter extends AbstractJdbcWriter {

    public MysqlWriter(RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }
}