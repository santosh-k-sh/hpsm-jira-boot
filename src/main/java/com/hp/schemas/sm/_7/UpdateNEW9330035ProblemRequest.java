
package com.hp.schemas.sm._7;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="model" type="{http://schemas.hp.com/SM/7}NEW9.33.0035ProblemModelType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="attachmentInfo" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="attachmentData" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="ignoreEmptyElements" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="updateconstraint" type="{http://www.w3.org/2001/XMLSchema}long" default="-1" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "model"
})
@XmlRootElement(name = "UpdateNEW9.33.0035ProblemRequest")
public class UpdateNEW9330035ProblemRequest {

    @XmlElement(required = true)
    protected NEW9330035ProblemModelType model;
    @XmlAttribute(name = "attachmentInfo")
    protected Boolean attachmentInfo;
    @XmlAttribute(name = "attachmentData")
    protected Boolean attachmentData;
    @XmlAttribute(name = "ignoreEmptyElements")
    protected Boolean ignoreEmptyElements;
    @XmlAttribute(name = "updateconstraint")
    protected Long updateconstraint;

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link NEW9330035ProblemModelType }
     *     
     */
    public NEW9330035ProblemModelType getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link NEW9330035ProblemModelType }
     *     
     */
    public void setModel(NEW9330035ProblemModelType value) {
        this.model = value;
    }

    /**
     * Gets the value of the attachmentInfo property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAttachmentInfo() {
        return attachmentInfo;
    }

    /**
     * Sets the value of the attachmentInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttachmentInfo(Boolean value) {
        this.attachmentInfo = value;
    }

    /**
     * Gets the value of the attachmentData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAttachmentData() {
        return attachmentData;
    }

    /**
     * Sets the value of the attachmentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttachmentData(Boolean value) {
        this.attachmentData = value;
    }

    /**
     * Gets the value of the ignoreEmptyElements property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIgnoreEmptyElements() {
        if (ignoreEmptyElements == null) {
            return true;
        } else {
            return ignoreEmptyElements;
        }
    }

    /**
     * Sets the value of the ignoreEmptyElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIgnoreEmptyElements(Boolean value) {
        this.ignoreEmptyElements = value;
    }

    /**
     * Gets the value of the updateconstraint property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getUpdateconstraint() {
        if (updateconstraint == null) {
            return -1L;
        } else {
            return updateconstraint;
        }
    }

    /**
     * Sets the value of the updateconstraint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUpdateconstraint(Long value) {
        this.updateconstraint = value;
    }

}
