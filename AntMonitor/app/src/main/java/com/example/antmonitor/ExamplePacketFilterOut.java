/*
 *  This file is part of AntMonitor <https://athinagroup.eng.uci.edu/projects/antmonitor/>.
 *  Copyright (C) 2018 Anastasia Shuba and the UCI Networking Group
 *  <https://athinagroup.eng.uci.edu>, University of California, Irvine.
 *
 *  AntMonitor is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  AntMonitor is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with AntMonitor. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.antmonitor;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.uci.calit2.antmonitor.lib.logging.ConnectionValue;
import edu.uci.calit2.antmonitor.lib.util.AhoCorasickInterface;
import edu.uci.calit2.antmonitor.lib.util.TCPReassemblyInfo;
import edu.uci.calit2.antmonitor.lib.vpn.OutPacketFilter;
import edu.uci.calit2.antmonitor.lib.logging.PacketAnnotation;

/**
 * Example Packet filter that blocks all Google apps and searches for the string "Irvine"
 * in outgoing SSL packets
 *
 * @author Anastasia Shuba
 */
public class ExamplePacketFilterOut extends OutPacketFilter {
    protected Context context;

    private final String TAG = ExamplePacketFilterOut.class.getSimpleName();

    /** Annotation used for blocking packets */
    //private final PacketAnnotation BLOCK_ANNOTATION = new PacketAnnotation(false);

    /** Annotation used for allowing packets */
    private final PacketAnnotation ALLOW_ANNOTATION = new PacketAnnotation(true);

    public ExamplePacketFilterOut(Context cxt) {
        super(cxt);
        TelephonyManager telephonyManager = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);

        // Set deviceId
        //String deviceId = Settings.Secure.getString(cxt.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Set IMEI

        //String imei = telephonyManager.getDeviceId();
        String imei1 = "867241031467865";
        String imei2 = "867241031479985";
        // Set PhoneNumber
        //String phoneNumber = telephonyManager.getLine1Number();
        // Set email
        //Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        //Account[] accounts = AccountManager.get(cxt).getAccounts();



        // Initialize the AhoCorasick interface to search for the string "Irvine"
        //String[] searchStrings = new String[] { "Irvine" };
        String[] searchStrings = new String[] { imei1, imei2 };
        AhoCorasickInterface.getInstance().init(searchStrings);
    }

    @Override
    public PacketAnnotation acceptIPDatagram(ByteBuffer packet) {
        // Search the current packet for the string "Irvine"
        final ArrayList<String> foundStrings = AhoCorasickInterface.getInstance().
                search(packet, packet.limit());
        if (foundStrings == null || foundStrings.isEmpty())
            return DEFAULT_ANNOTATION; // String "Irvine" was not found, allow packet


        ConnectionValue cv = mapDatagramToApp(packet);
        return processFoundStrs(foundStrings, cv, false);
    }

    @Override
    public PacketAnnotation acceptDecryptedSSLPacket(ByteBuffer packet, TCPReassemblyInfo tcpInfo) {
        // We can do the same thing here as in the above function
        final ArrayList<String> foundStrings = AhoCorasickInterface.getInstance().
                search(packet, packet.limit());
        if (foundStrings == null || foundStrings.isEmpty())
            return DEFAULT_ANNOTATION; // String "Irvine" was not found, allow packet

        // Since the packet here does not contain headers, we must use the params function
        // to map and pass the needed info to it
        ConnectionValue cv = mapParamsToApp(tcpInfo.getRemoteIp(), tcpInfo.getSrcPort(),
                tcpInfo.getDestPort());
        return processFoundStrs(foundStrings, cv, true);
    }

    private PacketAnnotation processFoundStrs(ArrayList<String> foundStrings, ConnectionValue cv,
                                              boolean overTLS)
    {
        final int LOOP_SIZE = 2;
        for (int i = 0; i < foundStrings.size(); i += LOOP_SIZE)
        {
            // Each found string is followed by its position in the packet
            Log.d(TAG, "String " + foundStrings.get(i) + " was found at position " +
                    foundStrings.get(i + 1) + " in a packet generated by " + cv.getAppName() +
                    ". TLS used = " + overTLS);
            String filename = "AntMonitorlog.txt";
            this.context = context.getApplicationContext();
            File dir = context.getFilesDir();
            File outputfile = new File(dir, filename);
            String data = foundStrings.get(i) +","+cv.getAppName();
            writeFile(outputfile,  data);
        }

        // Block packet
        //return BLOCK_ANNOTATION;
        //allow packet
        return ALLOW_ANNOTATION;
    }



    private void writeFile(File file, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

