package org.apache.shardingsphere.sql.parser.sql.statement;

import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.VariableValueSegment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VariableProperty {
    
    private VariableSegment variable;
    
    private VariableValueSegment variableValue;
    
    private String scopeType;
    
}
