//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.10 at 02:09:32 PM CEST 
//


package at.bakery.kippen.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for typeEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="typeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cube"/>
 *     &lt;enumeration value="barrel"/>
 *     &lt;enumeration value="ball"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "typeEnum")
@XmlEnum
public enum TypeEnum {

    @XmlEnumValue("cube")
    CUBE("cube"),
    @XmlEnumValue("barrel")
    BARREL("barrel"),
    @XmlEnumValue("ball")
    BALL("ball");
    private final String value;

    TypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum c: TypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
