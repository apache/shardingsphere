package io.shardingjdbc.dbtest.data;

import lombok.Data;

@Data
public class ColumnDefinition {
    
    private String name;
    
    private String type;
    
    private Integer size;
    
    private Integer decimalDigits;
    
    private Integer numPrecRadix;
    
    private Integer nullAble;
    
    private int isAutoincrement;
    
}
