package io.shardingjdbc.dbtest.data;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DatasetDefinition {

    /**
     *  Map<表,Map<列,类型>>
     */
    private Map<String,Map<String,String>> configs = new HashMap<>();


    /**
     *  Map<表,List<Map<列,数据>>>
     */
    private Map<String,List<Map<String,String>>> datas = new HashMap<>();

}
