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

package org.apache.shardingsphere.infra.metadata.schema.builder.util;

import org.junit.Assert;
import org.junit.Test;

public class IndexMetaDataUtilTest {
    
    @Test
    public void assertGetLogicIndexNameWithIndexNameSuffix() {
        String logicIndexName = IndexMetaDataUtil.getLogicIndexName("order_index_t_order", "t_order");
        Assert.assertEquals(logicIndexName, "order_index");
    }
    
    @Test
    public void assertGetLogicIndexNameWithMultiIndexNameSuffix() {
        String logicIndexName = IndexMetaDataUtil.getLogicIndexName("order_t_order_index_t_order", "t_order");
        Assert.assertEquals(logicIndexName, "order_t_order_index");
    }
    
    @Test
    public void assertGetLogicIndexNameWithoutIndexNameSuffix() {
        String logicIndexName = IndexMetaDataUtil.getLogicIndexName("order_index", "t_order");
        Assert.assertEquals(logicIndexName, "order_index");
    }
    
    @Test
    public void assertGetActualIndexNameWithActualTableName() {
        String actualIndexName = IndexMetaDataUtil.getActualIndexName("order_index", "t_order");
        Assert.assertEquals(actualIndexName, "order_index_t_order");
    }
    
    @Test
    public void assertGetActualIndexNameWithoutActualTableName() {
        String actualIndexName = IndexMetaDataUtil.getActualIndexName("order_index", null);
        Assert.assertEquals(actualIndexName, "order_index");
    }
}
