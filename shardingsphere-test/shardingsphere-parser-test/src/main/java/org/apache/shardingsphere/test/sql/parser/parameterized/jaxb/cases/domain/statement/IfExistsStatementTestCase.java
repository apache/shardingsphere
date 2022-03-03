
package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;

@Getter
public abstract class IfExistsStatementTestCase extends SQLParserTestCase{
    
    @XmlAttribute(name = "contains-exists-clause")
    private boolean containsExistClause;
}
