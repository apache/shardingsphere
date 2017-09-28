package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.spring.boot.SpringBootMain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootMain.class)
@ActiveProfiles("sharding")
public class SpringBootShardingTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    public void testWithShardingDataSource() {
        assertTrue(dataSource instanceof ShardingDataSource);
    }
}
