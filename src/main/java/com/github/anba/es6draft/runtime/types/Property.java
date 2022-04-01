/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.types;

import static com.github.anba.es6draft.runtime.types.Undefined.UNDEFINED;

/**
 * <h1>6 ECMAScript Data Types and Values</h1><br>
 * <h2>6.1 ECMAScript Language Types</h2><br>
 * <h3>6.1.7 The Object Type</h3>
 * <ul>
 * <li>6.1.7.1 Property Attributes
 * </ul>
 */
public final class Property implements Cloneable {
    private enum PropertyType {
        Data, Accessor
    }

    private PropertyType type;
    private Object value;
    private Callable getter;
    private Callable setter;
    private boolean writable;
    private boolean enumerable;
    private boolean configurable;

    /**
     * Copy constructor.
     * 
     * @param original
     *            the source property
     */
    private Property(Property original) {
        type = original.type;
        value = original.value;
        getter = original.getter;
        setter = original.setter;
        writable = original.writable;
        enumerable = original.enumerable;
        configurable = original.configurable;
    }

    /**
     * Create a new {@link Property} from the supplied {@link PropertyDescriptor} object.
     * <p>
     * <strong>package-private for PropertyDescriptor</strong>
     * 
     * @param original
     *            the source property descriptor
     */
    Property(PropertyDescriptor original) {
        type = original.isAccessorDescriptor() ? PropertyType.Accessor : PropertyType.Data;
        value = original.getValue();
        getter = original.getGetter();
        setter = original.getSetter();
        writable = original.isWritable();
        enumerable = original.isEnumerable();
        configurable = original.isConfigurable();
    }

    /**
     * Create a new {@link Property} object for a data-property.
     * 
     * @param value
     *            the property value
     * @param writable
     *            the enumerable flag
     * @param enumerable
     *            the writable flag
     * @param configurable
     *            the configurable flag
     */
    public Property(Object value, boolean writable, boolean enumerable, boolean configurable) {
        this.type = PropertyType.Data;
        this.value = value;
        this.getter = null;
        this.setter = null;
        this.writable = writable;
        this.enumerable = enumerable;
        this.configurable = configurable;
    }

    /**
     * Create a new {@link Property} object for an accessor-property.
     * 
     * @param getter
     *            the accessor getter function, may be {@code null}
     * @param setter
     *            the accessor setter function, may be {@code null}
     * @param enumerable
     *            the writable flag
     * @param configurable
     *            the configurable flag
     */
    public Property(Callable getter, Callable setter, boolean enumerable, boolean configurable) {
        this.type = PropertyType.Accessor;
        this.value = UNDEFINED;
        this.getter = getter;
        this.setter = setter;
        this.writable = false;
        this.enumerable = enumerable;
        this.configurable = configurable;
    }

    /**
     * Converts this property to a data-property.
     */
    public void toDataProperty() {
        toProperty(PropertyType.Data);
    }

    /**
     * Converts this property to an accessor-property.
     */
    public void toAccessorProperty() {
        toProperty(PropertyType.Accessor);
    }

    private void toProperty(PropertyType newType) {
        assert type != newType;
        type = newType;
        // default attribute values per 6.1.7.1, table 3
        value = UNDEFINED;
        getter = null;
        setter = null;
        writable = false;
    }

    /**
     * Copies all present fields from {@code desc} into this {@link Property} object.
     * 
     * @param desc
     *            the property descriptor
     */
    public void apply(PropertyDescriptor desc) {
        if (isDataDescriptor()) {
            if (desc.hasValue()) {
                value = desc.getValue();
            }
            if (desc.hasWritable()) {
                writable = desc.isWritable();
            }
        } else {
            if (desc.hasGetter()) {
                getter = desc.getGetter();
            }
            if (desc.hasSetter()) {
                setter = desc.getSetter();
            }
        }
        if (desc.hasEnumerable()) {
            enumerable = desc.isEnumerable();
        }
        if (desc.hasConfigurable()) {
            configurable = desc.isConfigurable();
        }
    }

    /**
     * Updates the [[Value]] field of this {@link Property} object. Only applicable for writable data-properties.
     * 
     * @param value
     *            the new value
     */
    public void setValue(Object value) {
        assert isDataDescriptor() && writable && value != null;
        this.value = value;
    }

    /**
     * Returns a new {@link PropertyDescriptor} object for this property.
     * 
     * @return a new property descriptor for this property
     */
    public PropertyDescriptor toPropertyDescriptor() {
        return new PropertyDescriptor(this);
    }

    @Override
    public Property clone() {
        return new Property(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{');
        if (isDataDescriptor()) {
            sb.append("[[Writable]]: ").append(isWritable()).append(", ");
        }
        sb.append("[[Enumerable]]: ").append(isEnumerable()).append(", ");
        sb.append("[[Configurable]]: ").append(isConfigurable()).append(", ");
        if (isDataDescriptor()) {
            sb.append("[[Value]]: ").append(getValue());
        } else {
            sb.append("[[Get]]: ").append(getGetter()).append(", ");
            sb.append("[[Set]]: ").append(getSetter());
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * 6.2.5.1 IsAccessorDescriptor ( Desc )<br>
     * Returns {@code true} if this object is an accessor property descriptor.
     * 
     * @return {@code true} if this object is an accessor property
     */
    public boolean isAccessorDescriptor() {
        return type == PropertyType.Accessor;
    }

    /**
     * 6.2.5.2 IsDataDescriptor ( Desc )<br>
     * Returns {@code true} if this object is a data property descriptor.
     * 
     * @return {@code true} if this object is a data property
     */
    public boolean isDataDescriptor() {
        return type == PropertyType.Data;
    }

    /**
     * Returns the <code>[[Value]]</code> field.
     * 
     * @return the value field
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the <code>[[Get]]</code> field.
     * 
     * @return the getter field
     */
    public Callable getGetter() {
        return getter;
    }

    /**
     * Returns the <code>[[Set]]</code> field.
     * 
     * @return the setter field
     */
    public Callable getSetter() {
        return setter;
    }

    /**
     * Returns the <code>[[Writable]]</code> field.
     * 
     * @return the writable flag
     */
    public boolean isWritable() {
        return writable;
    }

    /**
     * Returns the <code>[[Enumerable]]</code> field.
     * 
     * @return the enumerable flag
     */
    public boolean isEnumerable() {
        return enumerable;
    }

    /**
     * Returns the <code>[[Configurable]]</code> field.
     * 
     * @return the configurable flag
     */
    public boolean isConfigurable() {
        return configurable;
    }
}
