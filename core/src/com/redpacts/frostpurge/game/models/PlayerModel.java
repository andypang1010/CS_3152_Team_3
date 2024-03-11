package com.redpacts.frostpurge.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Gdx;
import com.redpacts.frostpurge.game.views.GameCanvas;

public class PlayerModel extends CharactersModel{

    /**
     * Instantiates the player with their starting location and angle and with their texture
     * @param position vector2 representing the starting location
     * @param rotation float representing angle the player is facing
     */
    public PlayerModel(Vector2 position, float rotation){
        this.position = position;
        this.rotation = rotation;
        this.velocity = new Vector2(0,0);
        FileHandle fileHandle = Gdx.files.internal("livhead.png");
        Pixmap pixmap = new Pixmap(fileHandle);
        this.texture = new Texture(pixmap);
        pixmap.dispose();
    }
    /**
     * draws the player onto the game canvas
     */
    public void drawPlayer(GameCanvas canvas){
        canvas.draw(texture, Color.WHITE, (float) texture.getWidth() / 2, (float) texture.getHeight() / 2, position.x, position.y, rotation,.5f,.5f);
    }

}
