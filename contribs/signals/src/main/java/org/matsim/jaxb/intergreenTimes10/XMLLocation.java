//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.01.31 at 06:47:59 PM MEZ 
//


package org.matsim.jaxb.intergreenTimes10;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.matsim.org/files/dtd}coordinate" minOccurs="0"/>
 *         &lt;element name="actLocation" type="{http://www.matsim.org/files/dtd}actLocationType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "coordinate",
    "actLocation"
})
@XmlRootElement(name = "location")
final class XMLLocation {

    private XMLCoordinateType coordinate;
    private XMLActLocationType actLocation;

    /**
     * Gets the value of the coordinate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLCoordinateType }
     *     
     */
    public XMLCoordinateType getCoordinate() {
        return coordinate;
    }

    /**
     * Sets the value of the coordinate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLCoordinateType }
     *     
     */
    public void setCoordinate(XMLCoordinateType value) {
        this.coordinate = value;
    }

    /**
     * Gets the value of the actLocation property.
     * 
     * @return
     *     possible object is
     *     {@link XMLActLocationType }
     *     
     */
    public XMLActLocationType getActLocation() {
        return actLocation;
    }

    /**
     * Sets the value of the actLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLActLocationType }
     *     
     */
    public void setActLocation(XMLActLocationType value) {
        this.actLocation = value;
    }

}
