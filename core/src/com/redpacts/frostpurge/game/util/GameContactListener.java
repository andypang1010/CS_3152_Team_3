package com.redpacts.frostpurge.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.redpacts.frostpurge.game.assets.AssetDirectory;
import com.redpacts.frostpurge.game.models.*;

import java.util.Objects;
import java.util.logging.Level;

public class GameContactListener implements ContactListener {
    private World world;
    private LevelModel board;
    private Sound collide;
    private Sound breakBox;
    private Sound bounce;
    private Sound hit;
    private float volume;
    private float time;

    public GameContactListener(World world, LevelModel board, AssetDirectory assets, float volume){
        this.world = world;
        this.board = board;
        collide = assets.getEntry("Collide",Sound.class);
        breakBox = assets.getEntry("Break",Sound.class);
        bounce = assets.getEntry("Bounce",Sound.class);
        hit = assets.getEntry("Hit",Sound.class);
        this.volume = volume;
        time = 0;
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    @Override
    public void beginContact(Contact contact) {
        //System.out.println("CONTACTED");
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();
        GameObject obj1 = (GameObject)body1.getUserData();
        GameObject obj2 = (GameObject)body2.getUserData();

        // If either object is the avatar, change color
        if (obj1 != null && obj2 != null) {
            processCollision(contact, obj1, obj2, contact.getFixtureA(), contact.getFixtureB());
        }
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.
     */
    @Override
    public void endContact(Contact contact) {
//        System.out.println("NOT CONTACTED");
    }

    /** Unused ContactListener method */
    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    protected void processCollision(Contact contact, GameObject obj1, GameObject obj2, Fixture fixtureA, Fixture fixtureB) {
        if (obj1 instanceof PlayerModel && obj2 instanceof PlayerModel) {
            handleCollision((PlayerModel) obj1, (PlayerModel) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof EnemyModel) {
            handleCollision(contact, (PlayerModel) obj1, (EnemyModel) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof ObstacleTile) {
            handleCollision((PlayerModel) obj1, (ObstacleTile) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof SwampTile) {
            handleCollision((PlayerModel) obj1, (SwampTile) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof GoalTile && fixtureB.isSensor()) {
            handleCollision((PlayerModel) obj1, (GoalTile) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof BouncyTile) {
            handleCollision(contact, (PlayerModel) obj1, (BouncyTile) obj2);
        } else if (obj1 instanceof PlayerModel && obj2 instanceof BreakableTile) {
            handleCollision(contact, (PlayerModel) obj1, (BreakableTile) obj2);
        }

        else if (obj1 instanceof EnemyModel && obj2 instanceof PlayerModel) {
            handleCollision(contact, (PlayerModel) obj2, (EnemyModel) obj1);
        } else if (obj1 instanceof EnemyModel && obj2 instanceof EnemyModel) {
            handleCollision((EnemyModel) obj1, (EnemyModel) obj2);
        } else if (obj1 instanceof EnemyModel && obj2 instanceof ObstacleTile) {
            handleCollision((EnemyModel) obj1, (ObstacleTile) obj2);
        } else if (obj1 instanceof EnemyModel && obj2 instanceof BouncyTile) {
            handleCollision((EnemyModel) obj1, (BouncyTile) obj2);
        }

        else if (obj1 instanceof ObstacleTile && obj2 instanceof PlayerModel) {
            handleCollision((PlayerModel) obj2, (ObstacleTile) obj1);
        } else if (obj1 instanceof ObstacleTile && obj2 instanceof EnemyModel) {
            handleCollision((EnemyModel) obj2, (ObstacleTile) obj1);
        }

        else if (obj1 instanceof SwampTile && obj2 instanceof PlayerModel) {
            handleCollision((PlayerModel) obj2, (SwampTile) obj1);
        }

        else if (obj1 instanceof GoalTile && fixtureA.isSensor() && obj2 instanceof PlayerModel) {
            handleCollision((PlayerModel) obj2, (GoalTile) obj1);
        }

        else if (obj1 instanceof BouncyTile && obj2 instanceof PlayerModel) {
            handleCollision(contact, (PlayerModel) obj2, (BouncyTile) obj1);
        } else if (obj1 instanceof BouncyTile && obj2 instanceof EnemyModel) {
            handleCollision((EnemyModel) obj2, (BouncyTile) obj1);
        }

        else if (obj1 instanceof BreakableTile && obj2 instanceof PlayerModel) {
            handleCollision(contact, (PlayerModel) obj2, (BreakableTile) obj1);
        }
    }

    /**
     * Handles collisions between a player and an enemy
     *
     * @param player    The player
     * @param player2   Other player
     */
    private void handleCollision(PlayerModel player, PlayerModel player2) {}
    /**
     * Handles collisions between a player and an enemy
     *
     * @param player The player
     * @param enemy  The enemy
     */
    private void handleCollision(Contact contact, PlayerModel player, EnemyModel enemy) {
        if(!player.getInvincibility() && !player.getGameOver()) { // Player is not invincible, nor the gameplay is over
            if(Objects.equals(enemy.getEnemyType(), "flies")){
                player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().scl(0.25f));
                player.addHp(-1);
            }else{
                Vector2 contactDirection = player.getPosition().cpy().sub(enemy.getPosition()).nor();
                player.addHp(-15);
                player.setShake(true);
                player.getBody().applyForceToCenter(contactDirection.scl(50), true);
                enemy.getBody().applyForceToCenter(contactDirection.scl(-50), true);
                if (time>.5f){
                    hit.play(volume*2f);
                    time = 0;
                }
                player.startInvincibility();
            }
        } else { // Player is invincible
            contact.setEnabled(false);
        }
    }

    /**
     * Handles collisions between a player and an enemy
     *
     * @param player The player
     * @param tile   The tile
     */
    private void handleCollision(PlayerModel player, ObstacleTile tile) {
        // TODO: Update so that we dampen the velocity of player
//        System.out.println("Contact with obstacle");
//        System.out.println();

//        float vx1 = player.getVelocity().x;
//        float vy1 = player.getVelocity().y;
//
//        player.setVelocity(-vx1, -vy1);
        if (time>.5f){
            collide.play(volume*1.5f);
            time = 0;
        }
    }

    /**
     * Handles collisions between a player and an enemy
     *
     * @param player The player
     * @param tile   The tile
     */
    private void handleCollision(PlayerModel player, SwampTile tile) {
        // TODO: Update so that we dampen the velocity of player
//        System.out.println("Contact with swamp");
//        System.out.println("player position:" + player.getPosition());
//        System.out.println("tile position:" + tile.getPosition());
//        world.destroyBody(tile.getBody());
//        board.removeExtra(tile.getPosition().x, tile.getPosition().y);
//        player.setCanBoost(true);
    }
    /**
     * Handles collisions between a player and an enemy
     *
     * @param player The player
     * @param tile   The tile
     */
    private void handleCollision(PlayerModel player, GoalTile tile) {
        // TODO: Update so that we dampen the velocity of player
        tile.activate();
        player.setWin(true);
    }
    /**
     * Handles collisions between a player and a bouncy
     *
     * @param player The player
     * @param tile   The tile
     */
    private void handleCollision(Contact contact, PlayerModel player, BouncyTile tile) {
        int xDirection = tile.checkBoundLeftRight(contact.getWorldManifold().getPoints()[0]);
        int yDirection = tile.checkBoundTopBottom(contact.getWorldManifold().getPoints()[0]);
        player.getBody().setLinearVelocity(player.getBody().getLinearVelocity().scl(4 * xDirection, 4 * yDirection));
        bounce.play(volume);
        tile.activate();
    }
    /**
     * Handles collisions between a player and a breakable collides
     *
     * @param player The player
     * @param tile   The tile
     */
    private void handleCollision(Contact contact, PlayerModel player, BreakableTile tile) {
        // TODO: Implement actual break speed
        if (player.getBody().getLinearVelocity().len() > 105) {
            contact.setEnabled(false);
            tile.deactivate();
            player.setShake(true);
            breakBox.play(volume*1.5f);
        }else{
            if (time>.5f){
                collide.play(volume*1.5f);
                time = 0;
            }
        }
    }

    /**
     * Handles collisions between a player and an enemy
     *
     * @param enemy    The enemy
     * @param enemy2   Other enemy
     */
    private void handleCollision(EnemyModel enemy, EnemyModel enemy2) {
        // TODO: Update so that we handle bounce correctly
//        float vx1 = enemy.getVelocity().x;
//        float vy1 = enemy.getVelocity().y;
//        float vx2 = enemy2.getVelocity().x;
//        float vy2 = enemy2.getVelocity().y;
//
//        enemy.setVelocity(-vx1, -vy1);
//        enemy2.setVelocity(-vx2, -vy2);
    }

    /**
     * Handles collisions between a player and an enemy
     *
     * @param enemy The enemy
     * @param tile   The tile
     */
    private void handleCollision(EnemyModel enemy, ObstacleTile tile) {
        // TODO: Update so that we dampen the velocity of enemy
//        float vx1 = enemy.getVelocity().x;
//        float vy1 = enemy.getVelocity().y;
//
//        enemy.setVelocity(-vx1, -vy1);
    }
    /**
     * Handles collisions between an enemy and a bouncy
     *
     * @param enemy The player
     * @param tile   The tile
     */
    private void handleCollision(EnemyModel enemy, BouncyTile tile) {
        tile.activate();
    }

    public void updateTime(float time){
        this.time+=time;
    }
}