package uk.ac.cam.cl.waytotheclinic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PathTileProvider implements TileProvider {
    private int TileWidth;
    private List<MapFragment.Point> path;
    private MapFragment parent;

    public void setPath(List<MapFragment.Point> path){
        this.path = path;
        //todo maybe more required?
    }

    public PathTileProvider(int width, MapFragment parent){
        TileWidth = width;
        this.parent = parent;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        Bitmap pathTile = constructFromPath(path, x, y, zoom);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pathTile.compress(Bitmap.CompressFormat.PNG, 0, baos);
        byte[] bitmapData = baos.toByteArray();
        return new Tile(TileWidth, TileWidth, bitmapData);
    }

    static int pathWidth = 3;
    private Bitmap constructFromPath(List<MapFragment.Point> points, int x, int y, int zoom){
        Bitmap result = Bitmap.createBitmap(TileWidth, TileWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawColor(Color.TRANSPARENT);

        if(points == null)
            return result;

        Paint pathPaint = new Paint();
        pathPaint.setARGB(255, 255, 0, 0);
        pathPaint.setStrokeWidth(pathWidth);

        double startX   = (double)(x) / Math.pow(2, zoom);
        double startY   = (double)(y) / Math.pow(2, zoom);

        for(int i = 1; i < points.size(); i++){
            MapFragment.Point a = points.get(i - 1);
            MapFragment.Point b = points.get(i);

            if(a.floor != parent.getFloor() || b.floor != parent.getFloor())
                continue;

//            double canvasAX = a.x - startX;
//            double canvasBX = b.x - startX;
//            double canvasAY = a.y - startY;
//            double canvasBY = b.y - startY;

            float scalingFactor = (float) (TileWidth * Math.pow(2, zoom));
            float canvasAX = (float) ((a.x - startX) * scalingFactor);
            float canvasAY = (float) ((a.y - startY) * scalingFactor);
            float canvasBX = (float) ((b.x - startX) * scalingFactor);
            float canvasBY = (float) ((b.y - startY) * scalingFactor);

            canvas.drawLine(canvasAX, canvasAY, canvasBX, canvasBY, pathPaint);
        }



        return result;
    }
}
