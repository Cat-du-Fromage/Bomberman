package ch.heigvd.dai.networkManager.networkTask;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.NetworkManager;

import java.io.Serializable;

public class ServerMenuReadyStatesTask extends NetworkTask implements Serializable
{
    boolean[] states;

    public ServerMenuReadyStatesTask(boolean[] states) {
        super();
        this.states = states;
    }

    @Override
    public void execute() {
        if(NetworkManager.getInstance().isServer())
        {
            //int index = GameManager.getInstance().getPlayerIndex(senderId);
            GameManager.getInstance().getMainMenu().serverSetReadyAt(senderId, states[0]);
        }
        else if(NetworkManager.getInstance().isClient())
        {
            GameManager.getInstance().getMainMenu().setReadyStates(states);
        }
    }
}
