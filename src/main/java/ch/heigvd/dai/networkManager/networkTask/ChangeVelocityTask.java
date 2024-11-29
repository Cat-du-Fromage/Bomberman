package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.NetworkManager;

import java.awt.*;
import java.io.Serializable;

public class ChangeVelocityTask extends NetworkTask implements Serializable
{
    private Point velocity;

    public ChangeVelocityTask(Point velocity)
    {
        this.velocity = velocity;
    }

    @Override
    public void execute()
    {
        if(NetworkManager.getInstance().isServer())
        {
            int playerIndex = GameManager.getInstance().getPlayerIndex(senderId);
            GameManager.getInstance().getGameScene().Players()[playerIndex].setVelocity(velocity);
        }
    }
}
