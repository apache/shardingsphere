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

package org.apache.shardingsphere.test.e2e.discovery.command;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.e2e.discovery.pojo.DistSQLCommandPOJO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Setter
@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DiscoveryDistSQLCommand {
    
    @XmlElement(name = "create-database")
    @Getter
    private DistSQLCommandPOJO createDatabase;
    
    @XmlElement(name = "register-storage-unit")
    @Getter
    private DistSQLCommandPOJO registerStorageUnits;
    
    @XmlElement(name = "create-discovery-rule")
    @Getter
    private DistSQLCommandPOJO createDiscoveryRule;
    
    @XmlElement(name = "create-readwrite-splitting-rule")
    @Getter
    private DistSQLCommandPOJO createReadwriteSplittingRule;
    
    @XmlElement(name = "drop-database")
    @Getter
    private DistSQLCommandPOJO dropDatabase;
    
    @XmlElement(name = "create-readwrite-splitting-database")
    @Getter
    private DistSQLCommandPOJO createReadwriteSplittingDatabase;
    
    @XmlElement(name = "register-single-storage-unit")
    @Getter
    private DistSQLCommandPOJO registerSingleStorageUnit;
}
