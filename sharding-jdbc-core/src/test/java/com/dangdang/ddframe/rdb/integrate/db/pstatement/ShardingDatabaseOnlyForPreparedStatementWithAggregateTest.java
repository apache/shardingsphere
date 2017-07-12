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

package com.dangdang.ddframe.rdb.integrate.db.pstatement;

import com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil;
import com.dangdang.ddframe.rdb.integrate.db.AbstractShardingDatabaseOnlyDBUnitTest;
import org.dbunit.DatabaseUnitException;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ShardingDatabaseOnlyForPreparedStatementWithAggregateTest extends AbstractShardingDatabaseOnlyDBUnitTest {
    
    @Test
    public void assertSelectCountAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCount.xml", getShardingDataSource().getConnection(), "t_order", getDatabaseTestSQL().getSelectCountAliasSql());
    }
    
    @Test
    public void assertSelectCount() throws SQLException {
        try (Connection conn = getShardingDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectCountAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next(), is(true));
            if (isAliasSupport()) {
                assertThat(rs.getInt("orders_count"), is(40));
            }
            assertThat(rs.getInt(1), is(40));
            assertThat(rs.next(), is(false));
        }
    }
    
    @Test
    public void assertSelectSumAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectSum.xml", getShardingDataSource().getConnection(), "t_order", getDatabaseTestSQL().getSelectSumAliasSql());
    }
    
    @Test
    public void assertSelectSum() throws SQLException {
        try (Connection conn = getShardingDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectSumAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next(), is(true));
            if (isAliasSupport()) {
                assertThat(rs.getLong("user_id_sum"), is(780L));
            }
            assertThat(rs.getLong(1), is(780L));
            assertThat(rs.next(), is(false));
        }
    }
    
    @Test
    public void assertSelectMaxAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectMax.xml", getShardingDataSource().getConnection(), "t_order", getDatabaseTestSQL().getSelectMaxAliasSql());
    }
    
    @Test
    public void assertSelectMax() throws SQLException {
        try (Connection conn = getShardingDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectMaxAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next(), is(true));
            if (isAliasSupport()) {
                assertThat(rs.getDouble("max_user_id"), is(29D));
            }
            assertThat(rs.getDouble(1), is(29D));
            assertThat(rs.next(), is(false));
        }
    }
    
    @Test
    public void assertSelectMinAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectMin.xml", getShardingDataSource().getConnection(), "t_order", getDatabaseTestSQL().getSelectMinAliasSql());
    }
    
    @Test
    public void assertSelectMin() throws SQLException {
        try (Connection conn = getShardingDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectMinAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next(), is(true));
            if (isAliasSupport()) {
                assertThat(rs.getFloat("mIN_user_id"), is(10F));
            }
            assertThat(rs.getFloat(1), is(10F));
            assertThat(rs.next(), is(false));
        }
    }
    
    @Test
    // TODO 改名 avg SHARDING_GEN_2 SHARDING_GEN_3
    public void assertSelectAvgAlias() throws SQLException, DatabaseUnitException {
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectAvg.xml", getShardingDataSource().getConnection(), 
                "t_order", getDatabaseTestSQL().getSelectAvgAliasSql());
    }
    
    @Test
    public void assertSelectAvgByName() throws SQLException {
        try (Connection conn = getShardingDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(getDatabaseTestSQL().getSelectAvgAliasSql());
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next(), is(true));
            if (isAliasSupport()) {
                assertThat(rs.getObject("user_id_avg"), Is.<Object>is(new BigDecimal("19.5000")));
            }
            assertThat(rs.getBigDecimal(1), Is.<Object>is(new BigDecimal("19.5000")));
            assertThat(rs.next(), is(false));
        }
    }
    
    @Test
    public void assertSelectCountWithBindingTable() throws SQLException, DatabaseUnitException {
        String selectSql = SqlPlaceholderUtil.replacePreparedStatement(getDatabaseTestSQL().getSelectCountWithBindingTableSql());
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_0.xml", 
                getShardingDataSource().getConnection(), "t_order_item", selectSql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_1.xml", 
                getShardingDataSource().getConnection(), "t_order_item", selectSql, 1, 9, 1000, 1909);
    }
    
    @Test
    public void assertSelectCountWithBindingTableAndWithoutJoinSql() throws SQLException, DatabaseUnitException {
        String selectSql = SqlPlaceholderUtil.replacePreparedStatement(getDatabaseTestSQL().getSelectCountWithBindingTableAndWithoutJoinSql());
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_0.xml",
                getShardingDataSource().getConnection(), "t_order_item", selectSql, 10, 19, 1000, 1909);
        assertDataSet("integrate/dataset/db/expect/select_aggregate/SelectCountWithBindingTable_1.xml",
                getShardingDataSource().getConnection(), "t_order_item", selectSql, 1, 9, 1000, 1909);
    }
}
