package org.apache.shardingsphere.infra.optimize.schema.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.adapter.java.AbstractQueryableTable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptTable.ToRelContext;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ShardingSphereCalciteTable extends AbstractQueryableTable
        implements TranslatableTable {

    private String tableName;

    private TableMetaData tableMetaData;

    public ShardingSphereCalciteTable(String tableName, TableMetaData tableMetaData) {
        super(Object[].class);
        this.tableName = tableName;
        this.tableMetaData = tableMetaData;
    }

    @Override
    public <T> Queryable<T> asQueryable(QueryProvider queryProvider, SchemaPlus schema, String tableName) {
        return null;
    }

    @Override
    public RelNode toRel(ToRelContext context, RelOptTable relOptTable) {
        return LogicalTableScan.create(context.getCluster(), relOptTable, Collections.emptyList());
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory typeFactory) {
        Map<String, ColumnMetaData> map = tableMetaData.getColumns();
        List<Map.Entry<String, RelDataType>> columnDataTypes = new ArrayList<>();
        for(Map.Entry<String, ColumnMetaData> entry : map.entrySet()) {
            ColumnMetaData columnMetaData = entry.getValue();
            SqlTypeName calciteSqlType = SqlTypeName.getNameForJdbcType(columnMetaData.getDataType());
            // TODO other extension type
            if(calciteSqlType == null) {
                throw new RuntimeException("unsupported column type");
            }
            // TODO to be replaced by RelDataTypeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName, int, int)
            RelDataType calciteType = typeFactory.createSqlType(calciteSqlType);
            // TODO to add Collation and nullable typeFactory.createTypeWithNullability()
            columnDataTypes.add(new SimpleEntry<>(entry.getKey(), calciteType));
        }
        
        return typeFactory.createStructType(columnDataTypes);
    }
}
