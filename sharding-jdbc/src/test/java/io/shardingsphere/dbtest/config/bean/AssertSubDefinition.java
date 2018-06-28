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

package io.shardingsphere.dbtest.config.bean;

import lombok.Getter;

import javax.xml.bind.annotation.*;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class AssertSubDefinition {
    
    @XmlAttribute(name = "sharding-rule-types")
    private String shardingRuleTypes;
    
    @XmlAttribute(name = "expected-data-file")
    private String expectedDataFile;
    
    @XmlAttribute(name = "database-types")
    private String databaseTypes = "H2,MySQL,Oracle,SQLServer,PostgreSQL";
    
    @XmlElement(name = "parameter")
    private ParameterDefinition parameter;
    
    @XmlElement(name = "expected-parameter")
    private ParameterDefinition expectedParameter;
    
    @XmlAttribute(name = "expected-update")
    private Integer expectedUpdate;
}
