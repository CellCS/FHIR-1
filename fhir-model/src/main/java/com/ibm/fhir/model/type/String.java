/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.annotation.Generated;

import com.ibm.fhir.model.util.ValidationSupport;
import com.ibm.fhir.model.visitor.Visitor;

/**
 * A sequence of Unicode characters
 */
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class String extends Element {
    private static final Pattern PATTERN = Pattern.compile("[ \\r\\n\\t\\S]+");

    protected final java.lang.String value;

    private volatile int hashCode;

    protected String(Builder builder) {
        super(builder);
        value = builder.value;
//        ValidationSupport.checkMaxLength(value);
//        ValidationSupport.checkMinLength(value);
//        ValidationSupport.checkValue(value, PATTERN);
        ValidationSupport.checkString(value);
        ValidationSupport.requireValueOrChildren(this);
    }

    /**
     * The actual value
     * 
     * @return
     *     An immutable object of type {@link java.lang.String}.
     */
    public java.lang.String getValue() {
        return value;
    }

    @Override
    public boolean hasValue() {
        return (value != null);
    }

    @Override
    public boolean hasChildren() {
        return super.hasChildren();
    }

    public static String of(java.lang.String value) {
        return String.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return String.builder().value(value).build();
    }

    @Override
    public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
        if (visitor.preVisit(this)) {
            visitor.visitStart(elementName, elementIndex, this);
            if (visitor.visit(elementName, elementIndex, this)) {
                // visit children
                accept(id, "id", visitor);
                accept(extension, "extension", visitor, Extension.class);
                accept(value, "value", visitor);
            }
            visitor.visitEnd(elementName, elementIndex, this);
            visitor.postVisit(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        String other = (String) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, 
                extension, 
                value);
            hashCode = result;
        }
        return result;
    }

    @Override
    public Builder toBuilder() {
        return new Builder().from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Element.Builder {
        protected java.lang.String value;

        protected Builder() {
            super();
        }

        /**
         * unique id for the element within a resource (for internal references)
         * 
         * @param id
         *     xml:id (or equivalent in JSON)
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * The actual value
         * 
         * @param value
         *     Primitive value for string
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder value(java.lang.String value) {
            this.value = value;
            return this;
        }

        /**
         * Build the {@link String}
         * 
         * @return
         *     An immutable object of type {@link String}
         */
        @Override
        public String build() {
            return new String(this);
        }

        protected Builder from(String string) {
            super.from(string);
            value = string.value;
            return this;
        }
    }
}
