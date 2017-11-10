package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.spring.boot.util.EmbedTestingServer;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootShardingTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class OrchestrationSpringBootShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertWithShardingDataSource() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(dataSource instanceof ShardingDataSource);
        Field field = ShardingDataSource.class.getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(dataSource);
        for (DataSource each : shardingContext.getShardingRule().getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxActive(), is(16));
        }
        //assertTrue(shardingContext.isShowSQL());
    }
}
