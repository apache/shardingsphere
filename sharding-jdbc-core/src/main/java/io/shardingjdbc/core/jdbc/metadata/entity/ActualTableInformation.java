package io.shardingjdbc.core.jdbc.metadata.entity;

import io.shardingjdbc.core.rule.DataNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The actual table information.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public class ActualTableInformation {
    
    private final DataNode dataNode;
    
    private final TableMeta tableMeta;
}
