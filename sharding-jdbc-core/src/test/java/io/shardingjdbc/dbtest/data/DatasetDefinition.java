package io.shardingjdbc.dbtest.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DatasetDefinition {

    /**
     * Map<table,Map<column,type>>
     */
    private Map<String, Map<String, String>> configs = new HashMap<>();

    /**
     * Map<table,List<Map<column,data>>>
     */
    private Map<String, List<Map<String, String>>> datas = new HashMap<>();

}
