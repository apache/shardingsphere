package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.dbcp.BasicDataSource;
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
@SpringBootTest(classes = SpringBootShardingTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class SpringBootShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertWithShardingDataSource() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(dataSource instanceof ShardingDataSource);
        Field field = ShardingDataSource.class.getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(dataSource);
        for (DataSource each : shardingContext.getShardingRule().getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxActive(), is(16));
        }
        assertTrue(shardingContext.isShowSQL());
    }
}
