package com.eyeson.socketio;

public class ClientDemo {
//    public static void main(String[] args) throws IOException {
//        System.out.println("客户端启动");
//        Socket client = new Socket();
//        client.connect(new InetSocketAddress("localhost", 9999));
//
//        OutputStream ou = client.getOutputStream();
//        ou.write("这是客户端发来的消息".getBytes());
//        // flush() 则要求立即将缓冲区的数据输出到接收方
//        ou.flush();
//        Thread inThread = new Thread() {
//            @Override
//            public void run() {
//                int itemp;
//                byte[] rbyte = new byte[1024];
//                while (true) {
//                    try {
//                        InputStream in = client.getInputStream();
//                        if (in != null) {
//                            itemp = in.read(rbyte);
//                            System.out.println("读取服务端消息大小: " + itemp);
//                            if (itemp > 0) {
//
//                                System.out.println("服务端传过来的消息: " + new String(rbyte));
//                                // 接收一次服务端信息就退出
//
//                                if (client != null) {
//                                    if (in != null) {
//                                        in.close();
//                                    }
//                                    if (ou != null) {
//                                        ou.close();
//                                    }
//                                    client.close();
//                                    return;
//                                }
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//
//        };
//        inThread.start();
//        // 给服务端发送消息
//
//    }
}
