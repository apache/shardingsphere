package io.shardingjdbc.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootShardingJdbcTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    public void testWithShardingDataSource() {
        assertNotNull(dataSource);
    }
}
