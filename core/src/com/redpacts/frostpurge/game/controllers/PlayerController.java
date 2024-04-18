package com.redpacts.frostpurge.game.controllers;

import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import com.redpacts.frostpurge.game.models.EnemyModel;
import com.redpacts.frostpurge.game.models.PlayerModel;
import com.redpacts.frostpurge.game.views.GameCanvas;

public class PlayerController extends CharactersController {

    static final float MAX_OFFSET = 500f;
    static final float OFFSET_MULTIPLIER = 2f;


    PlayerController(PlayerModel player){
        model = player;
        flip = false;
    }

    public void vacuum() {

    }
    public void boost() {

    }

    /**
     * resets the owner to origin for testing
     */
    private void reset(){
        model.setPosition(100, 100);
        model.setRotation(0);
        model.setVelocity(0,0);
    }
    /**
     * Sets angle of the owner so the character can be drawn correctly
     */
    private void setAngle(float x, float y){
        if (x != 0 && y!= 0){
            model.setRotation((float) Math.toDegrees(Math.atan2(-y,x)));
        }
    }
    /**
     * Checks if the player has any resources
     */
    public boolean hasResources(){
        return ((PlayerModel) model).getBoostNum() > 0;
    }

    /**
     * Update function that will be called in gameplaycontroller to update the owner actions.
     * Right now the input controller isn't done yet, so I am using booleans for buttons presses.
     */
    public void update(float horizontal, float vertical, boolean decelerate, boolean boost, boolean vacuum){
        ((PlayerModel) model).addBoostCoolDown(-1);
        setAngle(horizontal,vertical);
        if (!decelerate){
            if (model.getBody().getLinearVelocity().len() < 70){
                model.getBody().applyForceToCenter(horizontal*1.5f, -vertical*1.5f, true);
            }
        }else{
            model.getBody().setLinearVelocity(model.getBody().getLinearVelocity().scl(0.95f));
        }
        if (boost && ((PlayerModel) model).getBoostNum() > 0 && ((PlayerModel) model).getBoostCoolDown() == 0){
            model.getBody().applyForceToCenter(horizontal*100f, -vertical*100f, true);
            ((PlayerModel) model).addCanBoost(-1);
            ((PlayerModel) model).resetBoostCoolDown();
        }
        if (Math.abs(horizontal) >= .1f || Math.abs(vertical) >= .1f){
            model.setRotation(-(float) Math.toDegrees(Math.atan2(vertical,horizontal)));
        }
        model.getBody().setLinearVelocity(model.getBody().getLinearVelocity().scl(0.99f));//friction
        model.setPosition(model.getBody().getPosition().scl(10));
    }

    public Vector2 cameraOffsetPos() {
        Vector2 pos = model.getPosition().cpy();
        Vector2 dir = model.getBody().getLinearVelocity().nor();
        return pos.interpolate(new Vector2(dir.x * MAX_OFFSET + pos.x, dir.y * MAX_OFFSET + pos.y), model.getBody().getLinearVelocity().len() / 100f, Interpolation.smooth);
    }

    public void draw(GameCanvas canvas, float horizontal, float vertical){
        // Draw shadow
        short[] indices = new short[3];
        indices[0] = 0;
        indices[1] = 1;
        indices[2] = 2;

        Vector2 rayStart = model.getBody().getPosition().cpy();
        int numRays = 20; // Number of segments for circle
        float deltaAngle = 360f / (numRays - 1); // Angle between each segment

        float angle = 0;
        Vector2 dir = new Vector2(1, 0);
        Vector2 rayDirection = dir.cpy().rotateDeg(angle);
        Vector2 rayEnd = rayStart.cpy().add(rayDirection.scl(((PlayerModel)model).getRadius())); // Calculate end point of the ray
        Vector2 rayPrevious = rayEnd.cpy();
        Vector2 ray1, ray2, ray3;

        for (int i = 1; i < numRays; i++) {
            angle += deltaAngle;
            rayDirection = dir.cpy().rotateDeg(angle);
            rayEnd = rayStart.cpy().add(rayDirection.scl(((PlayerModel)model).getRadius()));

            ray1 = rayStart.cpy().scl(10).add(-100, -100);
            ray2 = rayPrevious.cpy().scl(10).add(-100, -100);
            ray3 = rayEnd.cpy().scl(10).add(-100, -100);

            float[] vertices = {ray1.x, ray1.y, ray2.x, ray2.y, ray3.x, ray3.y};
            PolygonRegion cone = new PolygonRegion(new TextureRegion(), vertices, indices);
            canvas.draw(cone, new Color(0f, 0f, 0f, 0.5f), 100, 100 ,0);

            rayPrevious = rayEnd.cpy();
        }

        // Draw player
        String direction = getDirection(horizontal,vertical,previousDirection);
        if (Math.abs(model.getBody().getLinearVelocity().y) + Math.abs(model.getBody().getLinearVelocity().x) > 1 || Math.abs(horizontal) + Math.abs(vertical)>.5) {
            model.resetFilmStrip(model.getFilmStrip("idle"+direction));
            processRun(direction);
            model.drawCharacter(canvas, (float) Math.toDegrees(model.getRotation()), Color.WHITE, "running", direction);
            ((PlayerModel) model).drawFire(canvas);
        }else{
            //System.out.println(Math.abs(model.getVelocity().y) + Math.abs(model.getVelocity().x));
            model.resetFilmStrip(model.getFilmStrip(direction));
            processRun("idle"+direction);
            model.drawCharacter(canvas, (float) Math.toDegrees(model.getRotation()), Color.WHITE, "idle", direction);
        }
        previousDirection = direction;

    }
}
