package ch.heigvd.dai.networkManager;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

import ch.heigvd.dai.networkManager.networkTask.*;
import picocli.CommandLine;

@CommandLine.Command(
        name = "client",
        description = "Start the client part of the network application using the fire-and-forget messaging pattern.")
public class Client implements Callable<Integer>
{
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<DatagramPacket> serverCommands = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<DatagramPacket> packetsToSend = new ConcurrentLinkedQueue<>();

    @CommandLine.Option(
            names = {"-C", "--client-address"},
            description = "client address to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "localhost")
    //defaultValue = "230.1.2.3") // ne fonctionne pas avec ça (a tester: 192.168.1.20 / 30)
    protected String clientAddress;

    @CommandLine.Option(
            names = {"-S", "--server-address"},
            description = "Server address to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "localhost")
    //defaultValue = "230.1.2.3") // ne fonctionne pas avec ça (a tester: 192.168.1.20 / 30)
    protected String serverAddress;

    @CommandLine.Option(
            names = {"-P", "--port"},
            description = "Port to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "7337")
    protected int port;
    /*
        @CommandLine.Option(
                names = {"-L", "--listening-port"},
                description = "Listening Port to use (default: ${DEFAULT-VALUE}).",
                defaultValue = "7347")
                */
    protected int clientPort;

    @Override
    public Integer call()
    {
        //new GameManager();
        try(ExecutorService executorService = Executors.newFixedThreadPool(3);)
        {
            socket = new DatagramSocket();
            clientPort = socket.getLocalPort();

            InetAddress clientAddress = InetAddress.getByName(serverAddress);
            String clientKey = clientAddress.toString() + ":" + clientPort;

            InetSocketAddress serverSocketAddress = new InetSocketAddress(serverAddress, port);
            new NetworkManager(this, clientKey, serverSocketAddress);
            //System.out.println("[Client] Before connection networkId = " + NetworkManager.getInstance().getNetworkId());
            // Initialiser une seule socket partagée

            System.out.println("[Client] Started on port " + clientAddress.toString() + " : " + clientPort);

            // Connecter au serveur immédiatement
            connectToServer();

            // Utiliser un pool de threads pour écouter et traiter

            executorService.submit(this::clientListener);
            executorService.submit(this::clientProcess);
            executorService.submit(this::clientSendPackets);
            // Garder les threads actifs
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        }
        catch (Exception e)
        {
            System.err.println("[Client] An error occurred: " + e.getMessage());
            return 1;
        }
        finally
        {
            if (socket != null && !socket.isClosed())
            {
                socket.close();
            }
        }
        return 0;
    }

    public Integer clientListener()
    {
        try
        {
            System.out.println("[Client] Listening for unicast messages on port " + clientPort + "...");

            while (!socket.isClosed())
            {
                // Create a buffer for the incoming request
                byte[] requestBuffer = new byte[4096];

                // Create a packet for the incoming request
                DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length);

                // Receive the packet - this is a blocking call
                socket.receive(requestPacket);
                System.out.println("[Client] Received serverCommands!");
                serverCommands.add(requestPacket);
            }
        }
        catch (Exception e)
        {
            System.err.println("[Client] (clientListener) An error occurred: " + e.getMessage());
        }
        return 0;
    }

    public Integer clientProcess()
    {
        try
        {
            System.out.println("[Client] Processing for unicast messages on port " + port + "...");
            while (!socket.isClosed())
            {
                if(serverCommands.isEmpty()) continue;
                while (!serverCommands.isEmpty())
                {

                    DatagramPacket command = serverCommands.poll();
                    NetworkTask task = NetworkTask.deserialize(command);
                    System.out.println("[Client] Process serverCommands : " + task.toString());

                    task.execute();
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("[Client] (clientProcess) An error occurred: " + e.getMessage());
        }
        return 0;
    }

    private Integer clientSendPackets()
    {
        try //(DatagramSocket socket = new DatagramSocket(serverPort))
        {
            System.out.println("[Client] Sending packets on port " + clientPort + "; to: " + port);

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
            System.err.println("[Client] An error occurred: " + e.getMessage());
        }
        return 0;
    }

    private void connectToServer() {
        try
        {
            InetAddress serverAddress = InetAddress.getByName(this.serverAddress);
            String clientKey = serverAddress.toString() + ":" + clientPort;
            InetSocketAddress clientSocketAddress = new InetSocketAddress(serverAddress, clientPort);
            ConnectToServerTask connectionTask = new ConnectToServerTask(clientKey, clientSocketAddress);

            // Sérialiser l'objet en un tableau d'octets
            //byte[] buffer =  NetworkTask.Create(connectionTask);
            DatagramPacket packet = NetworkTask.CreateDatagram(connectionTask, serverAddress, port);
            //DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
            socket.send(packet);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sendPacket(DatagramPacket packet)
    {
        packetsToSend.add(packet);
    }
}
