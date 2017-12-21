package io.shardingjdbc.core.parsing.parser.sql;

import io.shardingjdbc.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangqisen on 2017/12/20.
 */
public class OrStatementParseTest {

    // CHECKSTYLE:OFF
    @Test
    public void testOrStatementParse() throws SQLException {
        // CHECKSTYLE:ON
        DataSource dataSource = getShardingDataSource();
        testSelect(dataSource);
    }

    private static void testSelect(final DataSource dataSource) throws SQLException{
        String sql="SELECT * FROM t_order WHERE user_id IN(?,?,?) OR id=5 ORDER BY id limit 0,10";
//        String sql="SELECT * FROM t_order WHERE user_id IN(?,?,?) OR order_id='5' ORDER BY id";
        try(
                Connection conn=dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql)){
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 1001);
            preparedStatement.setInt(3,1);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                int rsSize=0;
                while (rs.next()) {
                    System.out.println(rs.getInt(1) +","
                            + rs.getInt(2)
                            +","+rs.getInt(3));
                    rsSize++;
                }
                Assert.assertNotEquals(0,rsSize);
            }

        }
    }

    private static ShardingDataSource getShardingDataSource() throws SQLException{
        final ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_0.t_order_0,ds_0.t_order_1,ds_1.t_order_0,ds_1.t_order_1");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id,id", TestComplexKeysShardingAlgorithm.class.getName()));
        tableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id,id", TestComplexKeysShardingAlgorithm.class.getName()));
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        return new ShardingDataSource(shardingRuleConfig.build(dataSourceMap));
    }

    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }

    private static DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
