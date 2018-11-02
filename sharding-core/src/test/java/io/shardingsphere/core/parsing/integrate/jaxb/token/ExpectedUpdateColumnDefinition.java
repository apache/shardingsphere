package io.shardingsphere.core.parsing.integrate.jaxb.token;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class ExpectedUpdateColumnDefinition extends ExpectedColumnDefinition {
    @XmlAttribute(name = "origin-column-name")
    private String originColumnName;
}
