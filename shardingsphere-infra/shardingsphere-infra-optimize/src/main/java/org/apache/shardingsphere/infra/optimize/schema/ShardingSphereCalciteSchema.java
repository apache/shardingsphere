package org.apache.shardingsphere.infra.optimize.schema;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.schema.table.ShardingSphereCalciteTable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * calcite schema for optimizing sql using calcite optimizing framework
 */
public class ShardingSphereCalciteSchema extends AbstractSchema {

    private final ShardingSphereSchema schema;

    public ShardingSphereCalciteSchema(ShardingSphereSchema schema) {
        this.schema = schema;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        Collection<String> tableNames = schema.getAllTableNames();
        return tableNames.stream().map(tableName -> new ShardingSphereCalciteTable(tableName, schema.get(tableName)))
                .collect(Collectors.toMap(ShardingSphereCalciteTable::getTableName, Function.identity()));
    }
}