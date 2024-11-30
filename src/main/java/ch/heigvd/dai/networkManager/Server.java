package ch.heigvd.dai.networkManager;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.Date;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.networkTask.*;
import picocli.CommandLine;

@CommandLine.Command(
        name = "server",
        description = "Start the server application using the request-reply messaging pattern for the clients.")
public class Server implements Callable<Integer>
{
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<DatagramPacket> clientCommands = new ConcurrentLinkedQueue<>();

    private ConcurrentLinkedQueue<DatagramPacket> packetsToSend = new ConcurrentLinkedQueue<>();

    @CommandLine.Option(
            names = {"-M", "--multicast-address"},
            description = "server address to use for the clients (default: ${DEFAULT-VALUE}).",
            defaultValue = "localhost")
    //defaultValue = "230.1.2.3") Ne fonctionne pas avec ça! a tester (192.168.1.10)
    protected String serverAddress;

    @CommandLine.Option(
            names = {"-E", "--clients-port"},
            description = "Port to use for the clients (default: ${DEFAULT-VALUE}).",
            defaultValue = "7337")
    protected int serverPort;

    @Override
    public Integer call() {
        try (ExecutorService executorService = Executors.newFixedThreadPool(3))
        {
            InetAddress inetAddress = InetAddress.getByName(serverAddress);
            String hostKey = inetAddress.toString() + ":" + serverPort;
            InetSocketAddress value = new InetSocketAddress(inetAddress, serverPort);
            //new GameManager();
            new NetworkManager(this, hostKey, value);
            socket = new DatagramSocket(serverPort); // Unique socket shared between listener and processor
            System.out.println("[Server] Listening and processing on port " + serverPort);

            executorService.submit(this::serverListener);
            executorService.submit(this::serverProcess);
            executorService.submit(this::serverSendPackets);
            executorService.shutdown();
        } catch (Exception e) {
            System.out.println("[Server] call Exception: " + e);
            return 1;
        }
        return 0;
    }

    public Integer serverListener()
    {
        try
        {
            System.out.println("[Server] Listening for unicast messages on port " + serverPort + "...");
            while (!socket.isClosed())
            {
                // Create a buffer for the incoming request
                byte[] requestBuffer = new byte[1024];

                // Create a packet for the incoming request
                DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                // Receive the packet - this is a blocking call
                socket.receive(requestPacket);
                System.err.println("[Server] get packet: from port = " + requestPacket.getAddress() + ":" + requestPacket.getPort());
                //A partir de là, on enregistre les changements
                clientCommands.add(requestPacket);
            }
        } catch (Exception e) {
            System.err.println("[Server] An error occurred: " + e.getMessage());
        }
        return 0;
    }

    public Integer serverProcess()
    {
        try
        {
            System.out.println("[Server] Processing for unicast messages on port " + serverPort + "...");

            while (!socket.isClosed())
            {
                if(clientCommands.isEmpty()) continue;
                while (!clientCommands.isEmpty())
                {
                    DatagramPacket command = clientCommands.poll();
                    NetworkTask task = NetworkTask.deserialize(command);
                    task.execute();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("[Server] An error occurred: " + e.getMessage());
        }
        return 0;
    }

    private Integer serverSendPackets()
    {
        try
        {
            System.out.println("[Server] Sending packets on port " + serverPort);
            while (!socket.isClosed())
            {
                if(packetsToSend.isEmpty()) continue;
                while (!packetsToSend.isEmpty()) {
                    DatagramPacket responsePacket = packetsToSend.poll();
                    socket.send(responsePacket);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("[Server] An error occurred: " + e.getMessage());
        }
        return 0;
    }

    public void sendPacket(DatagramPacket packet)
    {
        packetsToSend.add(packet);
    }
}