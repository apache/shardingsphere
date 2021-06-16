/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Expected table rule.
 */
@Getter
@Setter
public final class ExpectedTableRule extends SQLParserTestCase {
    
    @XmlElement(name = "table")
    private String logicTable;

    @XmlElement(name = "data-source")
    private List<String> dataSources;

    @XmlElement(name = "table-strategy-column")
    private String tableStrategyColumn;

    @XmlElement(name = "key-generate-strategy-column")
    private String keyGenerateStrategyColumn;

    @XmlElement(name = "table-strategy")
    private ExpectedFunction tableStrategy;

    @XmlElement(name = "key-generate-strategy")
    private ExpectedFunction keyGenerateStrategy;
}
