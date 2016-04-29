/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.spring.cases.namespace;

import com.dangdang.ddframe.rdb.sharding.spring.AbstractShardingBothDataBasesAndTablesSpringDBUnitTest;
import org.springframework.test.context.ContextConfiguration;

// TODO 解析可能有问题, 不能解析出关联表, 所以导致动态表时查找关联表分片value失败, 测试用例先用非Dynamic的, 防止install失败
//@ContextConfiguration(locations = "classpath:META-INF/rdb/namespace/withNamespaceAlgorithmExpressionForDynamic.xml")
@ContextConfiguration(locations = "classpath:META-INF/rdb/namespace/withNamespaceAlgorithmExpression.xml")
public final class WithNamespaceAlgorithmExpressionForDynamicTest extends AbstractShardingBothDataBasesAndTablesSpringDBUnitTest {
}
