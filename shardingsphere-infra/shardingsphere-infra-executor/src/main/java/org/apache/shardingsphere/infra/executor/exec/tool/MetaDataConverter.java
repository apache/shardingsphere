package org.apache.shardingsphere.infra.executor.exec.tool;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.shardingsphere.infra.executor.exec.meta.ResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;

import java.util.List;
import java.util.stream.Collectors;

public final class MetaDataConverter {
    
    /**
     * Convert <code>RelDataType</code> to <code>ColumnMetaData</code>.
     * @param relDataType relDataType
     * @return <code>ColumnMetaData</code> list
     */
    public static List<ColumnMetaData> convertFrom(final RelDataType relDataType) {
        return relDataType.getFieldList().stream().map(relDataTypeField -> {
            String columnName = relDataTypeField.getName();
            RelDataType fieldDataType = relDataTypeField.getType();
            int dataType = fieldDataType.getSqlTypeName().getJdbcOrdinal();
            return new ColumnMetaData(columnName, dataType, false, false, false);
        }).collect(Collectors.toList());
    }
    
    /**
     * Build <code>QueryResultMetaData</code> from <code>RelNode</code>, this function depends 
     * on {@link #convertFrom(RelDataType)}.
     * @param relNode relNode
     * @return <code>QueryResultMetaData</code> 
     */
    public static QueryResultMetaData buildMetaData(final RelNode relNode) {
        return new ResultColumnMetaData(convertFrom(relNode.getRowType()));
    }
}
