package com.socketio;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ServerDemo {
    public static Set<Socket> clients = new HashSet<Socket>();
    public static boolean close = true;
    public static int count = 1;

//    public static void main(String[] args) throws IOException {
//        System.out.println("服务端启动");
//        ServerSocket server = new ServerSocket();
//        server.bind(new InetSocketAddress("localhost", 9999));
//        // 等待客户端连接
//        while (close) {
//            Socket client = server.accept();
//            System.out.println(count + "个客户端连接成功");
//            count++;
//            clients.add(client);
//            InputStream in = client.getInputStream();
//            byte[] rbyte = new byte[1024];
//            // 创建匿名线程
//            Thread thread = new Thread() {
//                int itemp = 0;
//
//                @Override
//                public void run() {
//                    try {
//                        itemp = in.read(rbyte);
//                        System.out.println("读取客户端消息大小: " + itemp);
//                        if (itemp > 0) {
//                            System.out.println("客户端消息: " + new String(rbyte));
//                            // 将连接上来的客户端信息转发给其他客户端
//                            for (Socket c : clients) {
//
//                                if (!client.equals(c)) {
//                                    boolean btemp = c.isClosed();
//                                    System.out.println("当前连接是否以关闭: " + btemp);
//                                    btemp = c.isConnected();
//                                    System.out.println("当前连接是否成功: " + btemp);
//                                    if (!c.isClosed() && c.isConnected()) {
//                                        try {
//                                            // 发送前验证该连接是否有效
//                                            OutputStream ou = c.getOutputStream();
//                                            ou.write((client.getInetAddress().toString() + "连接上服务端").getBytes());
//                                            // flush() 则要求立即将缓冲区的数据输出到接收方
//                                            ou.flush();
//                                        } catch (IOException e1) {
//                                            System.out.println("关闭无效连接");
//                                            c.close();
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            };
//            thread.start();
//        }
//        System.out.println("服务端退出");
//    }

}
