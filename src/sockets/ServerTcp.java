/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 *
 * @author 1
 */
public class ServerTcp extends Observable {

    public static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger("LogServer");
    public static final String END_OK = "\n\r"; //Символ конца сообщения
    public static final String STR_ANSWER = "OK"; // Фраза ответа
    public static InetAddress INET_ADDRESS;
    private ServerSocket socket;
    private int port;
// A pre-allocated buffer for encrypting data
    private final ByteBuffer buffer = ByteBuffer.allocate(Short.MAX_VALUE);

    public ServerTcp(int port) throws IOException {
        this.port = port;
        //    this.socket = new ServerSocket(0);
        //this.port = socket.getLocalPort();

    }

    public int getPort() {
        return port;
    }

    public void startServer() {

        Runnable r = new StartClientGo();
        Thread t = new Thread(r, "ServerSocket");
        t.start();

    }

    class StartClientGo implements Runnable {

        @Override
        public void run() {
            start();
        }
    }

    private void start() {

        try {
            // Instead of creating a ServerSocket,
            // create a ServerSocketChannel
            ServerSocketChannel ssc = ServerSocketChannel.open();

            // Set it to non-blocking, so we can use select
            ssc.configureBlocking(false);

            // Get the Socket connected to this channel, and bind it
            // to the listening port
            ServerSocket ss = ssc.socket();
            InetSocketAddress isa = new InetSocketAddress(port);
            ss.bind(isa);

            // Create a new Selector for selecting
            Selector selector = Selector.open();

            // Register the ServerSocketChannel, so we can
            // listen for incoming connections
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            LOGGER.info("Listening on port " + port);

            while (true) {
                // See if we've had any activity -- either
                // an incoming connection, or incoming data on an
                // existing connection
                int num = selector.select();

                // If we don't have any activity, loop around and wait
                // again
                if (num == 0) {
                    continue;
                }

                // Get the keys corresponding to the activity
                // that has been detected, and process them
                // one by one
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();
                while (it.hasNext()) {
                    // Get a key representing one of bits of I/O
                    // activity
                    SelectionKey key = (SelectionKey) it.next();

                    // What kind of activity is it?
                    if ((key.readyOps() & SelectionKey.OP_ACCEPT)
                            == SelectionKey.OP_ACCEPT) {

                        LOGGER.info("acc");

                        // It's an incoming connection.
                        // Register this socket with the Selector
                        // so we can listen for input on it
                        Socket s = ss.accept();
                        LOGGER.info("Got connection from " + s);

                        // Make sure to make it non-blocking, so we can
                        // use a selector on it.
                        SocketChannel sc = s.getChannel();
                        sc.configureBlocking(false);

                        // Register it with the selector, for reading
                        sc.register(selector, SelectionKey.OP_READ);
                    } else if ((key.readyOps() & SelectionKey.OP_READ)
                            == SelectionKey.OP_READ) {

                        SocketChannel sc = null;

                        try {

                            // It's incoming data on a connection, so
                            // process it
                            sc = (SocketChannel) key.channel();
                            boolean ok = processInput(sc);

                            // If the connection is dead, then remove it
                            // from the selector and close it
                            if (!ok) {
                                key.cancel();

                                Socket s = null;
                                try {
                                    s = sc.socket();
                                    s.close();
                                } catch (IOException ie) {

                                    LOGGER.error("Error closing socket " + s + ": " + ie);
                                }
                            }

                        } catch (IOException ie) {

                            // On exception, remove this channel from the selector
                            key.cancel();

                            try {
                                sc.close();
                            } catch (IOException ie2) {
                                LOGGER.error(ie2);
                            }

                            LOGGER.info("Closed " + sc);
                        }
                    }
                }

                // We remove the selected keys, because we've dealt
                // with them.
                keys.clear();
            }
        } catch (IOException ie) {
            LOGGER.error(ie);
        }
    }

    // Do some cheesy encryption on the incoming data,
    // and send it back out
    private boolean processInput(SocketChannel sc) throws IOException {

        buffer.clear();
        sc.read(buffer);
        buffer.flip();

        // If no data, close the connection
        if (buffer.limit() == 0) {
            return false;
        }

        byte[] answer = Arrays.copyOf(buffer.array(), buffer.limit());

        String sAnswer = new String(answer);

        byte[] answerb;
        int id = 0;
        Object result = null;

        JSONObject sonAnswer;
        sonAnswer = new JSONObject();
        HashMap<String, Object> hmParam = null;

        try {

            JSONObject jsono = JSONObject.fromObject(sAnswer);
            hmParam = (HashMap<String, Object>) JSONObject.toBean(jsono, Map.class);

            id = (int) hmParam.get("#ID");

            Script script = new Script();
            result = script.evalScript(id, hmParam);

            if (result instanceof Map) {
                if (((Map) result).containsKey("observes")) {

                    notifyObservers(result);
                }
            }

        } catch (Exception e) {

            String msg = e.getMessage();

            result = new HashMap<String, Object>();

            ((Map) result).put("error", msg);
        }
        sonAnswer = JSONObject.fromObject(result);

        String jSon = sonAnswer.toString();

        answerb = jSon.getBytes();

        //  if (alSend == null) {
        //    return true;
        //}
        buffer.clear();
        buffer.put(answerb);
        buffer.flip();

        sc.write(buffer);

        LOGGER.info("Processed " + buffer.limit() + " from " + sc);
        return true;
    }

    class ClientGo implements Runnable {

        Socket sClient;

        public ClientGo(Socket s) {
            sClient = s;

        }

        @Override
        public void run() {
            InputStream is = null;
            OutputStream os = null;

            try {
                is = sClient.getInputStream();
                os = sClient.getOutputStream();

                PrintWriter writer = new PrintWriter(os, true);

                Scanner scanner = new Scanner(is);
                String msg = "";

                boolean done = false;

                while (!done && scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    msg = msg + line;

                    if (msg.endsWith(END_OK)) {
                        done = true;
                    }

                }

                if (msg.contains("constants.caesar")) {
                }

                setChanged();
                notifyObservers(msg);

                writer.println(STR_ANSWER + END_OK);

            } catch (IOException ex) {

                LOGGER.error(ex.getMessage(), ex);

            } finally {
                try {
                    is.close();
                    os.close();
                    sClient.close();

                } catch (IOException ex) {

                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

    class ClientByteGo implements Runnable {

        Socket sClient;

        public ClientByteGo(Socket s) {
            sClient = s;

        }

        @Override
        public void run() {
            InputStream is = null;
            OutputStream os = null;

            try {
                is = sClient.getInputStream();
                os = sClient.getOutputStream();

                PrintWriter writer = new PrintWriter(os, true);

                Scanner scanner = new Scanner(is);
                String msg = "";

                boolean done = false;

                while (!done && scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    msg = msg + line;

                    if (msg.endsWith(END_OK)) {
                        done = true;
                    }

                }

                setChanged();
                notifyObservers(msg);

                writer.println(STR_ANSWER + END_OK);

            } catch (IOException ex) {

                LOGGER.error(ex.getMessage(), ex);

            } finally {
                try {
                    is.close();
                    os.close();
                    sClient.close();

                } catch (IOException ex) {

                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }
}
