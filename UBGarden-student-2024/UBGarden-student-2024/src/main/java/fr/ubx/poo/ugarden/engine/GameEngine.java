    /*
     * Copyright (c) 2020. Laurent Réveillère
     */

    package fr.ubx.poo.ugarden.engine;

    import fr.ubx.poo.ugarden.Utils;
    import fr.ubx.poo.ugarden.game.Direction;
    import fr.ubx.poo.ugarden.game.Game;
    import fr.ubx.poo.ugarden.game.Position;
    import fr.ubx.poo.ugarden.go.bonus.InsecticideSpray;
    import fr.ubx.poo.ugarden.go.bonus.Nest;
    import fr.ubx.poo.ugarden.go.decor.Decor;
    import fr.ubx.poo.ugarden.go.decor.DoorClosed;
    import fr.ubx.poo.ugarden.go.decor.DoorOpened;
    import fr.ubx.poo.ugarden.go.decor.ground.Grass;
    import fr.ubx.poo.ugarden.go.personage.Gardener;
    import fr.ubx.poo.ugarden.go.personage.Hornet;
    import fr.ubx.poo.ugarden.view.*;
    import javafx.animation.AnimationTimer;
    import javafx.application.Platform;
    import javafx.scene.Group;
    import javafx.scene.Scene;
    import javafx.scene.layout.Pane;
    import javafx.scene.layout.StackPane;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;
    import javafx.scene.text.Text;
    import javafx.scene.text.TextAlignment;
    import javafx.stage.Stage;

    import java.util.*;
    import java.util.Timer;


    public final class GameEngine {

        private static AnimationTimer gameLoop;
        private final Game game;
        private Timer timer=new Timer();
        private final Gardener gardener;
        private List<Sprite> sprites = new LinkedList<>();
        private final Set<Sprite> cleanUpSprites = new HashSet<>();
        private final Stage stage;
        private final Pane layer = new Pane();
        private StatusBar statusBar;
        private Input input;

        public GameEngine(Game game, final Stage stage) {
            this.stage = stage;
            this.game = game;
            this.gardener = game.getGardener();
            initialize();
            buildAndSetGameLoop();
        }

        private void initialize() {
            Group root = new Group();
            sprites = new LinkedList<>();
            int height = game.world().getGrid().height();
            int width = game.world().getGrid().width();
            int sceneWidth = width * ImageResource.size;
            int sceneHeight = height * ImageResource.size;
            Scene scene = new Scene(root, sceneWidth, sceneHeight + StatusBar.height);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm());

            stage.setScene(scene);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.hide();
            stage.show();

            input = new Input(scene);
            root.getChildren().add(layer);
            statusBar = new StatusBar(root, sceneWidth, sceneHeight);

            // Create sprites
            int currentLevel = game.world().currentLevel();

            for (var decor : game.world().getGrid().values()) {
                if(decor.getBonus() instanceof Nest){

                    timer=new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            Hornet hornet=new Hornet(game,decor.getPosition(), Utils.generateRandomString(12));
                            hornet.doRandomMove(game.world().getGrid().width(),game.world().getGrid().height());
                            sprites.add(new SpriteHornet(layer, hornet));

                            Position newRandomPositionForBomb;
                            do{
                                Random random = new Random();
                                int x = random.nextInt(game.world().getGrid().width()); // Random x coordinate between 0 (inclusive) and maxX (exclusive)
                                int y = random.nextInt(game.world().getGrid().height()); // Random y coordinate between 0 (inclusive) and maxY (exclusive)
                                newRandomPositionForBomb=new Position(game.world().currentLevel(),x, y);
                            }while(!(game.world().getGrid().get(newRandomPositionForBomb) instanceof Grass) || game.world().getGrid().get(newRandomPositionForBomb).getBonus()!=null);

                            InsecticideSpray bomb=new InsecticideSpray(game,newRandomPositionForBomb,game.world().getGrid().get(newRandomPositionForBomb));
                            game.world().getGrid().get(newRandomPositionForBomb).setBonus(bomb);
                            sprites.add(SpriteFactory.create(layer, bomb));
                        }
                    };

                    // Schedule the task to run every 1000 milliseconds (1 second)
                    timer.scheduleAtFixedRate(task, 2000, 10000);
                }
                    sprites.add(SpriteFactory.create(layer, decor));
                    decor.setModified(true);
                    var bonus = decor.getBonus();
                    if (bonus != null) {
                        sprites.add(SpriteFactory.create(layer, bonus));
                        bonus.setModified(true);

                }
            }

            List<Position> randomPositionsForHornets=Utils.generateRandomPositions(3,game.world().getGrid().width(), game.world().getGrid().height(),gardener.getPosition(),game.world().currentLevel());
            for(Position x : randomPositionsForHornets){
                Hornet hornet=new Hornet(game,x,Utils.generateRandomString(12));
                hornet.doRandomMove(game.world().getGrid().width(),game.world().getGrid().height());
                sprites.add(new SpriteHornet(layer, hornet));
            }
            sprites.add(new SpriteGardener(layer, gardener));

        }

        void buildAndSetGameLoop() {
            gameLoop = new AnimationTimer() {
                public void handle(long now) {
                    checkLevel();

                    // Check keyboard actions
                    processInput();

                    // Do actions
                    update(now);
                    checkCollision();

                    // Graphic update
                    cleanupSprites();
                    render();
                    statusBar.update(game);
                }
            };
        }


        private void checkLevel() {
            if (game.isSwitchLevelRequested()) {
                cleanupSprites();
                game.clearSwitchLevel();

                for(int i=0;i<game.world().getGrid().width();i++){
                    for(int j=0;j<game.world().getGrid().width();j++){
                        Position positionToCheck= new Position(game.world().currentLevel(),i,j);
                        Decor x=game.world().getGrid().get(positionToCheck);
                        if(x instanceof DoorOpened){
                            game.getGardener().setPosition(positionToCheck);
                            break;
                        }
                    }
                }

                timer.cancel();
                stage.close();
                initialize();
            }
        }

        private void checkCollision() {

                    for (Sprite item : sprites) {
                        if(gardener.getPosition().equals(item.getPosition()) && item instanceof SpriteHornet && Objects.equals(((Hornet) item.getGameObject()).getId(), ((Hornet) item.getGameObject()).getId())){
                            if(gardener.getInsecticideSprays()>0) gardener.setInsecticideSprays(gardener.getInsecticideSprays()-1);
                            else gardener.hurt(20);
                            int index = sprites.indexOf(item);
                            if (index != -1) {
                                System.out.println("Ouch!");
                                sprites.get(index).remove();
                                sprites.get(index).getGameObject().remove();
                            }
                    }

                        if(game.world().getGrid().get(item.getPosition()).getBonus() instanceof InsecticideSpray){
                                if(item instanceof SpriteHornet && Objects.equals(((Hornet) item.getGameObject()).getId(), ((Hornet) item.getGameObject()).getId())){
                                    int index = sprites.indexOf(item);
                                    if (index != -1) {
                                        System.out.println("Cool a hornet just got exterminated!");
                                        sprites.get(index).remove();
                                        sprites.get(index).getGameObject().remove();
                                        ((InsecticideSpray) game.world().getGrid().get(item.getPosition()).getBonus()).cancelTimer();
                                        game.world().getGrid().get(item.getPosition()).getBonus().remove();
                                    }
                                }
                        }

                        if(gardener.getPosition().equals(item.getPosition()) && item.getGameObject() instanceof DoorClosed){
                            if(gardener.getNumberOfKeys()>0){
                                game.requestSwitchLevel(game.world().currentLevel()+1);
                                gardener.setKeys(gardener.getNumberOfKeys()-1);
                            }
                        }

                }
        }
        private void processInput() {
            if (input.isExit()) {
                gameLoop.stop();
                Platform.exit();
                System.exit(0);
            } else if (input.isMoveDown()) {
                gardener.requestMove(Direction.DOWN);
            } else if (input.isMoveLeft()) {
                gardener.requestMove(Direction.LEFT);
            } else if (input.isMoveRight()) {
                gardener.requestMove(Direction.RIGHT);
            } else if (input.isMoveUp()) {
                gardener.requestMove(Direction.UP);
            }
            input.clear();
        }

        private void showMessage(String msg, Color color) {
            Text waitingForKey = new Text(msg);
            waitingForKey.setTextAlignment(TextAlignment.CENTER);
            waitingForKey.setFont(new Font(60));
            waitingForKey.setFill(color);
            StackPane root = new StackPane();
            root.getChildren().add(waitingForKey);
            Scene scene = new Scene(root, 400, 200, Color.WHITE);
            stage.setScene(scene);
            input = new Input(scene);
            stage.show();
            new AnimationTimer() {
                public void handle(long now) {
                    processInput();
                }
            }.start();
        }

        private void update(long now) {
            game.world().getGrid().values().forEach(decor -> decor.update(now));

            gardener.update(now);
            if (gardener.getPosition().equals(game.getHedgehogPosition())) {
                gameLoop.stop();
                showMessage("Victoire!", Color.GREEN);
            }else if (gardener.getEnergy() < 0) {
                gameLoop.stop();
                showMessage("Perdu!", Color.RED);
            }

        }

        public void cleanupSprites() {
            sprites.forEach(sprite -> {
                if (sprite.getGameObject().isDeleted()) {
                    cleanUpSprites.add(sprite);
                }
            });
            cleanUpSprites.forEach(Sprite::remove);
            sprites.removeAll(cleanUpSprites);
            cleanUpSprites.clear();
        }

        private void render() {
            sprites.forEach(Sprite::render);
        }

        public void start() {
            gameLoop.start();
        }
    }