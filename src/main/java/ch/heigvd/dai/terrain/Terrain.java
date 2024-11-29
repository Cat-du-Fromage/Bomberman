package ch.heigvd.dai.terrain;

import ch.heigvd.dai.GameManager;
import ch.heigvd.dai.GridUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Terrain //extends JPanel
{
    //public final int GAME_WIDTH = 1024;
    //public final int GAME_HEIGHT = 512;
    public int GAME_WIDTH = 1024;
    public int GAME_HEIGHT = 512;
    public final int TILE_SIZE = 32;

    public final int TILE_WIDTH = GAME_WIDTH / TILE_SIZE;
    public final int TILE_HEIGHT = GAME_HEIGHT / TILE_SIZE;
    public final int TILE_COUNT = TILE_WIDTH * TILE_HEIGHT;

    private Point[] playerStarts = new Point[4];

    private Tile[] tiles = new Tile[TILE_COUNT];

    public Terrain()
    {
        GAME_WIDTH = GameManager.WIDTH;
        GAME_HEIGHT = GameManager.HEIGHT;
        System.out.println("Terrain created");
        InitializeTerrain();
        generateDirt();

        initStarts();
    }

    public void initStarts()
    {
        playerStarts[0] = tiles[TILE_WIDTH + 1].getCenter();
        playerStarts[1] = tiles[2*TILE_WIDTH-2].getCenter();
        playerStarts[2] = tiles[TILE_COUNT-(2*TILE_WIDTH)+2].getCenter();
        playerStarts[3] = tiles[TILE_COUNT-TILE_WIDTH-1].getCenter();
    }

    public TileType[] TilesType()
    {
        TileType[] result = new TileType[TILE_COUNT];
        for (int i = 0; i < TILE_COUNT; i++)
        {
            result[i] = tiles[i].getTileType();
        }
        return result;
    }

    public byte[] getTileTypeByteIndices()
    {
        byte[] result = new byte[TILE_COUNT];
        for (int i = 0; i < TILE_COUNT; i++)
        {
            result[i] = (byte)tiles[i].getTileType().ordinal();
        }
        return result;
    }

    /**
     * On construit aussi la bordure qui se trouve à l'exterieur
     */
    private void InitializeTerrain()
    {
        for (int i = 0; i < TILE_COUNT; i++)
        {
            int y = i / TILE_WIDTH;
            int x = i - y * TILE_WIDTH;
            boolean isBorder = y == 0 || y == TILE_HEIGHT - 1 || x == 0 || x == TILE_WIDTH - 1;
            tiles[i] = isBorder ? new WallTile(TILE_SIZE, x, y) : new Tile(TILE_SIZE, x,y);
        }
    }

    public Point[] getPlayerStarts()
    {
        return playerStarts.clone();
    }

    public Tile[] getTiles()
    {
        return tiles;
    }

    public void setTiles(TileType[] tilesType)
    {
        for (int i = 0; i < TILE_COUNT; i++)
        {
            Point coord = tiles[i].coord;
            switch (tilesType[i])
            {
                case NONE -> tiles[i] = new Tile(TILE_SIZE, coord.x, coord.y);
                case WALL -> tiles[i] = new WallTile(TILE_SIZE, coord.x, coord.y);
                case DIRT -> tiles[i] = new DirtTile(TILE_SIZE, coord.x, coord.y);
            }
        }
    }

    public void setTiles(int[] tilesType)
    {
        for (int i = 0; i < TILE_COUNT; i++)
        {
            Point coord = tiles[i].coord;
            TileType tileType = TileType.values()[tilesType[i]];
            switch (tileType)
            {
                case NONE -> tiles[i] = new Tile(TILE_SIZE, coord.x, coord.y);
                case WALL -> tiles[i] = new WallTile(TILE_SIZE, coord.x, coord.y);
                case DIRT -> tiles[i] = new DirtTile(TILE_SIZE, coord.x, coord.y);
            }
        }
    }

    public void setTiles(byte[] tilesType)
    {
        for (int i = 0; i < TILE_COUNT; i++)
        {
            Point coord = tiles[i].coord;
            TileType tileType = TileType.values()[tilesType[i]];
            switch (tileType)
            {
                case NONE -> tiles[i] = new Tile(TILE_SIZE, coord.x, coord.y);
                case WALL -> tiles[i] = new WallTile(TILE_SIZE, coord.x, coord.y);
                case DIRT -> tiles[i] = new DirtTile(TILE_SIZE, coord.x, coord.y);
            }
        }
    }

    public int getTileIndex(Point position)
    {
        return GridUtils.getIndexFromPosition(position, new Point(TILE_WIDTH, TILE_HEIGHT), TILE_SIZE);
    }

    private ArrayList<Point> getPlayerSafeCoords()
    {
        ArrayList<Point> playerSafeCoords = new ArrayList<Point>(4 * 3);
        playerSafeCoords.add(new Point(1, 1));
        playerSafeCoords.add(new Point(2, 1));
        playerSafeCoords.add(new Point(1, 2));

        playerSafeCoords.add(new Point(TILE_WIDTH - 2, 1));
        playerSafeCoords.add(new Point(TILE_WIDTH - 3, 1));
        playerSafeCoords.add(new Point(TILE_WIDTH - 2, 2));

        playerSafeCoords.add(new Point(1, TILE_HEIGHT - 2));
        playerSafeCoords.add(new Point(2, TILE_HEIGHT - 2));
        playerSafeCoords.add(new Point(1, TILE_HEIGHT - 3));

        playerSafeCoords.add(new Point(TILE_WIDTH - 2, TILE_HEIGHT - 2));
        playerSafeCoords.add(new Point(TILE_WIDTH - 3, TILE_HEIGHT - 2));
        playerSafeCoords.add(new Point(TILE_WIDTH - 2, TILE_HEIGHT - 3));
        return playerSafeCoords;
    }

    private void generateDirt()
    {
        ArrayList<Point> playerSafeCoords = getPlayerSafeCoords();
        for (int i = 0; i < tiles.length; i++)
        {
            int y = i / TILE_WIDTH;
            int x = i - y * TILE_WIDTH;
            boolean isBorder = y == 0 || y == TILE_HEIGHT - 1 || x == 0 || x == TILE_WIDTH - 1;
            if(isBorder) continue;
            if(playerSafeCoords.contains(new Point(x, y))) continue;
            tiles[i] = new DirtTile(TILE_SIZE, x, y);
        }

        digHoles();
    }

    private void digHoles()
    {
        Random rng = new Random();
        int numHole = rng.nextInt(50, 100);
        for (int i = 0; i < numHole; i++)
        {
            int x = rng.nextInt(TILE_WIDTH);
            int y = rng.nextInt(TILE_HEIGHT);
            int tileIndex = y * TILE_WIDTH + x;
            TileType tileType = tiles[tileIndex].getTileType();
            if(tileType == TileType.WALL || tileType == TileType.NONE) continue;
            tiles[tileIndex] = new Tile(TILE_SIZE, x, y);
        }
    }

    public void draw(Graphics2D g)
    {
        for (int i = 0; i < tiles.length; i++)
        {
            tiles[i].draw(g);
            drawTileIndex(g, i);
        }
    }

    //for debug purpose
    private void drawTileIndex(Graphics2D g, int index)
    {
        g.setColor(Color.red);
        Point location = tiles[index].getPosition();
        Point center = new Point(location.x + 2, location.y + 20);
        //Point center2 = tiles[index].getPosition();
        //g.drawOval(center2.x, center2.y, 8, 16);
        g.drawString(String.valueOf(index), center.x, center.y);
    }

//╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
//║                                               ◆◆◆◆◆◆ NETCODE ◆◆◆◆◆◆                                                ║
//╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝

    public void OnReceiveUpdateFromServer(int x, int y, int tileTypeIndex)
    {
        //if(GameManager.isClient() || GameManager.isHost())
        {
            // on reçoit :
            // coord de la case
            // nouvel état de la case
        }
    }

    public void SendUpdateToClients()
    {
        //if(GameManager.isServer())
        // Doit pouvoir envoyer soit une case soit plusieurs (voire tout le terrain)
    }
}
