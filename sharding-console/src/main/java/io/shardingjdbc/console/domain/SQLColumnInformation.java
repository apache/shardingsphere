package io.shardingjdbc.console.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(of = { "columnLabel" })
public final class SQLColumnInformation {
    
    private String columnLabel;
    
    private String columnTypeName;
    
    private int columnSize;
}
