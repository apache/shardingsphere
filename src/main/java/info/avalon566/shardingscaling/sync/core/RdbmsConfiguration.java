package info.avalon566.shardingscaling.sync.core;

import lombok.Data;

/**
 * @author avalon566
 */
@Data
public class RdbmsConfiguration implements Cloneable {

    private String className;

    private String jdbcUrl;

    private String username;

    private String password;

    private String tableName;

    private String whereCondition;

    @Override
    public RdbmsConfiguration clone() {
        try {
            return (RdbmsConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}