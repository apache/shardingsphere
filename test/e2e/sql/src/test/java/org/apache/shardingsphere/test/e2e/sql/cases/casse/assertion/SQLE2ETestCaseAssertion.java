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

package org.apache.shardingsphere.test.e2e.sql.cases.casse.assertion;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * JAXB definition of SQL E2E test case assertion.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class SQLE2ETestCaseAssertion {
    
    @XmlAttribute(name = "expected-data-source-name")
    private String expectedDataSourceName;
    
    @XmlAttribute(name = "expected-data-file")
    private String expectedDataFile;
    
    @XmlAttribute(name = "expected-generated-key-data-file")
    private String expectedGeneratedKeyDataFile;
    
    @XmlAttribute
    private String parameters;
    
    @XmlElement(name = "initial-sql")
    private SQLE2ETestCaseAssertionSQL initialSQL;
    
    @XmlElement(name = "assertion-sql")
    private SQLE2ETestCaseAssertionSQL assertionSQL;
    
    @XmlElement(name = "destroy-sql")
    private SQLE2ETestCaseAssertionSQL destroySQL;
    
    /**
     * Get SQL values.
     *
     * @return SQL values
     */
    public Collection<SQLValue> getSQLValues() {
        if (null == parameters) {
            return Collections.emptyList();
        }
        Collection<SQLValue> result = new LinkedList<>();
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(parameters)) {
            List<String> parameterPair = parse(each);
            result.add(new SQLValue(parameterPair.get(0), parameterPair.get(1), ++count));
        }
        return result;
    }
    
    private List<String> parse(final String param) {
        List<String> result = Splitter.on(":").trimResults().splitToList(param);
        int size = result.size();
        if (size <= 2) {
            return result;
        }
        return parseComplex(param);
    }
    
    private List<String> parseComplex(final String param) {
        List<String> result = new ArrayList<>(2);
        int index = param.lastIndexOf(":");
        result.add(param.substring(0, index));
        result.add(param.substring(index + 1));
        return result;
    }
}
