package ch.heigvd.dai.networkManager;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.networkTask.ConnectToServerTask;
import ch.heigvd.dai.networkManager.networkTask.NetworkTask;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NetworkManager
{
    private static NetworkManager instance;
    public static NetworkManager getInstance(){return instance;}

    public final int MAX_PLAYERS = 4;

    private boolean isClient = false;
    private boolean isServer = false;

    private Server server = null;
    private Client client = null;

    private String networkId;
    private InetSocketAddress clientSocketAddress;
    private InetSocketAddress serverSocketAddress;

    private Map<String, InetSocketAddress> clients = new HashMap<>();
//╔════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
//║                                          ◆◆◆◆◆◆ CONSTRUCTORS ◆◆◆◆◆◆                                        ║
//╚════════════════════════════════════════════════════════════════════════════════════════════════════════════╝
    private NetworkManager(String networkId)
    {
        this.networkId = networkId;
        instance = this;
        new GameManager();
    }

    public NetworkManager(Server server, String networkId, InetSocketAddress socketAddress)
    {
        this(networkId);
        this.server = server;
        isServer = true;
        serverSocketAddress = socketAddress;
        onPlayerConnection(networkId, socketAddress);
        System.out.println("NetworkManager created as Server: " + isServer);
    }

    public NetworkManager(Client client, String networkId, InetSocketAddress serverSocketAddress)
    {
        this(networkId);
        this.client = client;
        isClient = true;
        this.serverSocketAddress = serverSocketAddress;
        System.out.println("NetworkManager created as Client: " + isClient);
    }
//╔════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
//║                                            ◆◆◆◆◆◆ GETTERS ◆◆◆◆◆◆                                           ║
//╚════════════════════════════════════════════════════════════════════════════════════════════════════════════╝

    public boolean isServer() {return isServer;}

    public boolean isClient() {return isClient;}

    public String getNetworkId() {return networkId;}

    public boolean tryAddPlayer(String key, InetSocketAddress value)
    {
        return clients.putIfAbsent(key, value) == null;
    }

//╔════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
//║                                            ◆◆◆◆◆◆ METHODS ◆◆◆◆◆◆                                           ║
//╚════════════════════════════════════════════════════════════════════════════════════════════════════════════╝

    //╓────────────────────────────────────────────────────────────────────────────────────────────────────────────╖
    //║ ◈◈◈◈◈◈ Client Methods ◈◈◈◈◈◈                                                                               ║
    //╙────────────────────────────────────────────────────────────────────────────────────────────────────────────╜

    public void sendToServer(NetworkTask task)
    {
        if(!isClient) return;
        client.sendPacket(NetworkTask.CreateDatagram(task, serverSocketAddress));
    }

    //╓────────────────────────────────────────────────────────────────────────────────────────────────────────────╖
    //║ ◈◈◈◈◈◈ Server Methods ◈◈◈◈◈◈                                                                               ║
    //╙────────────────────────────────────────────────────────────────────────────────────────────────────────────╜

    public void onPlayerConnection(String key, InetSocketAddress value)
    {
        if(!isServer || clients.size() >= MAX_PLAYERS ||!tryAddPlayer(key, value)) return;
        System.out.println("NetworkManager Server: onPlayerConnection " + key);
        GameManager.getInstance().addPlayer(key);
    }

    public void sendToClients(NetworkTask task)
    {
        if(!isServer) return;
        for (Map.Entry<String, InetSocketAddress> entry : clients.entrySet())
        {
            if(entry.getKey().equals(networkId)) continue;
            InetSocketAddress socketAddress = entry.getValue();
            DatagramPacket packet = NetworkTask.CreateDatagram(task, socketAddress);
            server.sendPacket(packet);
        }
    }

    public void sendToClient(String key, NetworkTask task)
    {
        if(!isServer) return;
        System.out.println("Server: sendToClient packet " + key);
        server.sendPacket(NetworkTask.CreateDatagram(task, clients.get(key)));
    }

    public void sendToClient(int index, NetworkTask task)
    {
        if(!isServer) return;
        String clientKey = GameManager.getInstance().getPlayerKey(index);
        System.out.println("Server: sendToClient packet");
        server.sendPacket(NetworkTask.CreateDatagram(task, clients.get(clientKey)));
    }



    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
