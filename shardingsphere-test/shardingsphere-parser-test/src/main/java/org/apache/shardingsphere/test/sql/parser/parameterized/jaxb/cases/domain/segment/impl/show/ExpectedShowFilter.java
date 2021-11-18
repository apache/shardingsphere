package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.show;

import lombok.Getter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.like.ExpectedLikeClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.where.ExpectedWhereClause;

import javax.xml.bind.annotation.XmlElement;

/**
 * Expected show filter segment.
 */
@Getter
public final class ExpectedShowFilter extends AbstractExpectedSQLSegment {
    
    @XmlElement(name = "like")
    private ExpectedLikeClause like;
    
    @XmlElement(name = "where")
    private ExpectedWhereClause where;
}
