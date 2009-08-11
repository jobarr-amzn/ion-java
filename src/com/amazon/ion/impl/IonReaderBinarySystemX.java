// Copyright (c) 2009 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion.impl;

import com.amazon.ion.IonType;
import com.amazon.ion.SymbolTable;
import com.amazon.ion.Timestamp;
import com.amazon.ion.impl.IonScalarConversionsX.AS_TYPE;
import com.amazon.ion.impl.IonScalarConversionsX.ValueVariant;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;

/**
 *
 */
public class IonReaderBinarySystemX extends IonReaderBinaryRawX
{

    // ValueVariant _v; actually owned by the raw reader so it can be cleared at appropriate times

    public IonReaderBinarySystemX(byte[] bytes) {
        UnifiedInputStreamX uis = UnifiedInputStreamX.makeStream(bytes);
        init(uis);
    }
    public IonReaderBinarySystemX(byte[] bytes, int offset, int length) {
        UnifiedInputStreamX uis = UnifiedInputStreamX.makeStream(bytes, offset, length);
        init(uis);

    }
    public IonReaderBinarySystemX(InputStream userBytes) {
        UnifiedInputStreamX uis;
        try {
            uis = UnifiedInputStreamX.makeStream(userBytes);
        }
        catch (IOException e) {
            throw new IonReaderBinaryExceptionX(e);
        }
        init(uis);
    }
    public IonReaderBinarySystemX(UnifiedInputStreamX userBytes) {
        init(userBytes);
    }


    //
    // public methods that typically user level methods
    // these are filled in by either the system reader
    // or the user reader.  Here they just fail.
    //

    @Override
    public int getFieldId()
    {
        return _value_field_id;
    }

    @Override
    public Iterator<Integer> iterateTypeAnnotationIds()
    {
        Iterator<Integer> it = new IonReaderTextUserX.IntIterator(_annotation_ids, 0, _annotation_count);
        return it;
    }

    @Override
    public int[] getTypeAnnotationIds()
    {
        try {
            load_annotations();
        }
        catch (IOException e) {
            error(e);
        }
        int[] anns = new int[_annotation_count];
        System.arraycopy(_annotation_ids, 0, anns, 0, _annotation_count);
        return anns;
    }

    //
    //  basic scalar value getters (for actual content)
    //
    protected final void prepare_value(int as_type) {
        if (_v.isEmpty()) {
            try {
                load_cached_value(as_type);
            }
            catch (IOException e) {
                error(e);
            }
        }
        if (as_type != 0 && !_v.hasValueOfType(as_type)) {
            // we should never get here with a symbol asking for anything other
            // than a numeric cast (from some other numeric already loaded)
            assert( !IonType.SYMBOL.equals(_value_type)
                 || (_v.hasNumericType() && ValueVariant.isNumericType(as_type)));

            if (!_v.can_convert(as_type)) {
                String message = "can't cast from "
                    +IonScalarConversionsX.getValueTypeName(_v.getAuthoritativeType())
                    +" to "
                    +IonScalarConversionsX.getValueTypeName(as_type);
                error_at(message);
            }
            int fnid = _v.get_conversion_fnid(as_type);
            _v.cast(fnid);
        }
    }

    /**
     * this checks the state of the raw reader to make sure
     * this is valid.  It also checks for an existing cached
     * value of the correct type.  It will either cast the
     * current value from an existing type to the type desired
     * or it will construct the desired type from the raw
     * input in the raw reader
     *
     * @param value_type desired value type (in local type terms)
     * @throws IOException
     */
    protected final void load_cached_value(int value_type) throws IOException
    {
        if (_v.isEmpty()) {
            load_scalar_value();
        }
    }

    static final int MAX_BINARY_LENGTH_INT = Integer.SIZE;
    static final int MAX_BINARY_LENGTH_LONG = Long.SIZE;

    private final void load_scalar_value() throws IOException
    {
        // make sure we're trying to load a scalar value here
        switch(_value_type) {
        case NULL:
        case BOOL:
        case INT:
        case FLOAT:
        case DECIMAL:
        case TIMESTAMP:
        case SYMBOL:
        case STRING:
            break;
        default:
            return;
        }

        // this will be true when the value_type is null as
        // well as when we encounter a null of any other type
        if (_value_is_null) {
            _v.setValueToNull(_value_type);
            _v.setAuthoritativeType(AS_TYPE.null_value);
            return;
        }

        switch (_value_type) {
        default:
            return;
        case BOOL:
            _v.setValue(_value_is_true);
            _v.setAuthoritativeType(AS_TYPE.boolean_value);
            break;
        case INT:
            if (_value_len == 0) {
                int v = 0;
                _v.setValue(v);
                _v.setAuthoritativeType(AS_TYPE.int_value);
            }
            else if (_value_len <= MAX_BINARY_LENGTH_LONG) {
                long v = readULong(_value_len);
                if (_value_tid == IonConstants.tidNegInt) {
                    v = -v;
                }
                _v.setValue(v);
                _v.setAuthoritativeType(AS_TYPE.long_value);
            }
            else {
                boolean is_negative = (_value_tid == IonConstants.tidNegInt);
                BigInteger v = readBigInteger(_value_len, is_negative);
                _v.setValue(v);
                _v.setAuthoritativeType(AS_TYPE.bigInteger_value);
            }
            break;
        case FLOAT:
            double d;
            if (_value_len == 0) {
                d = 0.0;
            }
            else {
                d = readFloat(_value_len);
            }
            _v.setValue(d);
            _v.setAuthoritativeType(AS_TYPE.double_value);
            break;
        case DECIMAL:
            BigDecimal bd = readDecimal(_value_len);
            _v.setValue(bd);
            _v.setAuthoritativeType(AS_TYPE.bigDecimal_value);
            break;
        case TIMESTAMP:
            // TODO: it looks like a 0 length return a null timestamp - is that right?
            Timestamp t = readTimestamp(_value_len);
            _v.setValue(t);
            _v.setAuthoritativeType(AS_TYPE.timestamp_value);
            break;
        case SYMBOL:
            long sid = readULong(_value_len);
            if (sid < 1 || sid > Integer.MAX_VALUE) {
                String message = "symbol id ["
                               + sid
                               + "] out of range "
                               + "(1-"
                               + Integer.MAX_VALUE
                               + ")";
                error_at(message);
            }
            // TODO: is treating this as an int too misleading?
            _v.setValue((int)sid);
            _v.setAuthoritativeType(AS_TYPE.int_value);
            break;
        case STRING:
            String s = readString(_value_len);
            _v.setValue(s);
            _v.setAuthoritativeType(AS_TYPE.string_value);
            break;
        }
        _state = State.S_AFTER_VALUE;
    }

    //
    // public value routines
    //
    @Override
    public boolean isNullValue()
    {
        return _value_is_null;
    }
    @Override
    public boolean booleanValue()
    {
        prepare_value(AS_TYPE.boolean_value);
        return _v.getBoolean();
    }
    @Override
    public double doubleValue()
    {
        prepare_value(AS_TYPE.double_value);
        return _v.getDouble();
    }
    @Override
    public int intValue()
    {
        prepare_value(AS_TYPE.int_value);
        return _v.getInt();
    }
    @Override
    public long longValue()
    {
        prepare_value(AS_TYPE.long_value);
        return _v.getLong();
    }
    @Override
    public BigInteger bigIntegerValue()
    {
        prepare_value(AS_TYPE.bigInteger_value);
        return _v.getBigInteger();
    }
    @Override
    public BigDecimal bigDecimalValue()
    {
        prepare_value(AS_TYPE.bigDecimal_value);
        return _v.getBigDecimal();
    }
    @Override
    public Date dateValue()
    {
        prepare_value(AS_TYPE.date_value);
        return _v.getDate();
    }
    @Override
    public Timestamp timestampValue()
    {
        prepare_value(AS_TYPE.timestamp_value);
        return _v.getTimestamp();
    }
    @Override
    public String stringValue()
    {
        if (IonType.SYMBOL.equals(_value_type)) {
            throw new UnsupportedOperationException("not supported - use UserReader");
        }
        prepare_value(AS_TYPE.string_value);
        return _v.getString();
    }

    //
    // unsupported public methods that require a symbol table
    // to operate - which is only supported on a user reader
    //
    @Override
    public String getFieldName()
    {
        throw new UnsupportedOperationException("not supported - use UserReader");
    }
    @Override
    public Iterator<String> iterateTypeAnnotations()
    {
        throw new UnsupportedOperationException("not supported - use UserReader");
    }
    @Override
    public String[] getTypeAnnotations()
    {
        throw new UnsupportedOperationException("not supported - use UserReader");
    }
    @Override
    public SymbolTable getSymbolTable()
    {
        return null;
    }

}