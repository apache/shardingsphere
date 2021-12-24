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

package org.apache.shardingsphere.infra.instance;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.instance.utils.IpUtils;
import org.junit.Test;

import java.lang.management.ManagementFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class InstanceTest {
    
    private String ip = IpUtils.getIp();
    
    @Test
    public void assertGetIdWithPort() {
        Instance.getInstance().init(3307);
        String id = Instance.getInstance().getId();
        assertThat(id.split("@").length, is(2));
        assertThat(id, is(Joiner.on("@").join(ip, 3307)));
    }
    
    @Test
    public void assertGetDefaultId() {
        Instance.getInstance().init(null);
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        String id = Instance.getInstance().getId();
        assertThat(id.split("@").length, is(2));
        assertThat(id, is(Joiner.on("@").join(ip, pid)));
    }
    
    @Test
    public void assertGetInstanceId() {
        Instance.getInstance().init(null);
        assertThat(Instance.getInstance().getInstanceId("127.0.0.1", "3307"), is("127.0.0.1@3307"));
    }
}
