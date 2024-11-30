package ch.heigvd.dai.mainMenu;

import ch.heigvd.dai.EGameState;
import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.networkManager.NetworkManager;
import ch.heigvd.dai.networkManager.networkTask.ServerMenuReadyStatesTask;
import ch.heigvd.dai.networkManager.networkTask.StartGameTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MainMenu extends JPanel implements ActionListener
{
    private GameManager gameManager;

    private JPanel playersPanel;
    private JPanel buttonsPanel;
    private JButton startButton;
    private JButton readyButton;

    private ArrayList<PlayerCheckBox> players;

    public MainMenu(GameManager gameManager, int width, int height)
    {
        this.gameManager = gameManager;
        this.setFocusable(true);
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.blue);

        initialize();
    }

    private void initialize()
    {
        createPlayersPanel();
        createButtonsPanel();
    }

    private void createPlayersPanel()
    {
        playersPanel = new JPanel();
        playersPanel.setPreferredSize(new Dimension(200, 200));
        playersPanel.setBackground(Color.gray);
        playersPanel.setVisible(true);

        initializePlayerBox();

        this.add(playersPanel, BorderLayout.CENTER);
    }

    private void initializePlayerBox()
    {
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.X_AXIS));
        players = new ArrayList<>();
        int maxPlayer = NetworkManager.getInstance().MAX_PLAYERS;
        for (int i = 0; i < maxPlayer; i++)
        {
            players.add(new PlayerCheckBox("Slot" + i));
            playersPanel.add(players.get(i));
        }
    }

    private void createButtonsPanel()
    {
        System.out.println("createButtonsPanel");
        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setPreferredSize(new Dimension(200, 100));
        buttonsPanel.setBackground(Color.yellow);
        buttonsPanel.add(createStartButton());
        buttonsPanel.add(createReadyButton());
        buttonsPanel.setVisible(true);
        this.add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JButton createButton(String name)
    {
        JButton button = new JButton(name);
        button.setFocusable(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setPreferredSize(new Dimension(500, 50));
        //button.addActionListener(this);
        button.setVisible(true);
        return button;
    }

    private JButton createStartButton()
    {
        startButton = createButton("Start");
        startButton.addActionListener(this);
        return startButton;
    }

    private JButton createReadyButton()
    {
        readyButton = createButton("Ready");
        readyButton.addActionListener(this);
        return readyButton;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startButton)
        {
            System.out.println("Start clicked");
            onStartClicked();
        }
        else if (e.getSource() == readyButton)
        {
            //System.out.println("Ready clicked for index : " + gameManager.getPlayerIndex());
            onReadyClicked();
        }
    }

    //Server Only
    private void onStartClicked()
    {
        if(!NetworkManager.getInstance().isServer()) return;
        int readyCount = 0;
        for (PlayerCheckBox player : players) {
            readyCount += player.isReady() ? 1 : 0;
        }
        if(readyCount != gameManager.numPlayer()) return;

        gameManager.changeGameState(EGameState.GAME);
    }

    private void onReadyClicked()
    {
        boolean currentState = players.get(gameManager.getPlayerIndex()).isReady();
        if(NetworkManager.getInstance().isServer())
        {
            serverSetReady(!currentState);
        }
        else
        {
            clientSetReady(!currentState);
        }
    }

    // States Manipulation
    public boolean[] getReadyStates()
    {
        boolean[] states = new boolean[players.size()];
        for (int i = 0; i < players.size(); i++)
        {
            states[i] = players.get(i).isReady();
        }
        return states;
    }

    //Server to Clients Only PAS de packet a envoyer
    //on ne fait qu'appliquer l'état du serveur!!
    public void setReadyStates(boolean[] states)
    {
        for (int i = 0; i < players.size(); i++)
        {
            players.get(i).setReady(states[i]);
        }
    }

    //Commande au serveur un changement
    public void clientSetReady(boolean state)
    {
        if(NetworkManager.getInstance().isServer()) return;
        ServerMenuReadyStatesTask task = new ServerMenuReadyStatesTask(new boolean[]{state});
        NetworkManager.getInstance().sendToServer(task);
    }

    //le serveur se met à jour puis transmet aux clients
    public void serverSetReady(boolean state)
    {
        if(!NetworkManager.getInstance().isServer()) return;
        //int index = gameManager.getPlayerIndex(playerId);
        players.get(gameManager.getPlayerIndex()).setReady(state);
        ServerMenuReadyStatesTask task = new ServerMenuReadyStatesTask(getReadyStates());
        NetworkManager.getInstance().sendToClients(task);
    }

    //Réception d'une commande client
    //server met à jour puis transmet
    public void serverSetReadyAt(String playerId, boolean state)
    {
        if(NetworkManager.getInstance().isServer())
        {
            int index = gameManager.getPlayerIndex(playerId);
            players.get(index).setReady(state);
            ServerMenuReadyStatesTask task = new ServerMenuReadyStatesTask(getReadyStates());
            NetworkManager.getInstance().sendToClients(task);
        }
    }
}
