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

package org.apache.shardingsphere.test.it.distsql.handler.engine.query.jaxb;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedList;
import java.util.List;

/**
 * DistSQL rule query executor test case.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public final class DistSQLRuleQueryExecutorTestCase {
    
    @XmlAttribute(name = "dist-sql")
    private String distSQL;
    
    @XmlAttribute(name = "current-rule-config-yaml-file")
    private String currentRuleConfigurationYAMLFile;
    
    @XmlElementWrapper(name = "expected-query-result-rows")
    @XmlElement(name = "expected-query-result-row")
    private final List<String> expectedQueryResultRows = new LinkedList<>();
}
