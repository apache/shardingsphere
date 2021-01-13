package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForCalciteTest;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public final class CalcitePrepareStatement extends AbstractShardingSphereDataSourceForCalciteTest {

    private static final String INSERT_SQL = "INSERT INTO t_encrypt (id, cipher_pwd, plain_pwd) VALUES (?, ?, ?)";

    private static final String SELECT_SQL_BY_ID = "SELECT id, cipher_pwd, plain_pwd FROM t_encrypt WHERE id = ?";

    @Before
    public void init() throws SQLException {
        try (PreparedStatement statement = getShardingSphereDataSource().getConnection().prepareStatement(INSERT_SQL)) {
            statement.setInt(1, 99);
            statement.setString(2, "cipher");
            statement.setString(3, "plain");
            statement.execute();
        }
    }

    @Test
    public void assertQueryWithCalciteInSingleTable() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource().getConnection().prepareStatement(SELECT_SQL_BY_ID);
        preparedStatement.setToCalcite(true);
        preparedStatement.setInt(1, 99);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(99));
        assertThat(resultSet.getString(2), is("cipher"));
        assertThat(resultSet.getString(3), is("plain"));
    }

}
