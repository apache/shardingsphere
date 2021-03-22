package org.apache.shardingsphere.infra.optimize.schema;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AbstractSchemaTest {

    protected ShardingSphereSchema buildSchema() {
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
        return new ShardingSphereSchema(tableMetaDataMap);
    }

}