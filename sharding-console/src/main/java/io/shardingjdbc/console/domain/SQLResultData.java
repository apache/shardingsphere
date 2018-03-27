package io.shardingjdbc.console.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Result information.
 * 
 * @author zhangyonglun
 */
@AllArgsConstructor
@Getter
@Setter
public final class SQLResultData {
    
    private Integer affectedRows;
    
    private Long durationMilliseconds;
    
    private String sql;
    
    private List<SQLColumnInformation> sqlColumnInformationList;
    
    private List<SQLRowData> sqlRowDataList;
}
