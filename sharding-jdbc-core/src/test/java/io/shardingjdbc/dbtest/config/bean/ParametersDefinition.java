package io.shardingjdbc.dbtest.config.bean;


import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import lombok.Getter;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class ParametersDefinition {

    @XmlElement(name = "parameter")
    private List<ParameterDefinition> parameter;



}
