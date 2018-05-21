/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.spring.namespace;

public class ResourceLocator {
    
    private RegTypeEnum regTypeEnum;
    
    public ResourceLocator() {
        this.regTypeEnum = RegTypeEnum.ZK_LOCAL;
    }
    
    public ResourceLocator(RegTypeEnum regTypeEnum) {
        this.regTypeEnum = regTypeEnum;
    }
    
    public String getConfigFileName(final String fileName) {
        switch (regTypeEnum) {
            case ZK_LOCAL:
                return doBuild("zookeeper/local/", fileName);
            case ZK_CLOUD:
                return doBuild("zookeeper/cloud/", fileName);
            case ETCD_LOCAL:
                return doBuild("etcd/local/", fileName);
            case ETCD_CLOUD:
                return doBuild("etcd/cloud/", fileName);
            default:
                 return doBuild("zookeeper/local/", fileName);
        }
    }
    
    private String doBuild(String regPath, String fileName) {
        return "META-INF/orche/" + regPath + fileName;
    }
}
