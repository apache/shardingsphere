package io.shardingjdbc.dbtest.config.bean;


import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class ParameterDefinition {

    @XmlAttribute(name = "value")
    private String value;

    @XmlAttribute(name = "type")
    private String type = "String";



}
