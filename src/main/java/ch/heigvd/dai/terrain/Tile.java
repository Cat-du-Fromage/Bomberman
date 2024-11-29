package ch.heigvd.dai.terrain;

import java.awt.*;

public class Tile
{
    private TileType type;
    private boolean walkable;

    protected int Size;
    protected Point coord;
    protected Point position;
    protected Point center;

    public Tile(int size, int x, int y)
    {
        this(size,x,y,TileType.NONE,true);
    }

    //constructor for children only
    protected Tile(int size, int x, int y, TileType type, boolean walkable)
    {
        this.type = type;
        this.walkable = walkable;

        Size = size;
        coord = new Point(x, y);
        position = new Point(x * size, y * size);
        this.center = new Point(position.x + size/2, position.y + size/2);
    }

    public TileType getTileType() { return type; }

    public boolean isWalkable() { return walkable; }

    public Point getPosition() { return position; }

    public Point getCenter() { return new Point(center.x, center.y); }

    public void draw(Graphics2D g) { return; }

    public void onBombHit() { return; }

    @Override
    public String toString() {
        return "Tile coord = " + coord + ", position = " + position + ", center = " + center;
    }
}

class WallTile extends Tile
{
    public WallTile(int size, int x, int y)
    {
        super(size, x, y, TileType.WALL, false);
    }

    @Override
    public void draw(Graphics2D g)
    {
        g.setPaint(Color.lightGray);
        g.fillRect(position.x, position.y, Size, Size);
        g.setColor(Color.gray); // Gris foncé
        g.setStroke(new BasicStroke(2)); // Épaisseur de la ligne de contour
        g.drawRect(position.x, position.y, Size, Size); // Dessine le contour du carré
    }
}

class DirtTile extends Tile
{
    public DirtTile(int size, int x, int y)
    {
        super(size, x, y, TileType.DIRT, false);
    }

    @Override
    public void draw(Graphics2D g)
    {
        Color lightBrown = new Color(79,58,43);
        Color darkBrown = new Color(43,24,12);
        g.setPaint(lightBrown);
        g.fillRect(position.x, position.y, Size, Size);
        g.setColor(darkBrown); // Gris foncé
        g.setStroke(new BasicStroke(2)); // Épaisseur de la ligne de contour
        g.drawRect(position.x, position.y, Size, Size); // Dessine le contour du carré
    }

    @Override
    public void onBombHit()
    {
        // if(isServer())
        // send to terrain the bonus that appear after being destroyed
    }
}


