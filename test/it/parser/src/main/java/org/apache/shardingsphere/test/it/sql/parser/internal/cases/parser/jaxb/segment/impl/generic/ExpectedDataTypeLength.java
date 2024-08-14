package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.generic;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedBaseSimpleExpression;

@Getter
@Setter
public class ExpectedDataTypeLength extends ExpectedBaseSimpleExpression {
    
    private int precision;
    
    private int scale;
    
    private String type;
}
