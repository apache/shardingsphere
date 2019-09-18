package info.avalon566.shardingscaling.sync.postgresql;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.jdbc.AbstractJdbcWriter;

/**
 * @author avalon566
 */
public class PostgresqlWriter extends AbstractJdbcWriter {

    public PostgresqlWriter(RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }
}