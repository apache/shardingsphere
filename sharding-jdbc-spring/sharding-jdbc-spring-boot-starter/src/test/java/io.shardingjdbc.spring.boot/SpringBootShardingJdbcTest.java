package io.shardingjdbc.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootMain.class)
public class SpringBootShardingJdbcTest {
    
    @Autowired
    private DataSource dataSource;
    
    @Test
    public void testWithShardingDataSource() {
        assertNotNull(dataSource);
    }
}
