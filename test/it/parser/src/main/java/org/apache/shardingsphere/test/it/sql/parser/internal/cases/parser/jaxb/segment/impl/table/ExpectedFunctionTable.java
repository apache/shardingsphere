package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedDelimiterSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedTableFunction;

import javax.xml.bind.annotation.XmlElement;

/**
 * Expected Function Table
 */
@Getter
@Setter
public final class ExpectedFunctionTable extends AbstractExpectedDelimiterSQLSegment {
    
    @XmlElement(name = "table-function")
    private ExpectedTableFunction tableFunction;
}
