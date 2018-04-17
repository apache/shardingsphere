package io.shardingjdbc.core.jdbc.metadata.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * The table metadata information.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class TableMeta {
    
    private final List<ColumnMeta> columnMetas;
}
