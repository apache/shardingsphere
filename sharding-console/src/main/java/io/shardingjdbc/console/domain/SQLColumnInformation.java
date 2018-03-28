package io.shardingjdbc.console.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * SQL column information.
 *
 * @author zhangyonglun
 */
@AllArgsConstructor
@Getter
@Setter
public final class SQLColumnInformation {
    
    private String columnLabel;
    
    private String columnTypeName;
    
    private int columnSize;
}
