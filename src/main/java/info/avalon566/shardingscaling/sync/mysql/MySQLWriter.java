package info.avalon566.shardingscaling.sync.mysql;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.jdbc.AbstractJdbcWriter;

/**
 * @author avalon566
 */
public class MySQLWriter extends AbstractJdbcWriter {

    public MySQLWriter(RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }
}