package com.augustl.jeromqissue;

import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQQueue;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ExampleText {
    @Test
    public void demonstrateIssue() {
        for (int i = 0; i < 50; i++) {
            performTest();
        }
    }

    private void performTest() {
        Context ctx = ZMQ.context(1);
        Socket recvMsgSock = ctx.socket(ZMQ.PULL);
        recvMsgSock.bind("tcp://*:5115");
        Socket processMsgSock = ctx.socket(ZMQ.PUSH);
        processMsgSock.bind("inproc://process-msg");

        List<Socket> workerSocks = new ArrayList<Socket>();
        for (int i = 0; i < 5; i++) {
            Socket workerSock = ctx.socket(ZMQ.PULL);
            workerSock.connect("inproc://process-msg");
            workerSocks.add(workerSock);
        }

        Thread proxyThr = new Thread(new ZMQQueue(ctx, recvMsgSock, processMsgSock));
        proxyThr.setName("Proxy thr");
        proxyThr.start();

        for (final Socket workerSock : workerSocks) {
            Thread workerThr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            byte[] msg = workerSock.recv();
                            // Process the msg!
                        }
                    } catch (Exception e) {

                    }
                }
            });
            workerThr.setName("A worker thread");
            workerThr.start();
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Closing now");


        recvMsgSock.close();
        processMsgSock.close();

        for (Socket workerSock : workerSocks) {
            workerSock.close();
        }

        ctx.term();
        System.out.println("Successfully closed");
    }
}
