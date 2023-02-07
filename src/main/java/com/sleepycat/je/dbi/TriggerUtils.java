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
package com.sleepycat.je.dbi;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.sleepycat.je.log.LogUtils;
import com.sleepycat.je.trigger.Trigger;

/**
 * Utility functions used by the trigger implementation.
 *
 */
public class TriggerUtils {

    /**
     * Determines the size in bytes used to represent the trigger in the log,
     * that is, the size of the output generated by
     * {@link #writeTriggers(ByteBuffer, byte[][])}
     *
     * @param triggerBytes the triggers whose size is to be estimated.
     *
     * @return the size in bytes
     */
    static int logSize(byte[][] triggerBytes) {

        if (triggerBytes == null) {
            return LogUtils.getPackedIntLogSize(0);
        }

        /* Add up the individual trigger sizes */
        int size = LogUtils.getPackedIntLogSize(triggerBytes.length);
        for (byte[] trigger : triggerBytes) {
            size += LogUtils.getByteArrayLogSize(trigger);
        }
        return size;
    }

    /**
     * Writes the triggers out to the log buffer.
     *
     * @param logBuffer the buffer in which the bytes are assembled.
     *
     * @param triggerBytes the trigger bytes to be written.
     */
    static void writeTriggers(ByteBuffer logBuffer,
                              byte[][] triggerBytes) {
        if (triggerBytes == null) {
            LogUtils.writePackedInt(logBuffer, 0);
        } else {
            /* Write out the triggers. */
            LogUtils.writePackedInt(logBuffer, triggerBytes.length);
            for (byte[] triggerByte : triggerBytes) {
                LogUtils.writeByteArray(logBuffer, triggerByte);
            }
        }
    }

    /**
     * Reads the triggers from a log buffer and returns then in their
     * serialized byte array form.
     *
     * @param logBuffer the buffer from which to read the triggers.
     * @param entryVersion the version associated with the current log
     *
     * @return the trigger bytes
     */
    static byte[][] readTriggers(ByteBuffer logBuffer,
                                 int entryVersion) {

        final int triggerCount = LogUtils.readPackedInt(logBuffer);
        if (triggerCount == 0) {
            return null;
        }

        byte[][] triggerBytes = new byte[triggerCount][];
        for (int i = 0; i < triggerBytes.length; i++) {
            triggerBytes[i] =
                LogUtils.readByteArray(logBuffer, false /* unpacked */);
        }
        return triggerBytes;
    }

    /**
     * Deserializes the trigger representation to yield the trigger object
     * instance.
     *
     * @param dbName the name to be associated with the de-serialized triggers
     *
     * @param triggerBytes the serialized representation of the trigger
     *
     * @return the list of trigger instances
     */
    static LinkedList<Trigger> unmarshallTriggers(String dbName,
                                                  byte[][] triggerBytes,
                                                  ClassLoader loader) {

        if (triggerBytes == null) {
            return null;
        }

        final LinkedList<Trigger> triggers = new LinkedList<Trigger>();
        for (int i = 0; i < triggerBytes.length; i++) {
            final Trigger trigger =
                (Trigger)DatabaseImpl.bytesToObject(triggerBytes[i],
                                                    "trigger:" + i,
                                                    loader);
            trigger.setDatabaseName(dbName);
            triggers.add(trigger);
        }
        return triggers;
    }

    /**
     * Dumps an XML representation of the triggers into the StringBuilder. It
     * gives preference to the instance representation if it's readily
     * available.
     *
     * @param sb the string buffer that will contain the XML representation
     * @param triggerBytes the bytes representing the trigger
     * @param triggers the trigger instances corresponding to triggerBytes
     */
    static void dumpTriggers(StringBuilder sb,
                            byte[][] triggerBytes,
                            List<Trigger> triggers) {

        if ((triggerBytes == null) || triggerBytes.length == 0) {
            return;
        }

        /* Use trigger instances if available, otherwise fallback to
         * using the byte arrays.
         */
        if ((triggers != null) && (triggers.size() != 0)) {
            for (Trigger trigger : triggers) {
                sb.append("<trigger name=\"" + trigger.getName() + "\"" +
                          " database name=\"" +
                          ((trigger.getDatabaseName() == null) ?
                          "null" : trigger.getDatabaseName()) + "\"" +
                          " className=\"" + trigger.getClass().getName() +
                "\">");
            }
        } else {
            /* Use the byte array */
            for (int i=0; i < triggerBytes.length; i++) {
                sb.append("<trigger name=\"" + "?" + i + "\"" +
                          " className=\"" + "?" +
                "\">");
            }
        }
    }
}
