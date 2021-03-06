package model.map.specification.texture;

import javafx.scene.image.Image;

public class CryptTexture extends TextureHandler {

    public CryptTexture() {
        this.corridor = new Image("map/crypt/CryptCorridor.png");
        this.turningCorridor = new Image("map/crypt/CryptTurningCorridor.png");
        this.corridorCrossing = new Image("map/crypt/CryptCorridorCrossing.png");
        this.stairs = new Image("map/crypt/CryptStairs.png");
        this.room = new Image("map/crypt/CryptRoom.png");
        this.wall = new Image("map/crypt/CryptWall.png");
        this.chest = new Image("map/crypt/SimpleChest.png");
    }
}
