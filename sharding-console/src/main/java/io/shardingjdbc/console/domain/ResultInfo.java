package io.shardingjdbc.console.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * ResultInfo.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public class ResultInfo {
    private String tip;

    private Long duration;

    private String sql;

    private Map<String, String> types;

    private List<Map<String, String>> data;

    public ResultInfo() {
        tip = "";
        duration = 0L;
        sql = "";
        types = null;
        data = null;
    }
}
