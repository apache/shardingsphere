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

package org.apache.shardingsphere.mask.rule.attribute;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;

import java.util.Collection;
import java.util.Collections;

/**
 * Mask table mapper rule attribute.
 */
public final class MaskTableMapperRuleAttribute implements TableMapperRuleAttribute {
    
    private final Collection<String> logicalTableNames;
    
    public MaskTableMapperRuleAttribute(final Collection<String> maskTableNames) {
        logicalTableNames = new CaseInsensitiveSet<>(maskTableNames);
    }
    
    @Override
    public Collection<String> getLogicTableNames() {
        return logicalTableNames;
    }
    
    @Override
    public Collection<String> getDistributedTableNames() {
        return Collections.emptySet();
    }
    
    @Override
    public Collection<String> getEnhancedTableNames() {
        return Collections.emptySet();
    }
}
