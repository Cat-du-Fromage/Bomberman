package ch.heigvd.dai;

import java.awt.*;

public class GridUtils
{
    public static int getIndexFromPosition(Point pointPos, Point gridSizeXY)
    {
        float percentX = Math.clamp(pointPos.x / gridSizeXY.x, 0, 1);
        float percentY = Math.clamp(pointPos.y / gridSizeXY.y, 0, 1);
        int coordX = Math.clamp((int)Math.floor(gridSizeXY.x * percentX), 0, gridSizeXY.x - 1);
        int coordY = Math.clamp((int)Math.floor(gridSizeXY.y * percentY), 0, gridSizeXY.y - 1);
        return coordY * gridSizeXY.x + coordX;
    }

    public static int getIndexFromPosition(Point pointPos, Point numTileXY, int cellSize) {
        // Clamp the coordinates within the valid range
        int coordX = Math.max(0, Math.min(pointPos.x / cellSize, numTileXY.x - 1));
        int coordY = Math.max(0, Math.min(pointPos.y / cellSize, numTileXY.y - 1));
        return coordY * numTileXY.x + coordX;
    }
}
