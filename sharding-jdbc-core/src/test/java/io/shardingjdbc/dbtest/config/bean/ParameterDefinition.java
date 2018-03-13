package io.shardingjdbc.dbtest.config.bean;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class ParameterDefinition {

    @XmlAttribute(name = "value")
    private String value;

    @XmlAttribute(name = "type")
    private String type = "String";



}
