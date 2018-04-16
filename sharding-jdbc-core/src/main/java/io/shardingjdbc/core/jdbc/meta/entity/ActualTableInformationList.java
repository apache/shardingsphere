package io.shardingjdbc.core.jdbc.meta.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The list of actual table information.
 *
 * @author panjuan
 */

@RequiredArgsConstructor
@Getter
public class ActualTableInformationList {
    
    private final List<ActualTableInformation> actualTableInformationList;
    
    /**
     * To judge whether all meta of tables in actualTableInformationList are same.
     *
     * @return true or false.
     */
    public boolean isAllTableMetaSame() {
        final List<TableMeta> tableMetaList = new ArrayList<>();
        for (ActualTableInformation each : actualTableInformationList) {
            tableMetaList.add(each.getTableMeta());
        }
        
        final Set<TableMeta> tableMetaSet = new HashSet<>(tableMetaList);
        return 1 == tableMetaSet.size();
    }
}
