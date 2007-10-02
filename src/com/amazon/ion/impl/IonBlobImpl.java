/*
 * Copyright (c) 2007 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion.impl;


import com.amazon.ion.IonBlob;
import com.amazon.ion.ValueVisitor;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements the Ion <code>blob</code> type.
 */
public final class IonBlobImpl
    extends IonLobImpl
    implements IonBlob
{

    static final int NULL_BLOB_TYPEDESC =
         IonConstants.makeTypeDescriptor(IonConstants.tidBlob,
                                         IonConstants.lnIsNullAtom);

    /**
     * Constructs a <code>null.blob</code> element.
     */
    public IonBlobImpl()
    {
        super(NULL_BLOB_TYPEDESC);
    }

    /**
     * Constructs a binary-backed element.
     */
    public IonBlobImpl(int typeDesc)
    {
        super(typeDesc);
        assert pos_getType() == IonConstants.tidBlob;
    }


    public void appendBase64(Appendable out)
        throws IOException
    {
        validateThisNotNull();
        InputStream byteStream = newInputStream();
        try
        {
            Base64Encoder.TextStream ts =
                new Base64Encoder.TextStream(byteStream);

            for (;;) {
                int c = ts.read();
                if (c == -1) break;
                out.append((char) c);
            }
        }
        finally
        {
            byteStream.close();
        }
    }


    public void accept(ValueVisitor visitor) throws Exception
    {
        makeReady();
        visitor.visit(this);
    }
}