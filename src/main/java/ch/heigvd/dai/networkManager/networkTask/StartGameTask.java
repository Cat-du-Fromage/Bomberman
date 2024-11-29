package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.EGameState;
import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.Player;
import ch.heigvd.dai.networkManager.NetworkManager;
import ch.heigvd.dai.terrain.TileType;

import java.awt.*;
import java.io.Serializable;

public class StartGameTask extends NetworkTask implements Serializable
{
    private int numPlayers;
    private byte[] tileTypes;

    public StartGameTask(byte[] tileTypes, int numPlayers)
    {
        this.numPlayers = numPlayers;
        this.tileTypes = tileTypes;
    }

    @Override
    public void execute()
    {
        if(NetworkManager.getInstance().isClient())
        {
            GameManager.getInstance().clientEnterGameScene(tileTypes, numPlayers);
        }
    }
}
