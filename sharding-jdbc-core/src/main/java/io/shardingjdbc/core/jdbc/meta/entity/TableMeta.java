package io.shardingjdbc.core.jdbc.meta.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

/**
 * The table structure.
 *
 * @author panjuan
 */
@Getter
@EqualsAndHashCode
public final class TableMeta {
    
    private List<ColumnMeta> columnMetas;
    
    public TableMeta() {
        columnMetas = new ArrayList<ColumnMeta>(30);
    }
    
    public final int getColumnAmount() {
        return columnMetas.size();
    }
}
