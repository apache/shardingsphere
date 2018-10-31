package io.shardingsphere.core.parsing.integrate.jaxb.token;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ExpectedColumnPosition {
    @XmlAttribute(name = "start-index")
    private int startIndex;

    @XmlAttribute(name = "first-column")
    private String firstColumn;

    @XmlAttribute(name = "column-name")
    private String columnName;
    
    @XmlAttribute(name = "after-column")
    private String afterColumn;
}
