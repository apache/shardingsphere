package org.apache.shardingsphere.infra.optimize.context;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.CalciteLogicSchema;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CalciteContextFactoryTest {

    @Test
    public void createTest() {
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>();
        ShardingSphereResource shardingSphereResource = new ShardingSphereResource(null, null, null, new H2DatabaseType());
        TableMetaData tableMetaData = new TableMetaData();
        ShardingSphereSchema schema = new ShardingSphereSchema();
        schema.put("tab_user", tableMetaData);
        ShardingSphereRuleMetaData metaData = new ShardingSphereRuleMetaData(new ArrayList<>(), new ArrayList<>());
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData("logic_db", shardingSphereResource, metaData, schema);
        metaDataMap.put("logic_db", shardingSphereMetaData);
        CalciteContextFactory calciteContextFactory = new CalciteContextFactory(metaDataMap);
        assertNotNull(calciteContextFactory);
        CalciteContext logicDb = calciteContextFactory.create("logic_db", new CalciteRowExecutor(new ArrayList<>(), 0, null, new JDBCExecutor(null, true), null, null));
        assertNotNull(logicDb);
        Properties properties = logicDb.getConnectionProperties();
        assertNotNull(properties);
        assertThat(properties.getProperty("lex"), is("MYSQL"));
        assertThat(properties.getProperty("conformance"), is("DEFAULT"));
        CalciteLogicSchema calciteLogicSchema = logicDb.getCalciteLogicSchema();
        assertNotNull(calciteLogicSchema);
        assertThat(calciteLogicSchema.getName(), is("logic_db"));
    }
    
}
