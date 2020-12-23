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

package org.apache.shardingsphere.dbtest.cases.assertion.root;

import com.google.common.base.Splitter;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * JAXB definition of integrate test case assertion.
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class IntegrateTestCaseAssertion {
    
    @XmlAttribute(name = "expected-data-file")
    private String expectedDataFile;
    
    @XmlAttribute
    private String parameters;
    
    /**
     * Get SQL values.
     * 
     * @return SQL values
     * @throws ParseException parse exception
     */
    public final Collection<SQLValue> getSQLValues() throws ParseException {
        if (null == parameters) {
            return Collections.emptyList();
        }
        Collection<SQLValue> result = new LinkedList<>();
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(parameters)) {
            // TODO improve the implement way
            if (each.startsWith("'")) {
                String value = each.substring(each.indexOf('\'') + 1, each.lastIndexOf('\''));
                result.add(new SQLValue(value, "json", ++count));
                continue;
            }
            List<String> parameterPair = Splitter.on(":").trimResults().splitToList(each);
            result.add(new SQLValue(parameterPair.get(0), parameterPair.get(1), ++count));
        }
        return result;
    }
    
    @Override
    public final String toString() {
        return expectedDataFile;
    }
}
