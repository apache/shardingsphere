package org.apache.shardingsphere.infra.federation.optimizer.context.parser.fixture;

import java.util.Properties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect.OptimizerSQLDialectBuilder;

public class OptimizerSQLDialectBuilderFixture implements OptimizerSQLDialectBuilder {

    private final DatabaseType databaseType;

    public OptimizerSQLDialectBuilderFixture(final DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    public Properties build() {
        return new Properties();
    }

    @Override
    public String getType() {
        return databaseType.getType();
    }
}
