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

package io.shardingsphere.dbtest.cases.dataset.init;

import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import lombok.Getter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@XmlRootElement(name = "datasets")
public final class DataSetsRoot {
    
    @XmlElement(name = "metadata")
    private List<DataSetMetadata> metadataList;
    
    @XmlElement(name = "dataset")
    private List<DataSetRow> dataSetRows;
    
    /**
     * find data set meta data.
     * 
     * @param dataNode data node
     * @return data set meta data
     */
    public DataSetMetadata findDataSetMetadata(final DataNode dataNode) {
        for (DataSetMetadata each : metadataList) {
            if (contains(new InlineExpressionParser(each.getDataNodes()).evaluate(), dataNode)) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find data node: %s", dataNode.toString()));
    }
    
    private boolean contains(final List<String> dataNodes, final DataNode dataNode) {
        for (String each : dataNodes) {
            if (new DataNode(each).equals(dataNode)) {
                return true;
            }
        }
        return false;
    }
}
