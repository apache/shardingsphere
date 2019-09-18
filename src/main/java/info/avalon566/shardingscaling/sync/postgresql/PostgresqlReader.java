package info.avalon566.shardingscaling.sync.postgresql;

import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.jdbc.AbstractJdbcReader;

/**
 * @author avalon566
 */
public class PostgresqlReader extends AbstractJdbcReader {

    public PostgresqlReader(RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }
}