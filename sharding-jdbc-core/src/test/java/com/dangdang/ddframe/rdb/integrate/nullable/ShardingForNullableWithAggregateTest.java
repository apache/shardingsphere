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

import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
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
        String sql = "SELECT COUNT(user_id) AS users_count FROM t_order";
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("users_count"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("users_count"), CoreMatchers.<Object>is(new BigDecimal("0")));
            assertThat(rs.getObject(1), CoreMatchers.<Object>is(new BigDecimal("0")));
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectSum() throws SQLException, DatabaseUnitException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectSumAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("user_id_sum"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("user_id_sum"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectMax() throws SQLException, DatabaseUnitException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectMaxAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("max_user_id"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("max_user_id"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectMin() throws SQLException, DatabaseUnitException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectMinAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("min_user_id"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("min_user_id"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
    @Test
    public void assertSelectAvg() throws SQLException, DatabaseUnitException {
        try (Connection conn = shardingDataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectAvgAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertThat(rs.getInt("user_id_avg"), is(0));
            assertThat(rs.getInt(1), is(0));
            assertThat(rs.getObject("user_id_avg"), nullValue());
            assertThat(rs.getObject(1), nullValue());
            assertFalse(rs.next());
        }
    }
    
}
