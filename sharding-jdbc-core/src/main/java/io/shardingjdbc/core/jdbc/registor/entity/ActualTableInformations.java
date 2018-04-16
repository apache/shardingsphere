package io.shardingjdbc.core.jdbc.registor.entity;

import lombok.AllArgsConstructor;
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
public class ActualTableInformations {
    
    private List<ActualTableInformation> actualTableInformationList;
    
    public boolean isAllTableStructuresSame() {
        final List<TableStructure> tableStructureList = new ArrayList<>();
        for(ActualTableInformation each : actualTableInformationList) {
            tableStructureList.add(each.getTableStructure());
        }
        
        final Set<TableStructure> tableStructureSet = new HashSet<>(tableStructureList);
        return tableStructureSet.size() == 1;
    }
}
