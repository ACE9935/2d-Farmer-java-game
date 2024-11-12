/*
 * Copyright (c) 2020. Laurent Réveillère
 */
package fr.ubx.poo.ugarden.go.bonus;

import fr.ubx.poo.ugarden.Utils;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.decor.Decor;
import fr.ubx.poo.ugarden.go.personage.Gardener;
import fr.ubx.poo.ugarden.go.personage.Hornet;
import fr.ubx.poo.ugarden.view.Sprite;
import fr.ubx.poo.ugarden.view.SpriteHornet;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Nest extends Bonus {
    public Nest(Position position, Decor decor) {
        super(position, decor);
    }


}
