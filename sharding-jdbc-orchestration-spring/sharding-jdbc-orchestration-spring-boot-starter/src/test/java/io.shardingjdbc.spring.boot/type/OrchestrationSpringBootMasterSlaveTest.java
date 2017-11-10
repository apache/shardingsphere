package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class OrchestrationSpringBootMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertWithMasterSlaveDataSource() {
        assertTrue(dataSource instanceof MasterSlaveDataSource);
        for (DataSource each : ((MasterSlaveDataSource) dataSource).getAllDataSources().values()) {
            assertThat(((BasicDataSource) each).getMaxActive(), is(16));
        }
    }
}
