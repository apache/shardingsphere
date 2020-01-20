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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.projection;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.generic.AbstractExpectedSegment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class ExpectedTopProjection extends AbstractExpectedSegment implements ExpectedProjection {
    
    @XmlAttribute(name = "top-value")
    private Long topValue;
    
    @XmlAttribute(name = "top-parameter-index")
    private Integer topParameterIndex;
    
    @XmlAttribute(name = "top-value-start-index")
    private int topValueStartIndex;
    
    @XmlAttribute(name = "top-value-stop-index")
    private int topValueStopIndex;
    
    @XmlAttribute(name = "alias")
    private String alias;
}
