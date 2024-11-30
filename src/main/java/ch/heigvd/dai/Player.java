package ch.heigvd.dai;

import ch.heigvd.dai.terrain.Terrain;
import ch.heigvd.dai.terrain.Tile;

import java.awt.*;
import java.io.Serializable;

public class Player implements Serializable
{
    Color color;
    int boundX = 26;
    int boundY = 26;

    Point position;
    Point velocity;

    private int baseSpeed = 2;
    private int speedMultiplier = 1;

    public Player(Point startPosition, Color color)
    {
        this.color = color;
        position = startPosition;
        velocity = new Point(0,0);
    }

    //┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
    //│  ◇◇◇◇◇◇ Server Only OR 'PlayersPositionSyncTask' commands ◇◇◇◇◇◇                                           │
    //└────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
    public void setPosition(Point newPosition)
    {
        position = newPosition;
    }

    //┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
    //│  ◇◇◇◇◇◇ Server Only OR 'ChangeVelocityTask' commands ◇◇◇◇◇◇                                                │
    //└────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
    public void setVelocity(Point velocity)
    {
        this.velocity = velocity;
    }

    //Oui il y a de la répétitions..
    //Si il y a encore ces répétitions de code c'est qu'on a pas eu le temps de refactoriser avant le rendu
    private boolean setTranslationX(Terrain terrain)
    {
        if(velocity.x == 0) return false;
        int maxMoveLength = baseSpeed * speedMultiplier;
        int translationX = velocity.x * maxMoveLength;

        int currentTileIndex = terrain.getTileIndex(position);
        Tile currentTile = terrain.getTiles()[currentTileIndex];
        Point currentTileCenter = currentTile.getCenter();

        int nextTileIndex = currentTileIndex + velocity.x;
        Tile nextTile = terrain.getTiles()[nextTileIndex];

        if(!nextTile.isWalkable())
        {
            if(currentTileCenter.x - position.x == 0) return false;
            boolean moveBeyondCenter = maxMoveLength >= Math.abs(currentTileCenter.x - position.x);
            position.x = moveBeyondCenter ? currentTileCenter.x : position.x + translationX;

        }
        else
        {
            int directionY = currentTileCenter.y - position.y < 0 ? -1 : 1;
            int moveToCenterLength = Math.abs(currentTileCenter.y - position.y);
            int moveLeftLength = maxMoveLength - moveToCenterLength;
            if(moveLeftLength > 0)
            {
                position.translate(moveLeftLength * velocity.x, currentTileCenter.y - position.y);
            }
            else
            {
                position.translate(0, maxMoveLength * directionY);
            }
        }
        return true;
    }

    private boolean setTranslationY(Terrain terrain)
    {
        if(velocity.y == 0) return false;
        int maxMoveLength = baseSpeed * speedMultiplier;
        int translationY = velocity.y * maxMoveLength;

        int currentTileIndex = terrain.getTileIndex(position);
        Tile currentTile = terrain.getTiles()[currentTileIndex];
        Point currentTileCenter = currentTile.getCenter();

        int nextTileIndex = currentTileIndex + velocity.y * terrain.TILE_WIDTH;
        Tile nextTile = terrain.getTiles()[nextTileIndex];

        if(!nextTile.isWalkable())
        {
            if(currentTileCenter.y - position.y == 0) return false;
            boolean moveBeyondCenter = maxMoveLength > Math.abs(currentTileCenter.y - position.y);
            position.y = moveBeyondCenter ? currentTileCenter.y : position.y + translationY;
        }
        else
        {
            int directionX = currentTileCenter.x - position.x < 0 ? -1 : 1;
            int moveToCenterLength = Math.abs(currentTileCenter.x - position.x);
            int moveLeftLength = maxMoveLength - moveToCenterLength;
            if(moveLeftLength > 0)
            {
                position.translate(currentTileCenter.x - position.x, moveLeftLength * velocity.y);
            }
            else
            {
                position.translate(maxMoveLength * directionX, 0);
            }
        }
        return true;
    }

    public boolean update(Terrain terrain) {
        boolean xChanged = setTranslationX(terrain);
        boolean yChanged = setTranslationY(terrain);
        return xChanged || yChanged;
    }

    public void draw(Graphics2D g)
    {
        //il faut appliquer un offset
        //awt calcul les forme depuis en haut a gauche donc il faut revenir sur x et y
        // de la moitié de la width et height
        Point drawStart = new Point(position.x - boundX/2, position.y - boundY/2);
        g.setColor(color);
        g.fillOval(drawStart.x, drawStart.y,boundX,boundY);
    }
}
