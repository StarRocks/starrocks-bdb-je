/*-
 * Copyright (C) 2002, 2018, Oracle and/or its affiliates. All rights reserved.
 *
 * This file was distributed by Oracle as part of a version of Oracle Berkeley
 * DB Java Edition made available at:
 *
 * http://www.oracle.com/technetwork/database/database-technologies/berkeleydb/downloads/index.html
 *
 * Please see the LICENSE file included in the top-level directory of the
 * appropriate version of Oracle Berkeley DB Java Edition for a copy of the
 * license and additional information.
 */

package com.sleepycat.je.utilint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.SortedSet;

import com.sleepycat.je.EnvironmentFailureException;

/**
 * A home for misc formatting utilities.
 */
public class FormatUtil {

    /**
     * Utility class to convert a sorted set of long values to a compact string
     * suitable for printing. The representation is made compact by identifying
     * ranges so that the sorted set can be represented as a sequence of hex
     * ranges and singletons.
     */
    public static String asHexString(SortedSet<Long> set) {

        if (set.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        java.util.Iterator<Long> i = set.iterator();
        long rstart = i.next();
        long rend = rstart;

        while (i.hasNext()) {
            final long f= i.next();
            if (f == (rend + 1)) {
                /* Continue the existing range. */
                rend++;
                continue;
            }

            /* flush and start new range */
            flushRange(sb, rstart, rend);
            rstart = rend = f;
        };

        flushRange(sb, rstart, rend);
        return sb.toString();
    }

    private static void flushRange(final StringBuilder sb,
                                   long rstart,
                                   long rend) {
        if (rstart == -1) {
            return;
        }

        if (rstart == rend) {
            sb.append(" 0x").append(Long.toHexString(rstart));
        } else {
            sb.append(" 0x").append(Long.toHexString(rstart)).
            append("-").
            append("0x").append(Long.toHexString(rend));
        }
    }

    /**
     * Clones an object by serializing it and then de-serializing it.
     */
    public static <T> T cloneBySerialization(final T obj) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            (new ObjectOutputStream(baos)).writeObject(obj);
            final byte[] bytes = baos.toByteArray();

            final ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bytes));

            @SuppressWarnings("unchecked")
            final T ret = (T) ois.readObject();

            return ret;

        } catch (ClassNotFoundException|IOException e) {
            throw EnvironmentFailureException.unexpectedException(e);
        }
    }
}
