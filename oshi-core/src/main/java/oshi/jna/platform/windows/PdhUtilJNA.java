/**
 * Oshi (https://github.com/oshi/oshi)
 *
 * Copyright (c) 2010 - 2018 The Oshi Project Team
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Maintainers:
 * dblock[at]dblock[dot]org
 * widdis[at]gmail[dot]com
 * enrico.bianchi[at]gmail[dot]com
 *
 * Contributors:
 * https://github.com/oshi/oshi/graphs/contributors
 */
package oshi.jna.platform.windows;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Memory; // NOSONAR
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;

/**
 * This class has already been submitted to the JNA project as PDHUtil.java. It
 * is named differently here to prevent confusion with the same-name class in
 * the oshi.util package. This class should be considered non-API as it WILL be
 * removed when its code is incorporated into the JNA project.
 *
 * @author widdis[at]gmail[dot]com
 */
public abstract class PdhUtilJNA {

    /**
     * Utility method to call Pdh's PdhLookupPerfNameByIndex that allocates the
     * required memory for the szNameBuffer parameter based on the type mapping
     * used, calls to PdhLookupPerfNameByIndex, and returns the received string.
     * 
     * @param szMachineName
     *            Null-terminated string that specifies the name of the computer
     *            where the specified performance object or counter is located.
     *            The computer name can be specified by the DNS name or the IP
     *            address. If NULL, the function uses the local computer.
     * @param dwNameIndex
     *            Index of the performance object or counter.
     * @return Returns the name of the performance object or counter.
     */
    public static String PdhLookupPerfNameByIndex(String szMachineName, int dwNameIndex) {
        int charToBytes = Boolean.getBoolean("w32.ascii") ? 1 : Native.WCHAR_SIZE;

        // Call once to get required buffer size
        DWORDByReference pcchNameBufferSize = new DWORDByReference(new DWORD(0));
        Pdh.INSTANCE.PdhLookupPerfNameByIndex(null, dwNameIndex, null, pcchNameBufferSize);

        // Allocate buffer and call again
        Memory mem = new Memory(pcchNameBufferSize.getValue().intValue() * charToBytes);
        Pdh.INSTANCE.PdhLookupPerfNameByIndex(null, dwNameIndex, mem, pcchNameBufferSize);

        // Convert buffer to Java String
        if (charToBytes == 1) {
            return mem.getString(0);
        } else {
            return mem.getWideString(0);
        }
    }

    /**
     * Utility method to call Pdh's PdhEnumObjectItems that allocates the
     * required memory for the mszCounterList parameter based on the type
     * mapping used, calls to PdhEnumObjectItems, and returns the received lists
     * of strings.
     * 
     * @param szDataSource
     *            String that specifies the name of the log file used to
     *            enumerate the counter and instance names. If NULL, the
     *            function uses the computer specified in the szMachineName
     *            parameter to enumerate the names.
     * @param szMachineName
     *            String that specifies the name of the computer that contains
     *            the counter and instance names that you want to enumerate.
     *            Include the leading slashes in the computer name, for example,
     *            \\computername. If the szDataSource parameter is NULL, you can
     *            set szMachineName to NULL to specify the local computer.
     * @param szObjectName
     *            String that specifies the name of the object whose counter and
     *            instance names you want to enumerate.
     * @param dwDetailLevel
     *            Detail level of the performance items to return. All items
     *            that are of the specified detail level or less will be
     *            returned.
     * @return Returns a List of Strings of the counters for the object.
     */
    public static List<String> PdhEnumObjectItemCounters(String szDataSource, String szMachineName, String szObjectName,
            int dwDetailLevel) {
        int charToBytes = Boolean.getBoolean("w32.ascii") ? 1 : Native.WCHAR_SIZE;

        // Call once to get string lengths
        DWORDByReference pcchCounterListLength = new DWORDByReference(new DWORD(0));
        DWORDByReference pcchInstanceListLength = new DWORDByReference(new DWORD(0));
        Pdh.INSTANCE.PdhEnumObjectItems(szDataSource, szMachineName, szObjectName, null, pcchCounterListLength, null,
                pcchInstanceListLength, dwDetailLevel, 0);

        // Allocate memory and call again to populate strings
        Memory mszCounterList = new Memory(pcchCounterListLength.getValue().intValue() * charToBytes);
        Memory mszInstanceList = new Memory(pcchInstanceListLength.getValue().intValue() * charToBytes);
        Pdh.INSTANCE.PdhEnumObjectItems(szDataSource, szMachineName, szObjectName, mszCounterList,
                pcchCounterListLength, mszInstanceList, pcchInstanceListLength, dwDetailLevel, 0);

        // Fetch counters
        List<String> counters = new ArrayList<String>();
        int offset = 0;
        while (offset < mszCounterList.size()) {
            String s = null;
            if (charToBytes == 1) {
                s = mszCounterList.getString(offset);
            } else {
                s = mszCounterList.getWideString(offset);
            }
            // list ends with double null
            if (s.isEmpty()) {
                break;
            }
            counters.add(s);
            // Increment for string + null terminator
            offset += (s.length() + 1) * charToBytes;
        }

        return counters;
    }

    /**
     * Utility method to call Pdh's PdhEnumObjectItems that allocates the
     * required memory for the mszInstanceList parameters based on the type
     * mapping used, calls to PdhEnumObjectItems, and returns the received lists
     * of strings.
     * 
     * @param szDataSource
     *            String that specifies the name of the log file used to
     *            enumerate the counter and instance names. If NULL, the
     *            function uses the computer specified in the szMachineName
     *            parameter to enumerate the names.
     * @param szMachineName
     *            String that specifies the name of the computer that contains
     *            the counter and instance names that you want to enumerate.
     *            Include the leading slashes in the computer name, for example,
     *            \\computername. If the szDataSource parameter is NULL, you can
     *            set szMachineName to NULL to specify the local computer.
     * @param szObjectName
     *            String that specifies the name of the object whose counter and
     *            instance names you want to enumerate.
     * @param dwDetailLevel
     *            Detail level of the performance items to return. All items
     *            that are of the specified detail level or less will be
     *            returned.
     * @return Returns a Lists of Strings of the instances of the object.
     */
    public static List<String> PdhEnumObjectItemInstances(String szDataSource, String szMachineName,
            String szObjectName, int dwDetailLevel) {
        int charToBytes = Boolean.getBoolean("w32.ascii") ? 1 : Native.WCHAR_SIZE;

        // Call once to get string lengths
        DWORDByReference pcchCounterListLength = new DWORDByReference(new DWORD(0));
        DWORDByReference pcchInstanceListLength = new DWORDByReference(new DWORD(0));
        Pdh.INSTANCE.PdhEnumObjectItems(szDataSource, szMachineName, szObjectName, null, pcchCounterListLength, null,
                pcchInstanceListLength, dwDetailLevel, 0);

        // Allocate memory and call again to populate strings
        Memory mszCounterList = new Memory(pcchCounterListLength.getValue().intValue() * charToBytes);
        Memory mszInstanceList = new Memory(pcchInstanceListLength.getValue().intValue() * charToBytes);
        Pdh.INSTANCE.PdhEnumObjectItems(szDataSource, szMachineName, szObjectName, mszCounterList,
                pcchCounterListLength, mszInstanceList, pcchInstanceListLength, dwDetailLevel, 0);

        List<String> instances = new ArrayList<String>();
        int offset = 0;
        while (offset < mszInstanceList.size()) {
            String s = null;
            if (charToBytes == 1) {
                s = mszInstanceList.getString(offset);
            } else {
                s = mszInstanceList.getWideString(offset);
            }
            // list ends with double null
            if (s.isEmpty()) {
                break;
            }
            instances.add(s);
            // Increment for string + null terminator
            offset += (s.length() + 1) * charToBytes;
        }

        return instances;
    }
}
