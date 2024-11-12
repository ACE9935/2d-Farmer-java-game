/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.ugarden.go.personage;

import fr.ubx.poo.ugarden.game.Direction;
import fr.ubx.poo.ugarden.game.Game;
import fr.ubx.poo.ugarden.game.Position;
import fr.ubx.poo.ugarden.go.GameObject;
import fr.ubx.poo.ugarden.go.Movable;
import fr.ubx.poo.ugarden.go.WalkVisitor;
import fr.ubx.poo.ugarden.go.Walkable;
import fr.ubx.poo.ugarden.go.decor.Decor;
import fr.ubx.poo.ugarden.go.decor.Flowers;
import fr.ubx.poo.ugarden.go.decor.Tree;

import java.util.Timer;
import java.util.TimerTask;

public class Hornet extends Decor implements Movable, Walkable, WalkVisitor {
    private Direction direction;
    private Timer timer;
    private final Game game;
    private final String id;

    public Hornet(Game game,Position position,String id) {

        super(position);
        this.id=id;
        this.game=game;
        this.direction = Direction.RIGHT;
    }

    public String getId() {
        return id;
    }
    @Override
    public void doMove(Direction direction) {

    }
    @Override
    public final boolean canMove(Direction direction) {
        // TO UPDATE
        return true;
    }
    @Override
    public final boolean canWalkOn(Flowers flowers) {
        // TO UPDATE
        return true;
    }

    public void doRandomMove(int width, int height) {
        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Direction x;
                Position newHornetPosition;
                do{
                    x=Direction.random();
                    newHornetPosition=x.nextPosition(getPosition(),1);

                }while(game.world().getGrid().get(newHornetPosition) instanceof Tree || newHornetPosition.x()<0 || newHornetPosition.y()<0 ||newHornetPosition.x()>=width || newHornetPosition.y()>=height);

                setPosition(newHornetPosition);
                direction=x;
            }
        };

        // Schedule the task to run every 1000 milliseconds (1 second)
        timer.scheduleAtFixedRate(task, 1000, game.configuration().hornetMoveFrequency()* 1000L);
    }

    public Direction getDirection() {
        return direction;
    }
}