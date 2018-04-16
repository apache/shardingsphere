package io.shardingjdbc.core.jdbc.registor.entity;

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
public final class TableStructure {
    
    private List<ColumnInformation> columnInformations;
    
    public TableStructure() {
        columnInformations = new ArrayList<ColumnInformation>(30);
    }
    
    public final int getColumnAmount() {
        return columnInformations.size();
    }
}
