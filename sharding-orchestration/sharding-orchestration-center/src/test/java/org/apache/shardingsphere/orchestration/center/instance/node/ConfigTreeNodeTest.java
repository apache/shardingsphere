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

package org.apache.shardingsphere.orchestration.center.instance.node;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class ConfigTreeNodeTest {
    
    private final Set<String> instanceKeys = Sets.newHashSet();
    
    private final ConfigTreeNode root = new ConfigTreeNode(null, "/", Sets.<ConfigTreeNode>newHashSet());
    
    @Before
    public void setUp() {
        instanceKeys.add("test.children.1");
        instanceKeys.add("test.children.2");
        instanceKeys.add("test1.children.3");
        root.initTree(instanceKeys, ".");
    }
    
    @Test
    public void assertInitTree() {
        assertThat(root.getChildrenNodes().size(), is(2));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        assertThat(root.getChildrenKeys("/test/children").size(), is(2));
        assertEquals(root.getChildrenKeys("/test1/children"), Sets.<String>newHashSet("/test1/children/3"));
    }
    
    @Test
    public void assertRefresh() {
        root.refresh("test1.children1.1", ".");
        assertThat(root.getChildrenKeys("/test1").size(), is(2));
        assertEquals(root.getChildrenKeys("/test1/children1"), Sets.<String>newHashSet("/test1/children1/1"));
    }
}
