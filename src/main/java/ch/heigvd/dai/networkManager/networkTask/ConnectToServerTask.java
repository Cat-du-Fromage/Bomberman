package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.NetworkManager;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class ConnectToServerTask extends NetworkTask
{
    public int playerIndex;
    public String key;
    public InetSocketAddress value;

    public ConnectToServerTask(String key, InetSocketAddress value) {
        super();
        this.key = key;
        this.value = value;
    }

    public ConnectToServerTask(int index) {
        super();
        playerIndex = index;
    }

    @Override
    public void execute()
    {
        if(NetworkManager.getInstance().isServer())
        {
            boolean accept = NetworkManager.getInstance().tryAddPlayer(key, value);
            if(!accept)
            {
                System.out.println("Connection to Server failed!");
            }
            else
            {
                System.out.println("ConnectToServerTask senderid = " + senderId + " key = " + key);
                GameManager.getInstance().onPlayerConnection(senderId);
            }
        }
        else
        {
            System.out.println("Client received his index in GAME: " + playerIndex);
            GameManager.getInstance().setPlayerIndex(playerIndex);
        }
    }
}
