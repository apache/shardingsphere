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

package io.shardingsphere.dbtest.config.bean;

import com.google.common.base.Joiner;
import io.shardingsphere.core.constant.DatabaseType;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Integrate test case.
 * 
 * @author zhangliang 
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class IntegrateTestCase {
    
    @XmlAttribute(name = "sql-case-id")
    private String sqlCaseId;
    
    @XmlAttribute(name = "database-types")
    private String databaseTypes = Joiner.on(",").join(DatabaseType.values());
    
    @Setter
    private String path;
    
    /**
     * Get sub asserts.
     * 
     * @return sub asserts
     */
    public abstract List<? extends SubAssert> getSubAsserts();
    
    /**
     * Get sharding rule types.
     * 
     * @return sharding rule types
     */
    public final Set<String> getShardingRuleTypes() {
        Set<String> result = new HashSet<>();
        for (SubAssert each : getSubAsserts()) {
            result.add(each.getShardingRuleType());
        }
        return result;
    }
}
