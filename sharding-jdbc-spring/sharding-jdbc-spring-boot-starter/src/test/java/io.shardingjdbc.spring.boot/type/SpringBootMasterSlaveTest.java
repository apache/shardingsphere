package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class SpringBootMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertWithMasterSlaveDataSource() {
        assertTrue(dataSource instanceof MasterSlaveDataSource);
        for (DataSource each : ((MasterSlaveDataSource) dataSource).getAllDataSources().values()) {
            assertThat(((BasicDataSource) each).getMaxActive(), is(16));
        }
    }
}
