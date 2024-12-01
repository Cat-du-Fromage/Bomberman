package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.networkManager.NetworkManager;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public abstract class NetworkTask implements Serializable
{
    String senderId;

    public NetworkTask()
    {
        //System.out.println("senderId = " + senderId);
        this.senderId = NetworkManager.getInstance().getNetworkId();
    }

    public abstract void execute();

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    public static byte[] Create(NetworkTask request)
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try(ObjectOutputStream objectStream = new ObjectOutputStream(byteStream))
        {
            objectStream.writeObject(request);
            objectStream.flush();
            return byteStream.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static DatagramPacket CreateDatagram(NetworkTask request, InetSocketAddress socketAddress)
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try(ObjectOutputStream objectStream = new ObjectOutputStream(byteStream))
        {
            objectStream.writeObject(request);
            objectStream.flush();
            byte[] buffer = byteStream.toByteArray();
            //System.out.println("DatagramPacket CreateDatagram buffer.length = " + buffer.length);
            return new DatagramPacket(buffer, buffer.length, socketAddress.getAddress(), socketAddress.getPort());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static DatagramPacket CreateDatagram(NetworkTask request, InetAddress destAddress, int port)
    {
        InetSocketAddress socketAddress = new InetSocketAddress(destAddress, port);
        return CreateDatagram(request, socketAddress);
    }

    public static NetworkTask deserialize(DatagramPacket packet)
    {
        // Désérialiser l'objet
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
             ObjectInputStream objectStream = new ObjectInputStream(byteStream))
        {
            return (NetworkTask) objectStream.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}

