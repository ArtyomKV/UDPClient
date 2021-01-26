package ru.realtrac.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Slf4j
public class Runner implements Runnable {

    private static final int PORT = 7000;
    private static String ADDRESS = "localhost";

    @Override
    public void run() {
        try {

            while (true) {
                byte[] mac = new byte[6];
                mac[0] = 111;
                mac[1] = 112;
                mac[2] = 113;
                mac[3] = 114;
                mac[4] = 115;
                mac[5] = 116;
                byte packetType = 1;
                int id = 1;
                double time = 8989.55;
                long latitude = -77;
                long longitude = -77;

                byte[] idBuf = ByteBuffer.allocate(4).putInt(id).array();
                byte[] timeBuf = ByteBuffer.allocate(8).putDouble(time).array();
                byte[] latitudeBuf = ByteBuffer.allocate(8).putLong(latitude).array();
                byte[] longitudeBuf = ByteBuffer.allocate(8).putLong(longitude).array();

                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName(ADDRESS);

                ByteBuffer sendBuf = ByteBuffer.allocate(1024);
                sendBuf.order(ByteOrder.BIG_ENDIAN);

                sendBuf.put(mac);
                sendBuf.put(packetType);
                sendBuf.put(idBuf);
                sendBuf.put(timeBuf);
                sendBuf.put(latitudeBuf);
                sendBuf.put(longitudeBuf);

                DatagramPacket sendPacket = new DatagramPacket(sendBuf.array(), sendBuf.capacity(), IPAddress, PORT);

                log.info("Sending package to server... mac: {}", Arrays.toString(mac));
                clientSocket.send(sendPacket);

                byte[] receiveBuf = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);

                log.info("Waiting answer from server...");
                clientSocket.setSoTimeout(1000);

                try {
                    clientSocket.receive(receivePacket);
                } catch (SocketTimeoutException e) {
                    log.error("second timeout expired... sending package again...");
                    clientSocket.send(sendPacket);
                    clientSocket.setSoTimeout(100);
                    try {
                        clientSocket.receive(receivePacket);
                    } catch (SocketTimeoutException ex) {
                        log.error("ms_second timeout expired... sending package again...");
                        clientSocket.send(sendPacket);
                    }
                }
                byte [] receivedBuf = receivePacket.getData();
                byte [] macFrom = Arrays.copyOfRange(receivedBuf, 0, 6);
                byte packageType = receivedBuf[6];

                log.info("Mac from server: {}", Arrays.toString(macFrom));
                log.info("Type from server: {}", packageType);

                clientSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
