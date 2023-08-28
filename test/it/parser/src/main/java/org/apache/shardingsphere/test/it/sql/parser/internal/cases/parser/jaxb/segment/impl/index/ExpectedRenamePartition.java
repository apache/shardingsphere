package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index;

import javax.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;

@Getter
@Setter
public class ExpectedRenamePartition extends AbstractExpectedSQLSegment {

    @XmlElement(name = "old-partition")
    private ExpectedPartition oldPartition;

    @XmlElement(name = "new-partition")
    private ExpectedPartition newPartition;
}
