package com.celirk.manifoldtravelers.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.celirk.manifoldtravelers.Screens.PlayScreen;
import com.celirk.manifoldtravelers.Sprites.Ground;
import com.celirk.manifoldtravelers.Sprites.Portalable;
import com.celirk.manifoldtravelers.Sprites.WeaponSpawner;

public class B2WorldCreator {
    public B2WorldCreator(PlayScreen screen) {
        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;
        TiledMap map = screen.getMap();

        for (MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {

            new Ground(screen, object);
        }

        for (MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {

            new Portalable(screen, object);
        }

        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {

            new WeaponSpawner(screen, object);
        }
    }
}
