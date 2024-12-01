package ch.heigvd.dai;

import ch.heigvd.dai.networkManager.NetworkManager;
import ch.heigvd.dai.networkManager.networkTask.ChangeVelocityTask;
import ch.heigvd.dai.networkManager.networkTask.PlayersPositionSyncTask;
import ch.heigvd.dai.terrain.Terrain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameScene extends JPanel implements ActionListener, KeyListener
{
    Color[] colors = new Color[]{Color.WHITE, Color.BLACK, Color.BLUE, Color.YELLOW};
    //sera utilisé par les bombe et joueurs?
    private final int DELAY = 25;
    private Timer timer;

    private Terrain terrain;
    private Player[] players;

    public GameScene(int width, int height)
    {
        terrain = new Terrain();
        this.setFocusable(true);
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.green);
        this.setVisible(true);
        this.addKeyListener(this);
    }

    public Player[] Players() {return players;}
    public Terrain Terrain() {return terrain;}

    //Sever Only
    public void onEnable()
    {
        //only the server can update
        if(NetworkManager.getInstance().isServer())
        {
            initializePlayers(GameManager.getInstance().numPlayer());
            timer = new Timer(DELAY, this);
            timer.start();
            this.revalidate();
            this.repaint();
        }
    }

    public void onClientEnable(byte[] tileTypes, int numPlayers)
    {
        if(!NetworkManager.getInstance().isClient()) return;
        terrain.setTiles(tileTypes);
        initializePlayers(numPlayers);
    }

    private void initializePlayers(int numPlayers)
    {
        players = new Player[numPlayers];
        Point[] playerStarts = terrain.getPlayerStarts();
        for (int i = 0; i < players.length; i++)
        {
            players[i] = new Player(playerStarts[i], colors[i]);
        }
    }

    // MAIN UPDATE methode
    @Override
    public void actionPerformed(ActionEvent e) {

        //ONLY SERVER!
        updatePlayers();
        repaint();
    }

    //update players logics
    private void updatePlayers()
    {
        int counter = 0;
        for (Player player : players)
        {
            counter += player.update(terrain) ? 1 : 0;
        }
        if(counter == 0) return;
        sendPlayersPositionToClients();
    }

    //update visuals
    public void draw(Graphics2D g)
    {
        terrain.draw(g);
        for (Player player : players)
        {
            player.draw(g);
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        draw((Graphics2D)g);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            //placer bombe
            int playerIndex = GameManager.getInstance().getPlayerIndex();
            Player playerWhoPlacedBomb = players[playerIndex];
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W :
                movePlayer(new Point(0,-1));
                break;
            case KeyEvent.VK_A :
                movePlayer(new Point(-1,0));
                break;
            case KeyEvent.VK_S :
                movePlayer(new Point(0,1));
                break;
            case KeyEvent.VK_D :
                movePlayer(new Point(1,0));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D:
                movePlayer(new Point(0,0));
                break;
        }
    }

    public void movePlayer(Point velocity)
    {
        if(NetworkManager.getInstance().isServer())
        {
            players[0].setVelocity(velocity);
        }
        else
        {
            sendVelocityToServer(velocity);
        }
    }

//╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
//║                                               ◆◆◆◆◆◆ NETCODE ◆◆◆◆◆◆                                                ║
//╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝

    public void clientSetPlayersPosition(Point[] positions)
    {
        if(!NetworkManager.getInstance().isClient()) return;
        for (int i = 0; i < players.length; i++)
        {
            players[i].setPosition(positions[i]);
        }
        repaint();
    }

    private void sendPlayersPositionToClients()
    {
        if(!NetworkManager.getInstance().isServer()) return;
        Point[] playerCoords = new Point[players.length];
        for (int i = 0; i < players.length; i++)
        {
            playerCoords[i] = players[i].position;
        }
        PlayersPositionSyncTask task = new PlayersPositionSyncTask(playerCoords);
        NetworkManager.getInstance().sendToClients(task);
    }

    private void sendVelocityToServer(Point velocity)
    {
        if(!NetworkManager.getInstance().isClient()) return;
        ChangeVelocityTask task = new ChangeVelocityTask(velocity);
        NetworkManager.getInstance().sendToServer(task);
    }
}
