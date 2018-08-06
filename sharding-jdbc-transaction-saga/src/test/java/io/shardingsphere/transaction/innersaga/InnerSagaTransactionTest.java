package io.shardingsphere.transaction.innersaga;

import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.algorithm.sharding.PreciseShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.standard.PreciseShardingAlgorithm;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingsphere.transaction.innersaga.api.SagaSoftTransaction;
import io.shardingsphere.transaction.innersaga.api.SagaSoftTransactionManager;
import io.shardingsphere.transaction.innersaga.api.config.SagaSoftTransactionConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ${DESCRIPTION}
 *
 * @author yangyi
 * @create 2018-08-03 下午2:21
 */

public class InnerSagaTransactionTest {

    public static void main(String[] args) throws Exception {
        DataSource dataSource = getShardingDataSource();
        dropTable(dataSource);
        System.out.println("drop table ok!");
        createTable(dataSource);
        System.out.println("create table ok!");
        insert(dataSource);
        System.out.println("insert complete!");
        updateFailure(dataSource);
        System.out.println("update complete!");
    }

    private static DataSource getShardingDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_order");
        orderTableRuleConfig.setActualDataNodes("ds_trans_${0..1}.t_order_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
        orderItemTableRuleConfig.setLogicTable("t_order_item");
        orderItemTableRuleConfig.setActualDataNodes("ds_trans_${0..1}.t_order_item_${0..1}");
        shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);

        shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");

        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("user_id", new ModuloShardingAlgorithm()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", new ModuloShardingAlgorithm()));
        return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }

    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_trans_0", createDataSource("ds_trans_0"));
        result.put("ds_trans_1", createDataSource("ds_trans_1"));
        return result;
    }

    private static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("school1020");
        return result;
    }

    private static SagaSoftTransaction getSagaSoftTransaction(DataSource dataSource) {
        SagaSoftTransactionConfiguration sagaSoftTransactionConfiguration = new SagaSoftTransactionConfiguration(dataSource);
        SagaSoftTransactionManager sagaSoftTransactionManager = new SagaSoftTransactionManager(sagaSoftTransactionConfiguration);
        sagaSoftTransactionManager.init();
        return sagaSoftTransactionManager.getTransaction();
    }

    private static void createTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
        executeUpdate(dataSource, "CREATE TABLE IF NOT EXISTS t_order_item (item_id INT NOT NULL, order_id INT NOT NULL, user_id INT NOT NULL, PRIMARY KEY (item_id))");
    }

    private static void dropTable(final DataSource dataSource) throws SQLException {
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order_item");
        executeUpdate(dataSource, "DROP TABLE IF EXISTS t_order");
    }

    private static void executeUpdate(final DataSource dataSource, final String sql) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    private static void insert(final DataSource dataSource) throws Exception {
        try {
            for (int i = 0; i < 100; i++) {
                String sql = String.format("INSERT INTO t_order VALUES (%s, %s, 'INIT');", 1000 + i, i);
                executeUpdate(dataSource, sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void updateFailure(final DataSource dataSource) throws Exception {
        String sql1 = "UPDATE t_order SET status='UPDATE_1' WHERE user_id=0 AND order_id=1000";
        String sql2 = "UPDATE t_order SET not_existed_column=1 WHERE user_id=1 AND order_id=?";
        String sql3 = "UPDATE t_order SET status='UPDATE_2' WHERE user_id=0 AND order_id=1000";
        SagaSoftTransaction userTransaction = getSagaSoftTransaction(dataSource);
        userTransaction.begin();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
             PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
             PreparedStatement preparedStatement3 = connection.prepareStatement(sql3)) {
            preparedStatement2.setObject(1, 1000);
            preparedStatement1.executeUpdate();
            preparedStatement2.executeUpdate();
            preparedStatement3.executeUpdate();
            userTransaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            userTransaction.rollback();
        }
    }

    static final class ModuloShardingAlgorithm implements PreciseShardingAlgorithm<Integer> {

        @Override
        public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Integer> shardingValue) {
            for (String each : availableTargetNames) {
                if (each.endsWith(shardingValue.getValue() % 2 + "")) {
                    return each;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
