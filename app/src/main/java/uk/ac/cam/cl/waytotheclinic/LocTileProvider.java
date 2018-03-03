package uk.ac.cam.cl.waytotheclinic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

public class LocTileProvider implements TileProvider{
    private MapFragment.Point location;
    private int TileWidth;
    private MapFragment parent;

    public void setLocation(MapFragment.Point loc){
        this.location = loc;
        //todo maybe more required?
    }

    public LocTileProvider(int width, MapFragment parent){
        TileWidth = width;
        this.parent = parent;
    }

    private static int locRadius = 5;
    @Override
    public Tile getTile(int x, int y, int zoom) {
        Bitmap locTile = Bitmap.createBitmap(TileWidth, TileWidth, Bitmap.Config.ARGB_8888);

        {
            Canvas canvas = new Canvas(locTile);
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);

            double startX   = (double)(x) / Math.pow(2, zoom);
            double startY   = (double)(y) / Math.pow(2, zoom);

            float scalingFactor = (float) (TileWidth * Math.pow(2, zoom));
            float drawX = (float) ((location.x - startX) * scalingFactor);
            float drawY = (float) ((location.y - startY) * scalingFactor);
            canvas.drawCircle(drawX, drawY, locRadius, paint);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        locTile.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] bitmapData = baos.toByteArray();
        return new Tile(TileWidth, TileWidth, bitmapData);
    }
}
