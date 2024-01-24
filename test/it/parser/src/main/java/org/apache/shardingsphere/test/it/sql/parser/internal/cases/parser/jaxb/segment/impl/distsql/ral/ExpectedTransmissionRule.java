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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ral;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedAlgorithm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Expected transmission rule.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedTransmissionRule {
    
    @XmlElement(name = "read")
    private ExpectedRead read;
    
    @XmlElement(name = "write")
    private ExpectedWrite write;
    
    @XmlElement(name = "stream-channel")
    private ExpectedAlgorithm streamChannel;
}
