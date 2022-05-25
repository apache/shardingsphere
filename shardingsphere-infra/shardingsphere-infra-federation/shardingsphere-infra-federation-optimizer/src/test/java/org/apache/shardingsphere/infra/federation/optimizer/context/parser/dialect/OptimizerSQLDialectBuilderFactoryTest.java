package org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect;

import java.util.Properties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.dialect.impl.MySQLOptimizerBuilder;
import org.apache.shardingsphere.infra.federation.optimizer.context.parser.fixture.OptimizerSQLDialectBuilderFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class OptimizerSQLDialectBuilderFactoryTest {

    @Test
    public void assertCreateOptimizerSQLDialectBuilder() {
        DatabaseType databaseType = DatabaseTypeFactory.getInstance("MySQL");
        OptimizerSQLDialectBuilder optimizerSQLDialectBuilder = new OptimizerSQLDialectBuilderFixture(databaseType);
        assertEquals(optimizerSQLDialectBuilder.getClass(), MySQLOptimizerBuilder.class);
        Properties properties = optimizerSQLDialectBuilder.build();
        assertEquals(properties, new Properties());

    }
}
