package io.shardingjdbc.example.jdbc.repository;

import io.shardingjdbc.core.api.HintManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RawJdbcRepository {
    
    private final DataSource dataSource;
    
    public RawJdbcRepository(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void testAll() throws SQLException {
        createTable();
        insertData();
        System.out.println("1.Equals Select--------------");
        printEqualsSelect();
        System.out.println("2.In Select--------------");
        printInSelect();
        System.out.println("3.Between Select--------------");
        printBetweenSelect();
        System.out.println("4.Hint Select--------------");
        printHintSimpleSelect();
        dropTable();
    }
    
    public void createTable() throws SQLException {
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id))");
    }
    
    public void dropTable() throws SQLException {
        executeUpdate(dataSource, "DROP TABLE t_order_item");
        executeUpdate(dataSource, "DROP TABLE t_order");
    }
    
    public void insertData() throws SQLException {
        for (int i = 1; i < 10; i++) {
            int orderId = executeUpdate(dataSource, "INSERT INTO t_order (user_id, status) VALUES (10, 'INIT')");
            executeUpdate(dataSource, String.format("INSERT INTO t_order_item (item_id, order_id, user_id) VALUES (%s10, %s, 10)", orderId, orderId));
            orderId = executeUpdate(dataSource, "INSERT INTO t_order (user_id, status) VALUES (11, 'INIT')");
            executeUpdate(dataSource, String.format("INSERT INTO t_order_item (item_id, order_id, user_id) VALUES (%s11, %s, 11)", orderId, orderId));
        }
    }
    
    public void printEqualsSelect() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            printSimpleSelect(preparedStatement);
        }
    }
    
    public void printInSelect() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id in (?, ?)";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 11);
            printSimpleSelect(preparedStatement);
        }
    }
    
    public void printBetweenSelect() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id BETWEEN ? AND ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 11);
            printSimpleSelect(preparedStatement);
        }
    }
    
    public void printHintSimpleSelect() throws SQLException {
        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id";
        try (
                HintManager hintManager = HintManager.getInstance();
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.addDatabaseShardingValue("t_order", "user_id", 11);
            printSimpleSelect(preparedStatement);
        }
    }
    
    private void printSimpleSelect(final PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                System.out.print("item_id:" + rs.getInt(1) + ", ");
                System.out.print("order_id:" + rs.getInt(2) + ", ");
                System.out.print("user_id:" + rs.getInt(3));
                System.out.println();
            }
        }
    }
    
    private int executeUpdate(final DataSource dataSource, final String sql) throws SQLException {
        int result = -1;
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                result = resultSet.getInt(1);
            }
        }
        return result;
    }
}
