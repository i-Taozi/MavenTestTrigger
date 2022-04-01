/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.runtime.objects.binary;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.anba.es6draft.runtime.Realm;
import com.github.anba.es6draft.runtime.internal.Bytes;
import com.github.anba.es6draft.runtime.types.ScriptObject;
import com.github.anba.es6draft.runtime.types.builtins.OrdinaryObject;

/**
 * <h1>24 Structured Data</h1><br>
 * <h2>24.1 ArrayBuffer Objects</h2>
 * <ul>
 * <li>24.1.5 Properties of the ArrayBuffer Instances
 * </ul>
 */
public final class ArrayBufferObject extends OrdinaryObject implements ArrayBuffer {
    /** [[ArrayBufferData]] */
    private ByteBuffer data;
    private ByteBuffer nonNativeData;

    /** [[ArrayBufferByteLength]] */
    private long byteLength;

    private boolean detached;

    /**
     * Constructs a new ArrayBuffer object.
     * 
     * @param realm
     *            the realm object
     * @param data
     *            the byte buffer
     * @param byteLength
     *            the byte length
     * @param prototype
     *            the prototype object
     */
    public ArrayBufferObject(Realm realm, ByteBuffer data, long byteLength, ScriptObject prototype) {
        super(realm, prototype);
        assert data != null : "cannot initialize ArrayBuffer with null";
        assert data.order() == Bytes.DEFAULT_BYTE_ORDER : "unexpected byte order";
        assert byteLength >= 0 : "negative byte length: " + byteLength;
        assert byteLength == data.capacity() : "invalid byte length: " + byteLength;
        this.data = data;
        this.byteLength = byteLength;
    }

    /**
     * [[ArrayBufferData]]
     * 
     * @return the underlying byte buffer or {@code null} if detached
     */
    @Override
    public ByteBuffer getData() {
        return data;
    }

    @Override
    public ByteBuffer getData(ByteOrder byteOrder) {
        if (data.order() == byteOrder) {
            return data;
        }
        ByteBuffer view = nonNativeData;
        if (view == null) {
            view = data.duplicate().order(byteOrder);
            nonNativeData = view;
        }
        return view;
    }

    /**
     * [[ArrayBufferByteLength]]
     * 
     * @return the array buffer length in bytes or {@code 0} if detached
     */
    @Override
    public long getByteLength() {
        return byteLength;
    }

    /**
     * Detaches this array buffer object.
     */
    @Override
    public void detach() {
        data = null;
        nonNativeData = null;
        byteLength = 0;
        detached = true;
    }

    /**
     * Returns {@code true} if this array buffer object has been detached.
     * 
     * @return {@code true} if this array buffer object is detached
     */
    @Override
    public boolean isDetached() {
        return detached;
    }

    @Override
    public boolean sameData(ArrayBuffer other) {
        return other == this;
    }

    @Override
    public String toString() {
        return String.format("%s, data=%s, byteLength=%d, detached=%b", super.toString(), data, byteLength, detached);
    }
}
