package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.NetworkManager;

import java.awt.*;
import java.io.Serializable;

public class PlayersPositionSyncTask extends NetworkTask implements Serializable
{
    private Point[] positions;

    public PlayersPositionSyncTask(Point[] positions)
    {
        this.positions = positions;
    }

    @Override
    public void execute()
    {
        if(NetworkManager.getInstance().isClient())
        {
            GameManager.getInstance().getGameScene().clientSetPlayersPosition(positions);
        }
    }
}
