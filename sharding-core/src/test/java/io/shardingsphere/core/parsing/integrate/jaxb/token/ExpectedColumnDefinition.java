package io.shardingsphere.core.parsing.integrate.jaxb.token;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter

public class ExpectedColumnDefinition {
    @XmlAttribute
    private String name;
    
    @XmlAttribute
    private String type;
    
    @XmlAttribute
    private Integer length;
    
    @XmlAttribute(name = "primary-key")
    private boolean primaryKey;
}
