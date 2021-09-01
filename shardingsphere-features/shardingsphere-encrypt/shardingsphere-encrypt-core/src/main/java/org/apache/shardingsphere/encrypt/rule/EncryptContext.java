package org.apache.shardingsphere.encrypt.rule;

import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import com.google.common.collect.ImmutableMap;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public final class EncryptContext {
    
    private Optional<EncryptTable> encryptTable;
    
    private SimpleTableSegment simpleTableSegment;
    
    private String column;
    
    public Map<String, String> of(){
        String dataSourceName = encryptTable.isPresent() ? encryptTable.get().getDataSourceName() : "";
        String owner = simpleTableSegment.getOwner().isPresent()?simpleTableSegment.getOwner().get().getIdentifier().getValue():"";
        String tableName = simpleTableSegment.getTableName().getIdentifier().getValue();
        return ImmutableMap.of("dataSource", dataSourceName, "owner", owner, "table", tableName, "column", column);
    }

}