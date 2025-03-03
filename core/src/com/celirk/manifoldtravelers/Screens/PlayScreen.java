package com.celirk.manifoldtravelers.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.celirk.manifoldtravelers.ManifoldTravelers;
import com.celirk.manifoldtravelers.Scenes.Hud;
import com.celirk.manifoldtravelers.Sprites.Item.Item;
import com.celirk.manifoldtravelers.Sprites.Player;
import com.celirk.manifoldtravelers.Sprites.Projectile.Projectile;
import com.celirk.manifoldtravelers.Sprites.Tile.Spawner.Spawner;
import com.celirk.manifoldtravelers.Utils.B2WorldCreator;
import com.celirk.manifoldtravelers.Utils.WorldContactListener;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.socket.client.IO;
import io.socket.client.Socket;

public class PlayScreen implements Screen {
    private TextureAtlas atlas;

    private Socket socket;

    private boolean isHost;

    private ManifoldTravelers game;

    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer b2dr;

    private B2WorldCreator creator;

    private Player player;

    private Array<Player> enemies;

    private Array<Item> items;

    private Array<Projectile> projectiles;

    public PlayScreen(ManifoldTravelers game) {
        atlas = new TextureAtlas("playerMove.pack");

        this.game = game;

        gamecam = new OrthographicCamera();

        gamePort = new FitViewport(ManifoldTravelers.V_WIDTH / ManifoldTravelers.PPM, ManifoldTravelers.V_HEIGHT / ManifoldTravelers.PPM, gamecam);

        hud = new Hud(game.batch);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("tmp1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / ManifoldTravelers.PPM);

        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, 0), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        world.setContactListener(new WorldContactListener());

        player = new Player(this, 64, 64);

        items = new Array<Item>(false,128);

        projectiles = new Array<Projectile>(false, 128);

        connectSocket();
        configSocketEvents();
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();

        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        game.batch.begin();
        player.draw(game.batch);
        game.batch.end();

        hud.stage.draw();
    }

    public void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W) && player.b2body.getLinearVelocity().y <= 2)
            player.b2body.applyLinearImpulse(new Vector2(0, 0.2f), player.b2body.getWorldCenter(), true);

        if (Gdx.input.isKeyPressed(Input.Keys.S) && player.b2body.getLinearVelocity().y >= -2)
            player.b2body.applyLinearImpulse(new Vector2(0, -0.2f), player.b2body.getWorldCenter(), true);

        if (Gdx.input.isKeyPressed(Input.Keys.D) && player.b2body.getLinearVelocity().x <= 2)
            player.b2body.applyLinearImpulse(new Vector2(0.2f, 0), player.b2body.getWorldCenter(), true);

        if (Gdx.input.isKeyPressed(Input.Keys.A) && player.b2body.getLinearVelocity().x >= -2)
            player.b2body.applyLinearImpulse(new Vector2(-0.2f, 0), player.b2body.getWorldCenter(), true);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            player.shoot(Gdx.input.getX() - gamePort.getScreenWidth() / 2,
                    Gdx.graphics.getHeight() - Gdx.input.getY() - gamePort.getScreenHeight() / 2,
                    delta);
    }

    public void update(float dt) {
        // dt == delta time
        handleInput(dt);

        world.step(dt, 6, 2);

        player.update(dt);

        for(Projectile projectile : projectiles){
            projectile.update(dt);
        }

        for(Item item : items){
            item.update(dt);
        }

        for(Spawner spawner : creator.getSpawners()) {
            spawner.update(dt);
        }

        if(isHost) {
            //socket.emit("pushUpdate",)
        }
        else {

        }

        hud.update(dt);

        gamecam.position.x = player.b2body.getPosition().x;
        gamecam.position.y = player.b2body.getPosition().y;

        gamecam.update();
        renderer.setView(gamecam);
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
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
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getMap() {
        return map;
    }

    public void appendItem(Item item) {
        items.add(item);
    }
    public void removeItem(Item item) {
        items.removeValue(item,true);
    }
    public void appendProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }
    public void removeProjectile(Projectile projectile) {
        projectiles.removeValue(projectile, true);
    }

    public void connectSocket(){
        try{
            socket = IO.socket("http://localhost:5432");
            socket.connect();
        }catch (Exception e){
            System.out.println(e);
        }
    }
    public void configSocketEvents(){
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                Gdx.app.log("SocketIO","Connected");
            }
        }).on("isHost", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    isHost = data.getBoolean("isHost");
                }catch(JSONException e){
                    Gdx.app.log("SocketIO", "Error checking host");
                }
            }
        }).on("socketID", new Emitter.Listener(){
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "My ID:" + id);
                }catch(JSONException e){
                    Gdx.app.log("SocketIO", "Error getting ID");
                }
            }
        }).on("listPlayers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    JSONArray players = data.getJSONArray("players");
                    for (int i = 0; i < players.length(); i++) {
                        JSONObject player = players.getJSONObject(i);
                        float x = (float) player.getDouble("x");
                        float y = (float) player.getDouble("y");
                        float velocity_x = (float) player.getDouble("velocity_x");
                        float velocity_y = (float) player.getDouble("velocity_y");
                        float hit_point = (float) player.getDouble("hit_point");
                    }

                }catch(JSONException e){
                    Gdx.app.log("SocketIO", "Error getting players");
                }
            }
        }).on("playerDisconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                try {
                    String id = data.getString("id");
                    Gdx.app.log("SocketIO", "New Player Connected:" + id);
                }catch(JSONException e){
                    Gdx.app.log("SocketIO", "Error getting New PlayerID");
                }
            }
        });

    }
}
