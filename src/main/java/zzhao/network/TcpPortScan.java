package zzhao.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
/**
 * 用建立链接的方式来测试端口
 * @author zz
 *
 */
public class TcpPortScan {
    public static void main(String[] args) throws IOException, InterruptedException {
        PortSelector portSelector = new PortSelector();
        Thread thread = new Thread(portSelector);
        thread.start();

        for (int port = 6010; port < 10000; port++) {
            InetSocketAddress address = new InetSocketAddress("localhost", port);
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.connect(address);
            socket.register(portSelector.getSelector(), SelectionKey.OP_CONNECT, address);
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
            while (!Thread.interrupted()) {
                try {
                    selector.select(100);

                    for (Iterator<SelectionKey> itor = selector.selectedKeys().iterator(); itor.hasNext();) {
                        SelectionKey key = (SelectionKey) itor.next();
                        itor.remove();
                        if (key.isConnectable()) {
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            InetSocketAddress address = (InetSocketAddress) key.attachment();
                            try {
                                socketChannel.finishConnect();
                                System.out.println(address.toString() + " conneted!");
                            } catch (Exception e) {
                                System.out.println(address.toString() + " conneted failed! " + e.getMessage());
                            }
                            socketChannel.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
