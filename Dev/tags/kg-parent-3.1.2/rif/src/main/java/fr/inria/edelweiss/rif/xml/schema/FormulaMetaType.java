//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.08.24 at 10:15:40 AM CEST 
//


package fr.inria.edelweiss.rif.xml.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for formula-meta.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="formula-meta.type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2007/rif#}Frame"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "formula-meta.type", propOrder = {
    "frame"
})
public class FormulaMetaType {

    @XmlElement(name = "Frame", required = true)
    protected Frame frame;

    /**
     * Gets the value of the frame property.
     * 
     * @return
     *     possible object is
     *     {@link Frame }
     *     
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * Sets the value of the frame property.
     * 
     * @param value
     *     allowed object is
     *     {@link Frame }
     *     
     */
    public void setFrame(Frame value) {
        this.frame = value;
    }

}
