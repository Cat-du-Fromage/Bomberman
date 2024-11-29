package ch.heigvd.dai;

import ch.heigvd.dai.mainMenu.MainMenu;
import ch.heigvd.dai.networkManager.NetworkManager;
import ch.heigvd.dai.networkManager.networkTask.ConnectToServerTask;
import ch.heigvd.dai.networkManager.networkTask.ServerMenuReadyStatesTask;
import ch.heigvd.dai.networkManager.networkTask.StartGameTask;
import ch.heigvd.dai.terrain.TileType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GameManager extends JFrame
{
    private static GameManager instance;
    public static GameManager getInstance(){return instance;}

    private int playerIndex = -1;

    public final int GAME_WIDTH = 1024;
    public final int GAME_HEIGHT = 512;

    private Map<String, Integer> playersKeyIndex;

    private EGameState gameState = EGameState.MAIN_MENU;

    private JLayeredPane layeredPane;
    private MainMenu mainMenu;
    private GameScene gameScene;

    public GameManager()
    {
        instance = this;
        playersKeyIndex = new HashMap<String, Integer>();

        initialize();
        mainMenu = new MainMenu(this, GAME_WIDTH, GAME_HEIGHT);
        gameScene = new GameScene(GAME_WIDTH, GAME_HEIGHT);

        InitializeLayeredPane();

        changeGameState(EGameState.MAIN_MENU);

        //SwingUtilities.invokeLater(gameScene::repaint);
        System.out.println("Game Manager Created");
    }

    private void InitializeLayeredPane()
    {
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        add(layeredPane);

        mainMenu.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);
        gameScene.setBounds(0, 0, GAME_WIDTH, GAME_HEIGHT);

        layeredPane.add(mainMenu, Integer.valueOf(0)); // Menu en arrière-plan
        layeredPane.add(gameScene, Integer.valueOf(1)); // Scène de jeu au-dessus
        layeredPane.addKeyListener(gameScene);
    }

    private void initialize()
    {
        this.setTitle("Bomberman");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setFocusable(true);
        this.setLayout( new FlowLayout(FlowLayout.CENTER));
        this.setSize(GAME_WIDTH + 32, GAME_HEIGHT + 64);
        this.setPreferredSize( new Dimension(GAME_WIDTH + 32, GAME_HEIGHT + 64));
        this.pack(); //will size the frame to fit all element

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public GameScene getGameScene(){return gameScene;}

    public int numPlayer()
    {
        return playersKeyIndex.size();
    }

    public void setPlayerIndex(int playerIndex)
    {
        this.playerIndex = playerIndex;
    }

    public int getPlayerIndex()
    {
        return playerIndex;
        //if(!playersKeyIndex.containsKey(playerId)) return -1;
        // return playersKeyIndex.get(playerId);
    }

    public int getPlayerIndex(String playerId)
    {
        if(!NetworkManager.getInstance().isServer())
        {
            System.out.println("ERROR your are not a server");
            return -1;
        }
        if(!playersKeyIndex.containsKey(playerId)) return -1;
        return playersKeyIndex.get(playerId);
    }

    public String getPlayerKey(int playerIndex)
    {
        for (Map.Entry<String, Integer> entry : playersKeyIndex.entrySet())
        {
            if(entry.getValue() == playerIndex) return entry.getKey();
        }
        return null;
    }

    public MainMenu getMainMenu()
    {
        return mainMenu;
    }

    public void addPlayer(String id)
    {
        if(playersKeyIndex.size() >= NetworkManager.getInstance().MAX_PLAYERS) return;
        int index = playersKeyIndex.size();
        playersKeyIndex.putIfAbsent(id, index);

        //SERVER ONLY PART
        if(!NetworkManager.getInstance().isServer()) return;
        if(id.equals(NetworkManager.getInstance().getNetworkId()))
        {
            playerIndex = index;
        }
        else
        {
            //Envoie au client son index de jeu
            ConnectToServerTask task = new ConnectToServerTask(index);
            NetworkManager.getInstance().sendToClient(id, task);
        }
    }

    public void onPlayerConnection(String id)
    {
        if(gameState != EGameState.MAIN_MENU) return;
        addPlayer(id);
        if(!NetworkManager.getInstance().isServer()) return;
        System.out.println("Server: onPlayerConnection GameManager");

        ServerMenuReadyStatesTask task = new ServerMenuReadyStatesTask(mainMenu.getReadyStates());
        NetworkManager.getInstance().sendToClient(id, task);
    }

    private void showMainMenu() {
        gameScene.setVisible(false);
        mainMenu.setVisible(true);
        layeredPane.repaint();
    }

    private void showGameScene() {
        pack();
        mainMenu.setVisible(false);
        gameScene.setVisible(true);
        revalidate();
        repaint();

        layeredPane.repaint();
        gameScene.requestFocus();

    }

    public void changeGameState(EGameState gameState)
    {
        this.gameState = gameState;
        if(gameState == EGameState.MAIN_MENU)
        {
            showMainMenu();
        }
        else if (gameState == EGameState.GAME)
        {
            gameScene.onEnable();
            showGameScene();
            StartGameTask task = new StartGameTask(gameScene.Terrain().getTileTypeByteIndices(), playersKeyIndex.size());
            NetworkManager.getInstance().sendToClients(task);
        }
    }

    public void clientEnterGameScene(byte[] tileTypes, int numPlayers)
    {
        if(!NetworkManager.getInstance().isClient()) return;
        gameScene.onClientEnable(tileTypes, numPlayers);
        showGameScene();
    }
}
