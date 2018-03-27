package io.shardingjdbc.console.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SQL row data.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public class SQLRowData {
    
    private Map<String, String> rowData;
    
    public SQLRowData() {
        rowData = new LinkedHashMap<>();
    }
}
