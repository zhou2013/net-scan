/*
 * @(#) UDPPortScan.java 2014-7-4
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
package zzhao.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 *  喵的~~~一直不成功，不知道为嘛...
 * @author hzzhaozhou
 * @version 2014-7-4
 */
public class UDPPortScan {
    public static void main(String[] args) throws IOException, InterruptedException {
        PortSelector portSelector = new PortSelector();
        Thread thread = new Thread(portSelector);
        thread.start();

        DatagramChannel channel = DatagramChannel.open();
        // channel.configureBlocking(false);
        // channel.connect(new InetSocketAddress("localhost", 1234));
        // channel.register(portSelector.getSelector(), SelectionKey.OP_READ);

        ByteBuffer buf = ByteBuffer.allocate(200);
        buf.put("test".getBytes());
        buf.flip();
        int count = channel.send(buf, new InetSocketAddress("localhost", 38358));
        buf.clear();
        SocketAddress address = channel.receive(buf);
        System.out.println(buf.toString());

        int startPort = 6010;
        for (int port = 0; port < 20; port++) {
            // InetSocketAddress address = new InetSocketAddress("localhost", startPort + port);

        }

        Thread.sleep(300000);
    }

    public static class PortSocket {

    }

    public static class PortSelector implements Runnable {

        private Selector selector;

        public PortSelector() throws IOException {
            selector = Selector.open();
        }

        public Selector getSelector() {
            return selector;
        }

        public void run() {
            ByteBuffer buf = ByteBuffer.allocate(200);
            buf.clear();

            while (!Thread.interrupted()) {
                try {
                    selector.select(100);

                    for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();) {
                        SelectionKey key = (SelectionKey) itor.next();
                        DatagramChannel channel = (DatagramChannel) key.channel();
                        itor.remove();
                        if (key.isReadable()) {
                            channel.receive(buf);
                            System.out.println(buf.toString());
                            buf.clear();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
