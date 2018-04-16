package io.shardingjdbc.core.jdbc.meta.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The list of actual table information.
 *
 * @author panjuan
 */
@NoArgsConstructor
@Getter
public class ActualTableMetaList {
    
    private List<ActualTableMeta> actualTableMetaList;
    
    public boolean isAllTableStructuresSame() {
        final List<TableMeta> tableMetaList = new ArrayList<>();
        for(ActualTableMeta each : actualTableMetaList) {
            tableMetaList.add(each.getTableMeta());
        }
        
        final Set<TableMeta> tableMetaSet = new HashSet<>(tableMetaList);
        return tableMetaSet.size() == 1;
    }
}
