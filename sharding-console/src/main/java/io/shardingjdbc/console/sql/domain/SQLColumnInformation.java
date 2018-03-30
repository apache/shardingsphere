package io.shardingjdbc.console.sql.domain;

import com.google.common.base.Objects;
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
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }
        SQLColumnInformation sqlColumnInformation = (SQLColumnInformation) obj;
        return Objects.equal(this.columnLabel.toUpperCase(), sqlColumnInformation.columnLabel.toUpperCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(columnLabel.toUpperCase());
    }
}
