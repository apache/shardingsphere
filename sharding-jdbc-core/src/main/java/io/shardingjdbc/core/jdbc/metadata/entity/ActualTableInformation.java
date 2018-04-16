package io.shardingjdbc.core.jdbc.metadata.entity;

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
    
    private final String dataSourceName;
    
    private final String tableName;
    
    private final TableStructure tableStructure;
}
