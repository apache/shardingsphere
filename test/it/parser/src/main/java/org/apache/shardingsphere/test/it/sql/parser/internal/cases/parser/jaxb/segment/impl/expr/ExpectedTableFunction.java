package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Expected table function
 */
@Getter
@Setter
public final class ExpectedTableFunction extends AbstractExpectedSQLSegment implements ExpectedExpressionSegment {
    
    @XmlAttribute(name = "function-name")
    private String functionName;
    
    @XmlAttribute
    private String text;
}

