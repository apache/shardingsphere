package io.shardingjdbc.dbtest.config.bean;

import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@XmlRootElement(name = "asserts")
public class AssertsDefinition {

    @XmlAttribute(name = "sharding-rule-config")
    private String shardingRuleConfig;

    private String path;

    @XmlElement(name = "assert")
    private List<AssertDefinition> asserts;

    public void setPath(String path) {
        this.path = path;
    }
}
