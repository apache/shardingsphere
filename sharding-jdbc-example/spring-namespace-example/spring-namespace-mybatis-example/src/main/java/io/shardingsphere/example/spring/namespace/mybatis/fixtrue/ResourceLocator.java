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

package io.shardingsphere.example.spring.namespace.mybatis.fixtrue;

public class ResourceLocator {
    
    private RegTypeEnum regTypeEnum;

    public ResourceLocator() {
        this.regTypeEnum = RegTypeEnum.ZK_LOCAL;
    }
    
    public ResourceLocator(final RegTypeEnum regTypeEnum) {
        this.regTypeEnum = regTypeEnum;
    }
    
    public String getConfigFileName(final String fileName) {
        switch (regTypeEnum) {
            case ZK_LOCAL:
                return "META-INF/orche/zookeeper/local/" + fileName;
            case ZK_CLOUD:
                return "META-INF/orche/zookeeper/cloud/" + fileName;
            case ETCD_LOCAL:
                return "META-INF/orche/etcd/local/" + fileName;
            case ETCD_CLOUD:
                return "META-INF/orche/etcd/cloud/" + fileName;
            default:
                return "META-INF/orche/zookeeper/local/" + fileName;
        }
    }
}
