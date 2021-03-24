package org.apache.shardingsphere.infra.executor.exec;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class BaseExecutorTest {
    
    private static final YamlRuleConfigurationSwapperEngine SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final Map<String, DataSource> ACTUAL_DATA_SOURCES = new HashMap<>();
    
    private static final String INIT_CALCITE_DATABASE_0 = "h2/jdbc_init_calcite_0.sql";
    
    private static final String INIT_CALCITE_DATABASE_1 = "h2/jdbc_init_calcite_1.sql";
    
    private static final String INIT_CALCITE_DATABASE_2 = "h2/jdbc_init_calcite_1.sql";
    
    private static Collection<ShardingSphereRule> rules;
    
    protected static ShardingSphereSchema schema;
    
    @BeforeClass
    public static synchronized void initializeDataSource() throws SQLException, IOException {
        createDataSources("ds_0", INIT_CALCITE_DATABASE_0);
        createDataSources("ds_1", INIT_CALCITE_DATABASE_1);
        createDataSources("ds_2", INIT_CALCITE_DATABASE_2);
        initSchema();
        initTable();
        initRule();
    }
    
    public static void initTable() throws SQLException {
        Map<String, DataSource> dataSourceMap = ACTUAL_DATA_SOURCES;
        Connection database0 = dataSourceMap.get("ds_0").getConnection();
        Connection database1 = dataSourceMap.get("ds_1").getConnection();
        Connection database2 = dataSourceMap.get("ds_2").getConnection();
        RunScript.execute(database0, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream("h2/calcite_data_0.sql"))));
        RunScript.execute(database1, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream("h2/calcite_data_1.sql"))));
        RunScript.execute(database2, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream("h2/calcite_data_2.sql"))));
    }
    
    public static void initRule() throws IOException {
        File yamlFile = new File(BaseExecutorTest.class.getResource("/config/sharding-databases-tables.yaml").getFile());
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlFile, YamlRootRuleConfigurations.class);
        Collection<RuleConfiguration> ruleConfigs = SWAPPER_ENGINE.swapToRuleConfigurations(configurations.getRules());
        rules = ShardingSphereRulesBuilder.build(ruleConfigs, new H2DatabaseType(), ACTUAL_DATA_SOURCES, "logic_db");
    }
    
    private static void createDataSources(final String dataSourceName, final String initSql) throws SQLException {
        ACTUAL_DATA_SOURCES.put(dataSourceName, build(dataSourceName));
        initializeSchema(dataSourceName, initSql);
    }
    
    private static void initializeSchema(final String dataSourceName, final String initSql) throws SQLException {
        try (Connection conn = ACTUAL_DATA_SOURCES.get(dataSourceName).getConnection()) {
            RunScript.execute(conn, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream(initSql))));
        }
    }
    
    protected static void initSchema() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData(
                Arrays.asList(new ColumnMetaData("order_id", Types.INTEGER, true, false, false),
                        new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                        new ColumnMetaData("status", Types.VARCHAR, false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("order_item_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("order_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("status", Types.VARCHAR, false, false, false),
                new ColumnMetaData("c_date", Types.TIMESTAMP, false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_user", new TableMetaData(Arrays.asList(new ColumnMetaData("user_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("user_name", Types.VARCHAR, false, false, false)), Collections.emptySet()));
        schema = new ShardingSphereSchema(tableMetaDataMap);
    }
    
    protected static Map<String, DataSource> getActualDataSources() {
        return ACTUAL_DATA_SOURCES;
    }
    
    /**
     * Build data source.
     *
     * @param dataSourceName data source name
     * @return data source
     */
    public static DataSource build(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMaxTotal(50);
        return result;
    }
}
