package com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Data source with name.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class NamedDataSource {
    
    private final String name;
    
    private final DataSource dataSource;
    
    /**
     * Transfer to map.
     * 
     * <p>Key is data source name, value is data source.</p>
     * 
     * @return map
     */
    public Map<String, DataSource> toMap() {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put(name, dataSource);
        return result;
    }
}
