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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.AbstractExpectedIdentifierSQLSegment;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

/**
 * Expected shadow rule.
 */
@Getter
@Setter
public final class ExpectedShadowRule extends AbstractExpectedIdentifierSQLSegment {
    
    @XmlAttribute(name = "rule-name")
    private String ruleName;
    
    @XmlAttribute(name = "source")
    private String source;
    
    @XmlAttribute(name = "shadow")
    private String shadow;
    
    @XmlElement(name = "table-rule")
    private Collection<ExpectedShadowTableRule> shadowTableRules;
    
    /**
     * Expected shadow table rule.
     */
    @Setter
    @Getter
    public static class ExpectedShadowTableRule extends AbstractExpectedIdentifierSQLSegment {
        
        @XmlAttribute(name = "table-name")
        private String tableName;
        
        @XmlElement(name = "shadow-algorithm")
        private Collection<ExpectedShadowAlgorithm> algorithms;
    }
}
