/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.nullable;

import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ShardingForNullableWithAggregateTest extends AbstractShardingNullableDBUnitTest {
    
    private ShardingDataSource shardingDataSource;
    
    @Before
    public void init() throws SQLException {
        shardingDataSource = getShardingDataSource();
    }
    
    @Test
    public void assertSelectCount() throws SQLException, DatabaseUnitException {
        String sql = "SELECT COUNT(`user_id`) FROM `t_order`";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("COUNT(`user_id`)"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("COUNT(`user_id`)"), CoreMatchers.<Object>is(new BigDecimal("0")));
            assertThat(rs.getObject(1), CoreMatchers.<Object>is(new BigDecimal("0")));
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        String sql = "SELECT SUM(`user_id`) FROM `t_order`";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("SUM(`user_id`)"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("SUM(`user_id`)"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MAX(`user_id`) FROM `t_order`";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("MAX(`user_id`)"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("MAX(`user_id`)"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        String sql = "SELECT MIN(`user_id`) FROM `t_order`";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("MIN(`user_id`)"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("MIN(`user_id`)"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        String sql = "SELECT AVG(`user_id`) FROM `t_order`";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("AVG(`user_id`)"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("AVG(`user_id`)"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
}
