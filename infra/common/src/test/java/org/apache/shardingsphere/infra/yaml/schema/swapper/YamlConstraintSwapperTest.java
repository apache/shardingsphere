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

package org.apache.shardingsphere.infra.yaml.schema.swapper;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereConstraint;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class YamlConstraintSwapperTest {
    
    private final YamlConstraintSwapper swapper = new YamlConstraintSwapper();
    
    @Test
    void assertSwapToYamlConfiguration() {
        ShardingSphereConstraint constraint = new ShardingSphereConstraint("fk_user", "t_user");
        YamlShardingSphereConstraint actual = swapper.swapToYamlConfiguration(constraint);
        assertThat(actual.getName(), is("fk_user"));
        assertThat(actual.getReferencedTableName(), is("t_user"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlShardingSphereConstraint yamlConstraint = new YamlShardingSphereConstraint();
        yamlConstraint.setName("fk_order");
        yamlConstraint.setReferencedTableName("t_order");
        ShardingSphereConstraint actual = swapper.swapToObject(yamlConstraint);
        assertThat(actual.getName(), is("fk_order"));
        assertThat(actual.getReferencedTableName(), is("t_order"));
    }
}
