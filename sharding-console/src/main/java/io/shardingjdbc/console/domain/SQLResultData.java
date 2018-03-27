package io.shardingjdbc.console.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Result information.
 * 
 * @author zhangyonglun
 */
@AllArgsConstructor
@Getter
@Setter
public class SQLResultData {
    
    private String affectedRows;
    
    private Long durationMilliseconds;
    
    private String sql;
    
    private List<SQLColumnInformation> sqlColumnInformationList;
    
    private List<SQLRowData> sqlRowDataList;
}
