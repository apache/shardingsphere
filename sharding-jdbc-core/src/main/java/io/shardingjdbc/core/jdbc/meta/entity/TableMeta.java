package io.shardingjdbc.core.jdbc.meta.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * The table meta information.
 *
 * @author panjuan
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class TableMeta {
    
    private final List<ColumnMeta> columnMetas;
    
    public TableMeta() {
        columnMetas = new ArrayList<>(30);
    }
    
}
