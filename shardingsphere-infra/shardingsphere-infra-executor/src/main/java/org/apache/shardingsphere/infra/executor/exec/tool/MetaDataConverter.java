package org.apache.shardingsphere.infra.executor.exec.tool;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.ResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;

import java.util.List;
import java.util.stream.Collectors;

public class MetaDataConverter {
    
    public static List<ColumnMetaData> convertFrom(RelDataType relDataType) {
        return relDataType.getFieldList().stream().map(relDataTypeField -> {
            String columnName = relDataTypeField.getName();
            RelDataType fieldDataType = relDataTypeField.getType();
            int dataType = fieldDataType.getSqlTypeName().getJdbcOrdinal();
            String dataTypeName = fieldDataType.getSqlTypeName().getName();
            return new ColumnMetaData(columnName, dataType, dataTypeName, false, false, false);
        }).collect(Collectors.toList());
    }
    
    public static QueryResultMetaData buildMetaData(RelNode relNode) {
        return new ResultColumnMetaData(convertFrom(relNode.getRowType()));
    }
}
