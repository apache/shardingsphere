/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.cases.assertion.root;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;

/**
 * JAXB definition of integrate test case.
 * 
 * @author zhangliang 
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class IntegrateTestCase {
    
    @XmlAttribute(name = "sql-case-id")
    private String sqlCaseId;
    
    @Setter
    private String path;
    
    /**
     * Get integrate test case assertions.
     * 
     * @return integrate test case assertions
     */
    public abstract List<? extends IntegrateTestCaseAssertion> getIntegrateTestCaseAssertions();
}
