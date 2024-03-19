package com.redpacts.frostpurge.game.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
//import com.redpacts.frostpurge.game.assets.AssetDirectory;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.redpacts.frostpurge.game.assets.AssetDirectory;
import com.redpacts.frostpurge.game.models.EnemyModel;
import com.redpacts.frostpurge.game.models.EnvironmentalObject;
import com.redpacts.frostpurge.game.models.MapModel;
import com.redpacts.frostpurge.game.models.PlayerModel;
import com.redpacts.frostpurge.game.models.TileModel;
import com.redpacts.frostpurge.game.util.EnemyStates;
import com.redpacts.frostpurge.game.util.ScreenListener;
import com.redpacts.frostpurge.game.util.TileGraph;
import com.redpacts.frostpurge.game.views.GameCanvas;

public class GameMode implements Screen {
    private GameCanvas canvas;

    private OrthographicCamera camera;
    private OrthographicCamera HUDcamera;
    private AssetDirectory directory;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    private CollisionController collisionController;

    /** Whether or not this player mode is still active */
    private boolean active;
    /** Board for the game*/
    private MapModel board;
    /** Player for the game*/
    private PlayerModel playerModel;

    /** Player for the game*/
    private PlayerController playerController;

    private EnemyController enemyController;

    private Array<EnemyModel> enemies;

    private TileGraph tileGraph = new TileGraph();

    private Texture statusBarBGTexture;
    private Texture statusBarTexture;


    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Variable to track the game state (SIMPLE FIELDS) */
    private GameState gameState;
    public GameMode(GameCanvas canvas) {
        this.canvas = canvas;
        active = false;
        // Null out all pointers, 0 out all ints, etc.
        gameState = GameState.INTRO;

        directory = new AssetDirectory("assets.json");
        directory.loadAssets();
        directory.finishLoading();
        enemies = new Array<EnemyModel>();
        // Create the controllers.

        statusBarBGTexture = new TextureRegion(directory.getEntry("StatusBar_BG", Texture.class)).getTexture();
        statusBarTexture = new TextureRegion(directory.getEntry("StatusBar_Bar", Texture.class)).getTexture();

        Array<Integer> obstacles = new Array<Integer>();// Obstacle locations
        obstacles.add(43, 50, 57, 383);
        obstacles.add(390, 397);
        Array<Integer> swamps = new Array<Integer>();// Swamp locations
        swamps.add(22, 25, 52, 55);
        Array<EnvironmentalObject> objects = new Array<EnvironmentalObject>();// Objects in level
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 1, 3));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 1, 10));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 1, 17));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 18, 3));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 18, 10));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.PLANT, 18, 17));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.HOUSE, 4, 4));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.HOUSE, 12, 4));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.HOUSE, 4, 10));
        objects.add(new EnvironmentalObject(EnvironmentalObject.ObjectType.HOUSE, 12, 10));

        inputController = new InputController();

        board = new MapModel(10,10, obstacles, swamps, objects, directory);

        populateTileGraph();

        playerModel = new PlayerModel(new Vector2(100,100),0, directory);
        playerController = new PlayerController(playerModel);

        EnemyModel enemy = new EnemyModel(new Vector2(600, 300), 90, directory);
        enemyController = new EnemyController(enemy, playerModel, board.getTileState(0, 7), board.getTileState(4, 7), EnemyStates.PATROL, tileGraph, board);

        enemies.add(enemy);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        HUDcamera = new OrthographicCamera();
        HUDcamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // YOU WILL NEED TO MODIFY THIS NEXT LINE
        collisionController = new CollisionController(board, playerModel, enemies, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float v) {

    }

    @Override
    public void resize(int i, int i1) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private void populateTileGraph() {
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {

                if (board.getTileState(i, j).getType() != TileModel.TileType.OBSTACLE) {
                    tileGraph.addTile(board.getTileState(i, j));

                    for (TileModel neighbor : board.getTileNeighbors(i, j)) {
                        if (neighbor.getType() != TileModel.TileType.OBSTACLE) {
                            tileGraph.connectTiles(board.getTileState(i, j), neighbor);
                        }
                    }
                }
            }
        }
    }

    public void update(float delta) {
        inputController.readInput(null,null);
        playerController.update(inputController.getHorizontal(), inputController.getVertical(), inputController.didDecelerate(), inputController.didBoost(), inputController.didVacuum());
        enemyController.update();
        collisionController.update();

        Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        canvas.begin();
        canvas.center(camera, playerController.getModel().getPosition().x, playerController.getModel().getPosition().y);
        board.draw(canvas);
        playerController.draw(canvas, inputController.getHorizontal(), inputController.getVertical());
        enemyController.draw(canvas);
        canvas.end();
        canvas.drawUI(statusBarBGTexture,Color.WHITE, -100, 1300, 0, .5f,.5f, HUDcamera);
        if (playerController.hasResources()){
            canvas.drawUI(statusBarTexture,Color.WHITE, -100, 1300, 0, .5f,.5f, HUDcamera);
            canvas.drawUI(statusBarTexture,Color.WHITE, 250, 1300, 0, .5f,.5f, HUDcamera);
            canvas.drawUI(statusBarTexture,Color.WHITE, 300, 1300, 0, .5f,.5f, HUDcamera);
        }
    }

    public enum GameState {
        /** Before the game has started */
        INTRO,
        /** While we are playing the game */
        PLAY,
        /** When the ships is dead (but shells still work) */
        OVER
    }

}
