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

package org.apache.shardingsphere.test.it.rewrite.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Rewrite assertion entity for JAXB.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class RewriteAssertionEntity {
    
    @XmlAttribute(required = true)
    private String id;
    
    @XmlElement(required = true)
    private RewriteInputEntity input;
    
    @XmlElement(required = true, name = "output")
    private List<RewriteOutputEntity> outputs;
    
    @XmlAttribute(name = "db-types")
    private String databaseTypes;
}
