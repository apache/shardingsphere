package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.*;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCClassDetermine;

public class JDBCClassDetermineTest {

    JDBCClassDetermine jdbcClassDetermine=new JDBCClassDetermine();

    @Test
    public void testMySQLUrl() {
        assertEquals("com.mysql.jdbc.Driver",jdbcClassDetermine.getDriverClassName("jdbc:mysql://localhost:3306/demo_ds_master"));
    }
    @Test
    public void testPostgreSQLUrl() {
        assertEquals("org.postgresql.Driver",jdbcClassDetermine.getDriverClassName("jdbc:postgresql://db.psql:5432/postgres"));

    }
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void testFailedUrl() {
        assertEquals("com.mysql.jdbc.Driver",jdbcClassDetermine.getDriverClassName("jdbc:oracle://db.psql:5432/postgres"));
    }

    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void testFailedUrl2() {
        assertEquals("com.mysql.jdbc.Driver",jdbcClassDetermine.getDriverClassName("xxxx"));
    }
}