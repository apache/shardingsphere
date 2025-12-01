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

package org.apache.shardingsphere.test.e2e.sql.cases.casse;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion.SQLE2ETestCaseAssertion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * JAXB definition of SQL E2E test case.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class SQLE2ETestCase {
    
    @XmlAttribute
    private String sql;
    
    @XmlAttribute(name = "db-types")
    private String dbTypes;
    
    @XmlAttribute(name = "scenario-types")
    private String scenarioTypes;
    
    @XmlAttribute(name = "scenario-comments")
    private String scenarioComments;
    
    @XmlAttribute
    private String adapters;
    
    @XmlAttribute(name = "delay-assertion-seconds")
    private int delayAssertionSeconds;
    
    @XmlAttribute
    private boolean smoke;
    
    @XmlAttribute(name = "skip-batch")
    private boolean skipBatch;
    
    @XmlElement(name = "assertion")
    private Collection<SQLE2ETestCaseAssertion> assertions = new LinkedList<>();
}
