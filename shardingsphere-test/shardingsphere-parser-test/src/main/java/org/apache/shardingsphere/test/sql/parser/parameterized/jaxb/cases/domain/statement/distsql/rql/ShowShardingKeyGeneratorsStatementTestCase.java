package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.schema.ExpectedSchema;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
public final class ShowShardingKeyGeneratorsStatementTestCase extends SQLParserTestCase {

    @XmlElement
    private ExpectedSchema schema;

}
