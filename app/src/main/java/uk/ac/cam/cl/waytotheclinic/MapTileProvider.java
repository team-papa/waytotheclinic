package uk.ac.cam.cl.waytotheclinic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

/**
 * Created by Chris on 12/02/2018.
 */

public class MapTileProvider implements TileProvider{
    Bitmap baseImage;
    GoogleMap map;
    public Tile NO_TILE;
    public int TileWidth;
    int backgourndColor = Color.WHITE;

    public MapTileProvider(Bitmap baseImage, GoogleMap map){
        //calculate the optimal tile size, min n for 2^n > max(width, height)
        int maxDim = Math.max(baseImage.getHeight(), baseImage.getWidth());
        int tileWidthPower = 1;
        for(; Math.pow(2, tileWidthPower) < maxDim; tileWidthPower++);
        TileWidth = (int)Math.pow(2, tileWidthPower);

        this.baseImage = Bitmap.createBitmap(TileWidth, TileWidth, baseImage.getConfig());
        Canvas canvas = new Canvas(this.baseImage);
        canvas.drawColor(backgourndColor);
        canvas.drawBitmap(baseImage, 0, 0, null);

        genNO_TILE(TileWidth, TileWidth);
        this.map = map;
    }

    private void genNO_TILE(int width, int height){
        Bitmap empty = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(empty);
        canvas.drawColor(backgourndColor);
        //test display code
//        for(int i = 0; i < width; i++){
//            for(int j = 0; j < height; j++){
//                empty.setPixel(i, j, j % 2 == 0 ? (i % 2 == 0 ? Color.BLACK : Color.WHITE) : (i % 2 == 1 ? Color.BLACK : Color.WHITE));
////                empty.setPixel(i, j, Color.BLACK);
//            }
//        }
        NO_TILE = bitmapToTile(empty);
    }

    private Tile bitmapToTile(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return new Tile(bmp.getWidth(), bmp.getHeight(), baos.toByteArray());
    }

    /**
     * @param zoom 0 = 1x1, 1 = 2x2, 2 = 4x4, ...
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
        int TileWidthZoomed = (int)Math.ceil(TileWidth * 1.0 / Math.pow(2, zoom));
        int TileHeightZoomed = (int)Math.ceil(TileWidth * 1.0 / Math.pow(2, zoom));

        int startX = x * TileWidthZoomed;
        int startY = y * TileHeightZoomed;

        if(startX > baseImage.getWidth() || startY > baseImage.getHeight())
            return NO_TILE;

        int width = Math.min(baseImage.getWidth() - startX, TileWidthZoomed);
        int height = Math.min(baseImage.getHeight() - startY, TileHeightZoomed);

        Bitmap outbmp = Bitmap.createBitmap(baseImage, startX, startY, width, height);
        return bitmapToTile(outbmp);
    }
}
