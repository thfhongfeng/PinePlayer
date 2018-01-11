package com.pine.player.service;

import android.text.TextUtils;
import android.util.Log;

import com.pine.player.decrytor.IPineMediaDecryptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tanghongfeng on 2017/8/18.
 */

public class PineMediaServerThread implements Runnable {

    private final static String TAG = "PineMediaServerThread";

    private static final int TIME_OUT = 0;
    private static final int BUFFER_SIZE = 1024 * 1024;

    private Selector mSelector;
    private AtomicBoolean mIsStop;
    private FileChannel mFileChannel;
    private long mRange;
    private String mPath;
    private String mPrePath;
    private AtomicBoolean mIsNeedReponse;
    private ServerSocketChannel mServerSocketChannel;
    private IPineMediaDecryptor mPlayerDecryptor;

    public PineMediaServerThread(int port) {
        Log.d(TAG, "construct");
        try {
            mIsStop = new AtomicBoolean(false);
            mIsNeedReponse = new AtomicBoolean(false);

            mSelector = Selector.open();
            mServerSocketChannel = ServerSocketChannel.open();
            mServerSocketChannel.configureBlocking(false);
            mServerSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPlayerDecryptor(IPineMediaDecryptor pinePlayerDecryptor) {
        mPlayerDecryptor = pinePlayerDecryptor;
    }

    public void release() {
        Log.d(TAG, "release");
        mIsStop.set(true);
        if (null != mSelector) {
            try {
                if (mFileChannel != null) {
                    mFileChannel.close();
                    mFileChannel = null;
                }
                // release时一定要close掉打开的ServerSocketChannel，否则再次启动时，会因为上次的
                // ServerSocketChannel没有关闭（端口也就还在使用中）而使得新的ServerSocketChannel
                // 无法正常绑定指定端口（端口被占用）。
                if (mServerSocketChannel != null) {
                    mServerSocketChannel.close();
                    mServerSocketChannel = null;
                }
                mSelector.close();
                mSelector = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        while (!mIsStop.get() && mSelector != null) {
            try {
                mSelector.select(TIME_OUT);
                Iterator<SelectionKey> keys = mSelector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    try {
                        handleClientRequest(key);
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                        if (null != key) {
                            key.cancel();
                        }
                        if (mFileChannel != null) {
                            mFileChannel.close();
                            mFileChannel = null;
                        }
                    }
                    keys.remove();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        release();
    }

    private void handleClientRequest(SelectionKey key) throws IOException {
        Log.d(TAG, "handleClientRequest key:" + key);
        if (key.isValid()) {
            // 处理新请求
            if (key.isAcceptable()) {
                // 接受新连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel client = ssc.accept();
                client.configureBlocking(false);
                // 将该连接加入selector
                client.register(mSelector, SelectionKey.OP_READ);
            } // accept end

            if (key.isReadable()) {
                String content = readFromClient(key);
                if (isHttpGetRequest(content)) {
                    parseHeads(content);
                    mIsNeedReponse.set(true);
                    key.channel().register(mSelector, SelectionKey.OP_WRITE);
                }
            } // read end

            if (key.isWritable()) {
                doWrite(key);
            }
        }
    }

    private String readFromClient(SelectionKey key) throws IOException {
        // read data from client
        StringBuffer result = new StringBuffer();
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (true) {
            buffer.clear();
            int readBytes = sc.read(buffer);
            if (0 == readBytes) break;
            if (-1 == readBytes) {
                // close connect with client
                key.cancel();
                sc.close();
                break;
            }
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            result.append(new String(bytes));
        }
        return result.toString();
    }

    private boolean isHttpGetRequest(String request) {
        return !TextUtils.isEmpty(request) && request.contains("GET");
    }

    private void parseHeads(String request) {
        mPrePath = mPath;
        mRange = 0;
        mPath = null;
        if (!TextUtils.isEmpty(request)) {
            String[] heads = request.split("\r\n");
            for (String head : heads) {
                if (head.contains("Range")) {
                    final int indexOfEqual = head.lastIndexOf("=");
                    final int indexOfMinus = head.lastIndexOf("-");
                    mRange = Long.valueOf(head.substring(indexOfEqual + 1, indexOfMinus));
                }
                if (head.contains("Path")) {
                    final int index = head.indexOf(":");
                    mPath = String.valueOf(head.substring(index + 2));
                }
            }
        }
    }

    private void doWrite(SelectionKey key) throws IOException {
        openFileChannel();
        writeHttpResponse(key, mFileChannel.size(), mRange);
        writeFileContent(key, mFileChannel, mRange);
    }

    private void openFileChannel() throws FileNotFoundException {
        if (mFileChannel != null && !mPath.equals(mPrePath)) {
            try {
                mFileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileChannel = null;
        }
        if (null == mFileChannel) {
            File file = new File(mPath);
            mFileChannel = new RandomAccessFile(file, "r").getChannel();
        }
    }

    private void writeHttpResponse(SelectionKey key, long size, long range) throws IOException {
        if (!mIsNeedReponse.get()) {
            return;
        }
        SocketChannel channel = (SocketChannel) key.channel();
        mIsNeedReponse.set(false);
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 206 Partial Content\r\n");
        sb.append("Content-Type: video/mp4\r\n");
        sb.append("Connection: Keep-Alive\r\n");
        sb.append("Accept-Ranges: bytes\r\n");
        sb.append("Content-Length: " + (size - range) + "\r\n");
        sb.append("Content-Range: bytes " + range + "-" + (size - 1) + "/" + size + "\r\n");
        sb.append("\r\n");


        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.clear();
        buffer.put(sb.toString().getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();
    }

    private void writeFileContent(SelectionKey key, FileChannel fileChannel, long range) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.clear();
        fileChannel.position(range);
        while (true) {
            if (buffer.hasRemaining()) {
                long startPos = fileChannel.position();
                int bytes = fileChannel.read(buffer);
                if (mPlayerDecryptor != null) {
                    mPlayerDecryptor.decrypt(buffer, startPos, bytes);
                }
                if (-1 == bytes) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        sc.write(buffer);
                    }
                    buffer.clear();

                    // close connect with client
                    fileChannel.close();
                    mFileChannel = null;
                    key.cancel();
                    sc.close();
                    break;
                }
            } else {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    sc.write(buffer);
                }
                buffer.clear();
            }
        }
    }
}
