/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.config.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import io.shardingjdbc.dbtest.config.bean.parsecontext.ParseContexDefinition;
import lombok.Getter;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class AssertDefinition {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "init-data-file")
    private String initDataFile;

    @XmlAttribute(name = "expected-data-file")
    private String expectedDataFile;

    @XmlAttribute(name = "expected-update")
    private Integer expectedUpdate;
    
    @XmlElement(name = "sql")
    private String sql;
    
    @XmlElement(name = "expected-sql")
    private String expectedSql;

    @XmlElement(name = "parameters")
    private ParametersDefinition parameters;

    @XmlElement(name = "parse-context")
    private ParseContexDefinition parseContex;

}
