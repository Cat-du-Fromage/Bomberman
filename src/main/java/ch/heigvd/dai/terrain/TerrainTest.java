package ch.heigvd.dai.terrain;

import javax.swing.*;
import java.awt.*;

class TestGamePanel extends JPanel {
    private Terrain terrain;

    // Constructeur où on initialise le terrain
    public TestGamePanel(Terrain terrain) {
        this.terrain = terrain;
        setPreferredSize(new Dimension(terrain.GAME_WIDTH, terrain.GAME_HEIGHT));  // Définir la taille du panneau
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        terrain.draw((Graphics2D) g);  // Appeler la méthode draw de Terrain pour dessiner
    }
}

class GameFrame extends JFrame
{
    TestGamePanel gamePanel;
    Terrain terrain;
    GameFrame(String title)
    {
        terrain = new Terrain();
        gamePanel = new TestGamePanel(terrain);
        this.setTitle(title);
        this.setSize(terrain.GAME_WIDTH, terrain.GAME_HEIGHT);
        this.setPreferredSize(new Dimension(terrain.GAME_WIDTH, terrain.GAME_HEIGHT));
        setBackground(Color.green);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.add(gamePanel);
        //this.pack(); //will size the frame to fit all element

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        revalidate();
        repaint();
    }

    @Override
    public void paintComponents(Graphics g) {
        System.out.println("GameFrame paintComponents");
        super.paintComponents(g);
        terrain.draw((Graphics2D) g);
    }
}

public class TerrainTest
{
    public static void main(String[] args)
    {
        GameFrame gameFrame = new GameFrame("TerrainTest");
    }
}
