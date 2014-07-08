/*
 * @(#) TcpPackageSender.java 2014-7-4
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
package zzhao.network;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author hzzhaozhou
 * @version 2014-7-4
 */
public class TcpPackageSender {
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

        int index = Integer.parseInt(args[0]);

    }
}
