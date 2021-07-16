import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Game extends Application {
    /**
     * Hlavná trieda pre spustenie hry.
     */

    static final int width = 1280;
    static final int height = 720;
    static final int framerate = 60;
    private double mX;
    private double mY;
    private boolean gameOver = false;
    private boolean gameWon = false;
    static int tick = 0;
    private final Random rand = new Random();
    private final Map<String, Image> images = new HashMap<>();
    private AudioClip bgMusic = new AudioClip(this.getClass().getResource("sounds/crimsonland_theme.mp3").toExternalForm());

    public static void main(String[] args) { launch(args); }

    /**
     * Spustí hru a nastaví ovládanie.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        loadImages();
        MyCanvas can = new MyCanvas();
        Pane root = new Pane(can);

        root.setFocusTraversable(true);
        primaryStage.setTitle("Crimsonland");
        Scene scene = new Scene(root, width, height);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("imgs/misc/icon.png"));

        scene.setCursor(Cursor.NONE);

        scene.widthProperty().addListener(ov -> { can.setWidth(scene.getWidth()); can.paint(); });
        scene.heightProperty().addListener(ov -> { can.setHeight(scene.getHeight()); can.paint(); });

        primaryStage.show();
        can.paint();

        root.setOnMousePressed(ev -> {
            can.player.shooting(true);
        });

        root.setOnMouseReleased(ev -> {
            can.player.shooting(false);
        });

        scene.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.W) {
                can.changeDirection(0, true);
            }
            if (ev.getCode() == KeyCode.S) {
                can.changeDirection(1, true);
            }
            if (ev.getCode() == KeyCode.A) {
                can.changeDirection(2, true);
            }
            if (ev.getCode() == KeyCode.D) {
                can.changeDirection(3, true);
            }
            if (ev.getCode() == KeyCode.SPACE) {
                if (gameOver || gameWon) {
                    can.reset();
                }
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, ev -> {
            if (ev.getCode() == KeyCode.W) {
                can.changeDirection(0, false);
            }
            if (ev.getCode() == KeyCode.S) {
                can.changeDirection(1, false);
            }
            if (ev.getCode() == KeyCode.A) {
                can.changeDirection(2, false);
            }
            if (ev.getCode() == KeyCode.D) {
                can.changeDirection(3, false);
            }
            if (ev.getCode() == KeyCode.R) {
                can.player.reload();
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, ev -> {
            mX = ev.getX();
            mY = ev.getY();
        });

        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, ev -> {
            mX = ev.getX();
            mY = ev.getY();
        });

        Timeline tl = new Timeline(new KeyFrame(new Duration((double) 1000/framerate), e -> { tick++; can.paint(); }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
    }

    /**
     * Vopred načíta obrázky a gify.
     */
    private void loadImages() {
        images.put("blood0", new Image("imgs/blood/blood0.png"));
        images.put("blood1", new Image("imgs/blood/blood1.png"));
        images.put("blood2", new Image("imgs/blood/blood2.png"));
        images.put("cursor", new Image("imgs/misc/cursor.png"));
        images.put("cursorFast", new Image("imgs/misc/cursorFast.png"));
        images.put("cursorReloading", new Image("imgs/misc/cursorReloading.png"));
        images.put("cursorReloadingFast", new Image("imgs/misc/cursorReloadingFast.png"));
        images.put("gameover", new Image("imgs/misc/gameover.gif"));
        images.put("gameoverR", new Image("imgs/misc/gameoverR.png"));
        images.put("gamewon", new Image("imgs/misc/gamewon.png"));
        images.put("bg", new Image("imgs/misc/bg.jpg"));
        images.put("ui", new Image("imgs/misc/ui.png"));
        images.put("gun1", new Image("imgs/guns/gun1.png"));
        images.put("gun2", new Image("imgs/guns/gun2.png"));
        images.put("gun3", new Image("imgs/guns/gun3.png"));
        images.put("gun1h", new Image("imgs/guns/gun1h.png"));
        images.put("gun2h", new Image("imgs/guns/gun2h.png"));
        images.put("gun3h", new Image("imgs/guns/gun3h.png"));
        images.put("p1", new Image("imgs/projectiles/p1.png"));
        images.put("pFire", new Image("imgs/projectiles/fireball.png"));
        images.put("perkDmg", new Image("imgs/perks/dmg.png"));
        images.put("perkInv", new Image("imgs/perks/invinc.png"));
        images.put("perkReload", new Image("imgs/perks/reload.png"));
        images.put("zombieIdle", new Image("imgs/enemies/enemyZombieIdle.png"));
        images.put("gunIdle", new Image("imgs/enemies/enemyGunIdle.png"));
        images.put("spiderIdle", new Image("imgs/enemies/enemySpiderIdle.png"));
    }

    class MyCanvas extends Canvas {
        /**
         * Hlavná trieda pre vykresľovanie grafiky a prácu s ostatnými triedami.
         */

        private Player player;
        private GraphicsContext g;
        private final List<Projectile> projectiles = new ArrayList<>();
        private final List<Enemy> enemies = new ArrayList<>();
        private final List<Image> bloodAnimationGif = new ArrayList<>();    // x,y,frame
        private final List<List<Double>> bloodAnimationCoords = new ArrayList<>();    // x,y,frame
        private final List<List<Double>> bloodImage = new ArrayList<>();
        private final List<Drop> drops = new ArrayList<>();
        private Level level = new Level(1);
        private final List<Perk> perks = new ArrayList<>();
        private List<AudioClip> bloodSounds = new ArrayList<>();

        public MyCanvas() {
            g = getGraphicsContext2D();
            setWidth(width);
            setHeight(height);
            player = new Player(100, new Gun1());
            bloodSounds.add(new AudioClip(this.getClass().getResource("sounds/blood1.mp3").toExternalForm()));
            bloodSounds.add(new AudioClip(this.getClass().getResource("sounds/blood2.mp3").toExternalForm()));
            bloodSounds.add(new AudioClip(this.getClass().getResource("sounds/blood3.mp3").toExternalForm()));
            bgMusic.setVolume(0.5);
            bgMusic.play();
        }

        /**
         * Slúži pre reštart hry. Vyčistí polia a premenné.
         */
        public void reset() {
            projectiles.clear();
            enemies.clear();
            bloodAnimationGif.clear();
            bloodAnimationCoords.clear();
            bloodImage.clear();
            drops.clear();
            level = new Level(1);
            perks.clear();
            player = new Player(100, new Gun1());
            gameOver = false;
            gameWon = false;
        }

        /**
         * Zobrazí hráča a zbraň.
         */
        public void drawPlayer() {
            // body
            double w = player.image.getWidth();
            double h = player.image.getHeight();
            g.setTransform(new Affine(Affine.rotate(getGunAngle(), player.centerX(), player.centerY())));
            if (player.moving()) {
                g.drawImage(player.image, player.x-w/6, player.y-h/6, w/3, h/3);
            } else {
                g.drawImage(player.imageIdle, player.x-w/6, player.y-h/6, w/3, h/3);
            }
            // gun
            g.setStroke(Color.RED);
            double gw = player.gun.img.getWidth();
            double gh = player.gun.img.getHeight();
            g.drawImage(player.gun.img, player.centerX()-gw/8, player.centerY()-gh/8, gw/5, gh/5);
            g.setTransform(new Affine());
        }

        /**
         * Zobrazí nepriateľov.
         */
        public void drawEnemies() {
            for (Enemy e : enemies) {
                double angle = Math.toDegrees(Math.atan2(player.centerY()-e.centerY(), player.centerX()-e.centerX()));
                if (e instanceof EnemySpider) angle -= 90;
                g.setTransform(new Affine(Affine.rotate(angle, e.centerX(), e.centerY())));
                if (e.moving(player.centerX(), player.centerY())) {
                    g.drawImage(e.image, e.x, e.y);
                } else {
                    g.drawImage(e.imageIdle, e.x, e.y);
                }
                g.setTransform(new Affine());
            }
        }

        /**
         * Pohyb nepriateľov.
         */
        public void moveEnemies() {
            for (Enemy e : enemies) {
                e.move(player.centerX(), player.centerY());
            }
        }

        /**
         * Vykreslí grafické rozhranie - healthbary, kurzor, aktuálnu zbraň a level
         */
        public void drawUI() {
            // player healthbar
            drawHealthbar(player.centerX(), player.y, player.image.getHeight()/5, player.health, player.health/100, 1);
            // enemy healthbar
            for (Enemy e : enemies) {
                drawHealthbar(e.centerX(), e.y, e.image.getWidth(), e.health, e.healthPercentage(), 0.5);
            };
            // cursor
            Image cursor;
            Image cursorReloading;
            if (player.fastReload) {
                cursorReloading = images.get("cursorReloadingFast");
                cursor = images.get("cursorFast");
            }
            else {
                cursorReloading = images.get("cursorReloading");
                cursor = images.get("cursor");
            }
            if (!player.reloading()) {
                g.drawImage(cursor, mX-cursor.getWidth()/2, mY-cursor.getHeight()/2);
            } else {
                g.drawImage(cursorReloading, mX-cursor.getWidth()/2, mY-cursor.getHeight()/2);
            }
            // ammo left
            g.setFont(Font.font("Arial", 20));
            g.setTextAlign(TextAlignment.CENTER);
            g.setFill(Color.CYAN);
            g.fillText(String.valueOf(player.ammoLeft()), mX, mY-cursor.getHeight());
            // current gun
            double w = player.gun.imgH.getWidth();
            double h = player.gun.imgH.getHeight();
            g.drawImage(player.gun.imgH, 5, 10, w/4, h/3);
            // current level
            g.setFill(Color.WHITE);
            g.fillText("LEVEL " + (level.round), width/2, 20);
            g.setStroke(Color.CYAN);
            g.strokeRect(width/2-120, 30, 240, 15);
            double percentage = (double) (level.totalEnemies-level.currEnemies) / level.totalEnemies;
            g.fillRect(width/2-120, 30, 240*percentage, 15);
        }

        /**
         * Vykreslí healthbar s danými vlastnosťami.
         * @param x
         * @param y
         * @param size
         * @param health
         * @param hPercent
         * @param scale
         */
        public void drawHealthbar(double x, double y, double size, double health, double hPercent, double scale) {
            double yPush = 0;
            if (scale == 1) {
                size *= 1.5;
                yPush = 15;
            }
            g.setStroke(Color.BLACK);
            g.strokeRect(x-size*scale, y-size/2-yPush, size*2*scale, height/70);
            if (scale == 1 && player.invincible) {
                g.setFill(Color.GOLD);
            } else if (health > 25) {
                g.setFill(Color.GREEN);
            } else {
                g.setFill(Color.RED);
            }
            g.fillRect(x-size*scale, y-size/2-yPush,size*2*scale*hPercent, height/70);
        }

        /**
         * Zobrazí projektily zbraní.
         */
        public void drawProjectiles() {
            for (Projectile p : projectiles) {
                p.move();
                double w = p.image.getWidth();
                double h = p.image.getHeight();
                if (player.bonusDmg && !p.enemy) {
                    g.setTransform(new Affine(Affine.rotate(p.angle, p.x, p.y)));
                    g.drawImage(images.get("pFire"), p.x-w/3, p.y-h/6, w, h/5);
                } else {
                    g.setTransform(new Affine(Affine.rotate(p.angle+90, p.x, p.y)));
                    g.drawImage(p.image, p.x-w/6, p.y-h/6, w/3, h/3);
                }
            }
            g.setTransform(new Affine());
        }

        /**
         * Zobrazí dropy (zbrane a perky).
         */
        public void drawDrops() {
            for (Drop drop : drops) {
                g.setTransform(new Affine(Affine.rotate(drop.angle, drop.x, drop.y)));
                g.drawImage(drop.img, drop.x-drop.width()/2, drop.y-drop.height()/2, drop.width(), drop.height());
                drop.rotate();
                g.setTransform(new Affine());
            }
        }

        /**
         * Despawnuje dropy (zbrane a perky).
         */
        public void dropDespawn() {
            List<Drop> toRemove = new ArrayList<>();
            for (Drop drop : drops) {
                if (drop.tickDespawn == tick) {
                    toRemove.add(drop);
                }
            }
            drops.removeAll(toRemove);
        }

        /**
         * Stará sa o zdvihnutie zraní a perkov zo zeme.
         */
        public void dropPickup() {
            List<Drop> toRemove = new ArrayList<>();
            for (Drop drop : drops) {
                if (Math.sqrt(Math.pow(drop.x-player.centerX(),2) + Math.pow(drop.y-player.centerY(),2)) < drop.width()/2) {
                    if (drop instanceof DropGun) {
                        player.gun = drop.gun;
                        player.ammoLeft = drop.gun.magSize;
                        player.reloadTick = -1;
                        new AudioClip(this.getClass().getResource("sounds/gunPickup.mp3").toExternalForm()).play();
                    } else if (drop instanceof DropPerk) {
                        drop.perk.start(tick, player.gun);
                        if (drop.perk instanceof PerkFastReload) player.fastReload = true;
                        else if (drop.perk instanceof PerkBonusDamage) player.bonusDmg = true;
                        else if (drop.perk instanceof PerkInvincibility) player.invincible = true;
                        new AudioClip(this.getClass().getResource("sounds/dropPickup.mp3").toExternalForm()).play();
                    }
                    toRemove.add(drop);
                }
            }
            drops.removeAll(toRemove);
        }

        /**
         * Vráti uhol pre rotáciu zbrane za kurzorom.
         * @return
         */
        private double getGunAngle() {
            return Math.toDegrees(Math.atan2(mY-player.centerY(), mX-player.centerX()));
        }

        /**
         * Stará sa o kolíziu projektilov zo zbraní s nepriateľmi a hráčom.
         */
        public void projectileCollision() {
            List<Enemy> toRemoveEnemy = new ArrayList<>();
            List<Projectile> toRemoveProjectile = new ArrayList<>();
            for (Projectile p : projectiles) {
                if (p.enemy) {
                    if (collides(p, player.centerX(), player.centerY(), p.image.getWidth())) {
                        player.health -= 10;
                        if (player.health < 0) gameOver = true;
                        toRemoveProjectile.add(p);
                    }
                } else {
                    for (Enemy e : enemies) {
                        if (collides(p, e.centerX(), e.centerY(), e.image.getWidth())) {
                            e.health -= player.gun.dmg;
                            toRemoveProjectile.add(p);
                            if (e.health <= 0) {
                                bloodSounds.get(rand.nextInt(bloodSounds.size())).play();
                                addBlood(e.centerX(), e.centerY());
                                toRemoveEnemy.add(e);
                                DropGun dropGun = e.dropGun();
                                DropPerk dropPerk = e.dropPerk();
                                if (dropPerk != null) {
                                    drops.add(dropPerk);
                                    perks.add(dropPerk.perk);
                                } else if (dropGun != null) {
                                    drops.add(dropGun);
                                }
                                level.killEnemy();
                            }
                        }
                    }
                }
            }
            enemies.removeAll(toRemoveEnemy);
            projectiles.removeAll(toRemoveProjectile);
        }

        /**
         * Zistí, či je projektil v kolízii s daným nepriateľom / hráčom.
         * @param p
         * @param x
         * @param y
         * @param r
         * @return
         */
        private boolean collides(Projectile p, double x, double y, double r) {
            return Math.sqrt(Math.pow(p.x-x,2) + Math.pow(p.y-y,2)) < r;
        }

        /**
         * Stará sa o damage hráčovi, ak sa ho dotýkajú nepriatelia.
         */
        public void enemyCollision() {
            for (Enemy e : enemies) {
                if (Math.sqrt(Math.pow(player.centerX()-e.centerX(),2) + Math.pow(player.centerY()-e.centerY(),2)) < player.image.getWidth()/6) {
                    if (!player.invincible) {
                        player.health -= e.damage;
                    }
                    if (player.health < 0) {
                        addBlood(player.centerX(), player.centerY());
                        new AudioClip(this.getClass().getResource("sounds/oof.mp3").toExternalForm()).play();
                        gameOver = true;
                    }
                }
            }
        }

        /**
         * Spawnuje nepriateľov v náhodných rohoch.
         */
        public void spawnEnemy() {
            if (level.totalEnemies > level.spawnedEnemies && (tick % (level.respawnInterval*framerate)) == 0) {
                level.spawnEnemy();
                int randomEnemyNum = level.enemyTypes.get(rand.nextInt(level.enemyTypes.size()));
                if (randomEnemyNum == 1) enemies.add(new EnemyZombie(rand.nextInt(4)));
                if (randomEnemyNum == 2) enemies.add(new EnemyGun(rand.nextInt(4)));
                if (randomEnemyNum == 3) enemies.add(new EnemySpider(rand.nextInt(4)));
            }
        }

        /**
         * Zistí, či nebol prejdený level.
         */
        public void checkLevelPass() {
            if (level.currEnemies == 0) {
                if (level.round == 3) {
                    gameWon = true;
                } else {
                    level.advanceLevel();
                }
            }
        }

        /**
         * Pridá do listu informácie pre vykreslenie krvi pri zabití.
         * @param x
         * @param y
         */
        public void addBlood(double x, double y) {
            List<Double> temp = new ArrayList<>();
            temp.add(x); temp.add(y); temp.add(Math.toDegrees(Math.atan2(y-player.centerY(), x-player.centerX()))+90);
            bloodAnimationCoords.add(temp);
            bloodAnimationGif.add(new Image("imgs/blood/splatter.gif"));
            List<Double> temp2 = new ArrayList<>();
            temp2.add(x); temp2.add(y); temp2.add((double) rand.nextInt(2));
            bloodImage.add(temp2);
        }

        /**
         * Zobrazí krv na zemi (animáciu a statický obrázok).
         */
        public void drawBlood() {
            for (List<Double> l : bloodImage) {
                double x = l.get(0);
                double y = l.get(1);
                int num = (int) Math.round(l.get(2));
                g.drawImage(images.get("blood"+num),x-images.get("blood"+num).getWidth()/2,y-images.get("blood"+num).getHeight()/2);

            }
            for (int i = 0; i < bloodAnimationGif.size(); i++) {
                Image gif = bloodAnimationGif.get(i);
                double x = bloodAnimationCoords.get(i).get(0);
                double y = bloodAnimationCoords.get(i).get(1);
                double angle = bloodAnimationCoords.get(i).get(2);
                g.setTransform(new Affine(Affine.rotate(angle, x, y)));
                g.drawImage(gif, x-gif.getWidth()/2, y-gif.getHeight());
                g.setTransform(new Affine());
            }
        }

        /**
         * Ruší efekty perkov ak uplynul čas.
         */
        public void perkExpire() {
            for (Perk perk : perks) {
                if (perk.expired()) {
                    perk.reset();
                    if (perk instanceof PerkFastReload) {
                        player.fastReload = false;
                    } else if (perk instanceof PerkBonusDamage) {
                        player.bonusDmg = false;
                    } else if (perk instanceof PerkInvincibility) {
                        player.invincible = false;
                    }
                }
            }
        }

        /**
         * Smer pohybu hráča po stlačení WSAD.
         * @param ix
         * @param on
         */
        public void changeDirection(int ix, Boolean on) {
            player.directions.set(ix, on);
        }

        /**
         * Strieľanie hráča a nepriateľov (vytváranie inštancií Projectile).
         */
        public void shoot() {
            if (player.canShoot()) {
                double dx = mX - player.centerX();
                double dy = mY - player.centerY();
                double angleRandomdness;    // angle variation when moving
                if (!player.moving()) {
                    angleRandomdness = 0;
                } else {
                    angleRandomdness = (double) 1/20 - rand.nextDouble()/10;
                }
                double angle = Math.atan2(dy, dx) + angleRandomdness;
                double angleDegrees = Math.toDegrees(Math.atan2(mY-player.centerY(), mX-player.centerX()));
                Image pImage = player.gun.projectileImage;
                projectiles.add(new Projectile(player.centerX(), player.centerY(), angle, 20, pImage, angleDegrees, false));
                player.gun.sound.play();
                player.shootAmmo();
            }

            for (Enemy e : enemies) {
                if (e instanceof EnemyGun) {
                    double dx = player.centerX() - e.centerX();
                    double dy = player.centerY() - e.centerY();
                    double angle = Math.atan2(dy, dx);
                    double angleDegrees = Math.toDegrees(Math.atan2(mY-player.centerY(), mX-player.centerX()));
                    if ((e.spawnedTick + tick) % (framerate * (rand.nextInt(2)+3)) == 0) {
                        projectiles.add(new Projectile(e.centerX(), e.centerY(), angle, 20, new Image("imgs/projectiles/enemyP.png"), angleDegrees, true));
                    }
                }
            }
        }

        /**
         * Vykreslenie grafiky, pohyb a ostatné metódy v triede.
         */
        public void paint() {
            double w = getWidth();
            double h = getHeight();

            if (gameOver || gameWon) {
                g.drawImage(images.get("bg"), 0, 0);
                g.drawImage(images.get("ui"), 0, 0);
                drawUI();
                drawEnemies();
                drawPlayer();
                drawBlood();
                g.setFill(Color.color(0, 0, 0, 0.5));
                g.fillRect(0, 0, width, height);
                if (gameOver) {
                    g.drawImage(images.get("gameover"), width/2-images.get("gameover").getWidth()/2, height/2-images.get("gameover").getHeight()/2);
                } else if (gameWon) {
                    g.drawImage(images.get("gamewon"), width/2-images.get("gamewon").getWidth()/2, height/2-images.get("gamewon").getHeight()/2);
                }
                g.drawImage(images.get("gameoverR"), width/2-images.get("gameoverR").getWidth()/2, height/2-images.get("gameoverR").getHeight()/2+100);
            } else {
                g.setFill(Color.WHITE);
                g.fillRect(0,0,w,h);
                g.drawImage(images.get("bg"), 0, 0);
                g.drawImage(images.get("ui"), 0, 0);
                drawBlood();
                drawPlayer();
                drawEnemies();
                drawDrops();
                drawProjectiles();
                drawUI();

                player.checkReload();
                spawnEnemy();
                player.move();
                moveEnemies();
                shoot();
                perkExpire();
                enemyCollision();
                dropDespawn();
                dropPickup();
                projectileCollision();
                checkLevelPass();
            }

            if (tick % (framerate*3) == 0) {
                if (!bgMusic.isPlaying()) {
                    bgMusic.play();
                }
            }
        }

    }

    static class Player {
        /**
         * Trieda hráča.
         */

        private final List<Boolean> directions = Arrays.asList(false,false,false,false);  // W,S,A,D
        private double health;
        private double x, y, dx, dy;
        private Gun gun;
        private int ammoLeft;
        private int reloadTick = -1;
        private boolean shooting = false;
        private int startShootingTick = 0;
        private int lastShotTick;
        private boolean fastReload = false;
        private boolean bonusDmg = false;
        private boolean invincible = false;
        private Image image;
        private Image imageIdle;

        public Player(double hp, Gun gun) {
            this.health = hp;
            this.x = (double) width / 2;
            this.y = (double) height / 2;
            this.gun = gun;
            this.ammoLeft = gun.magSize;
            this.image = new Image("imgs/misc/chara.gif");
            this.imageIdle = new Image("imgs/misc/charaIdle.png");
        }

        /**
         * Pohyb hráča do strán. Obsahuje akceleráciu a deceleráciu.
         */
        public void move() {
            Boolean W = directions.get(0);
            Boolean S = directions.get(1);
            Boolean A = directions.get(2);
            Boolean D = directions.get(3);
            double accel = 0.75;
            double speed = (double) width / 250;
            if (W && dy > -speed) dy -= accel;
            if (S && dy < speed) dy += accel;
            if (A && dx > -speed) dx -= accel;
            if (D && dx < speed) dx += accel;
            if (Math.abs(dx) + Math.abs(dy) > speed) {
                dx /= 1.2;
                dy /= 1.2;
            }
            if (!W && !S) dy /= 2;
            if (!A && !D) dx /= 2;
            if (Math.abs(dx) < 0.1) dx = 0;
            if (Math.abs(dy) < 0.1) dy = 0;
            x += dx;
            y += dy;
        }

        /**
         * Vycentrovaná X pozícia hráča.
         * @return
         */
        public double centerX() {
            return x + image.getWidth()/10;
        }

        /**
         * Vycentrovaná Y pozícia hráča.
         * @return
         */
        public double centerY() {
            return y + image.getWidth()/10;
        }

        /**
         * Prebije zbraň, ak sa momentálne neprebíja a chýbajú náboje.
         */
        public void reload() {
            if (!reloading() && ammoLeft() != gun.magSize) {
                reloadTick = tick;
                gun.reloadSound.play();
            }
        }

        /**
         * Vráti, či nie je prázdny zásobník.
         * @return
         */
        public boolean hasAmmo() {
            return ammoLeft > 0;
        }

        /**
         * Odčíta náboj a zapamätá si kedy bol vystrelený.
         */
        public void shootAmmo() {
            ammoLeft--;
            lastShotTick = tick;
        }

        /**
         * Vráti, koľko nábojov zostáva v zásobníku.
         * @return
         */
        public int ammoLeft() {
            return ammoLeft;
        }

        /**
         * Vráti, či sa momentálne prebíja zbraň.
         * @return
         */
        public boolean reloading() {
            return reloadTick != -1;
        }

        /**
         * Ak sa dokončilo prebíjanie, doplní sa zásobník.
         */
        public void checkReload() {
            if (tick == reloadTick+(framerate*gun.reloadTime)) {
                reloadTick = -1;
                ammoLeft = gun.magSize;
            }
        }

        /**
         * Zapamätá si, kedy sa začalo strielať (stlačenie LMB).
         * @param shooting
         */
        public void shooting(boolean shooting) {
            this.shooting = shooting;
            startShootingTick = tick+1;
        }

        /**
         * Zistí, či je možné strielať (neprebíja sa, nie je prázdny zásobník a nebolo vystrelené skôr, ako rýchlosť streľby danej zbrane.
         * @return
         */
        public boolean canShoot() {
            return !reloading() && shooting && hasAmmo() && (tick - startShootingTick) % gun.rateOfFire == 0
                    && (tick - lastShotTick) > gun.rateOfFire;
        }

        /**
         * Vráti, či sa hráč pohybuje.
         * @return
         */
        public boolean moving() {
            return dx != 0 || dy != 0;
        }

    }

    static class Gun {
        /**
         * Trieda zbrane so základnými premennými.
         */

        protected Image img;
        protected Image imgH;
        protected Image projectileImage;
        protected AudioClip sound;
        protected AudioClip reloadSound;
        protected int rateOfFire;
        protected double dmg;
        protected double dmgOrig;
        protected int magSize;
        protected double reloadTime;
        protected double reloadTimeOrig;

    }

    class Gun1 extends Gun {
        /**
         * Podtrieda prvej zbrane.
         */

        public Gun1() {
            this.img = images.get("gun1");
            this.imgH = images.get("gun1h");
            this.projectileImage = images.get("p1");
            this.sound = new AudioClip(this.getClass().getResource("sounds/gun1sound.mp3").toExternalForm());
            this.reloadSound = new AudioClip(this.getClass().getResource("sounds/gun1reload.mp3").toExternalForm());
            this.rateOfFire = 12;
            this.dmg = 25;
            this.dmgOrig = dmg;
            this.magSize = 10;
            this.reloadTime = 0.75;
            this.reloadTimeOrig = reloadTime;
        }

    }

    class Gun2 extends Gun {
        /**
         * Podtrieda druhej zbrane.
         */

        public Gun2() {
            this.img = images.get("gun2");
            this.imgH = images.get("gun2h");
            this.projectileImage = new Image("imgs/projectiles/green.gif");
            this.sound = new AudioClip(this.getClass().getResource("sounds/gun2sound.mp3").toExternalForm());
            this.reloadSound = new AudioClip(this.getClass().getResource("sounds/gun2reload.mp3").toExternalForm());
            this.rateOfFire = 4;
            this.dmg = 15;
            this.dmgOrig = dmg;
            this.magSize = 25;
            this.reloadTime = 1;
            this.reloadTimeOrig = reloadTime;
        }

    }

    class Gun3 extends Gun {
        /**
         * Podtrieda tretej zbrane.
         */

        public Gun3() {
            this.img = images.get("gun3");
            this.imgH = images.get("gun3h");
            this.projectileImage = new Image("imgs/projectiles/fireball.gif");
            this.sound = new AudioClip(this.getClass().getResource("sounds/gun3sound.mp3").toExternalForm());
            this.reloadSound = new AudioClip(this.getClass().getResource("sounds/gun3reload.mp3").toExternalForm());
            this.rateOfFire = 5;
            this.dmg = 50;
            this.dmgOrig = dmg;
            this.magSize = 15;
            this.reloadTime = 2;
            this.reloadTimeOrig = reloadTime;
        }

    }

    class Enemy {
        /**
         * Trieda nepriateľa.
         */

        protected Image image;
        protected Image imageIdle;
        protected double health;
        protected double fullHealth;
        private double x, y;
        protected double damage;
        protected double speed;
        Map<Gun, Double> dropGun = new HashMap<>();
        Map<Perk, Double> dropPerk = new HashMap<>();
        List<Gun> gunDrops = new ArrayList<>();
        List<Perk> perkDrops = new ArrayList<>();
        protected double gunChance;
        protected double perkChance;
        protected int spawnedTick;

        public Enemy(int corner) {
            if (corner == 0) {
                this.x = -20;   this.y = -20;
            } else if (corner == 1) {
                this.x = width+20;   this.y = -20;
            } else if (corner == 2) {
                this.x = -20;   this.y = height+20;
            } else if (corner == 3) {
                this.x = width+20;   this.y = height+20;
            }
        }

        /**
         * Pohyb nepriateľa smerom k daným súradniciam.
         * @param toX
         * @param toY
         */
        public void move(double toX, double toY) {
            double dx = toX - centerX();
            double dy = toY - centerY();
            if (moving(toX, toY)) {
                double angle = Math.atan2(dy, dx);
                x += Math.cos(angle) * speed;
                y += Math.sin(angle) * speed;
            }
        }

        /**
         * Zistí, či sa nepriateľ pohybuje (pre zobrazenie animácie alebo statického obrázku).
         * @param toX
         * @param toY
         * @return
         */
        public boolean moving(double toX, double toY) {
            double dx = toX - centerX();
            double dy = toY - centerY();
            return Math.abs(dx) + Math.abs(dy) > 2;
        }

        /**
         * X súradnica stredu nepriateľa.
         * @return
         */
        public double centerX() {
            return x + image.getWidth()/2;
        }

        /**
         * Y súradnica stredu nepriateľa.
         * @return
         */
        public double centerY() {
            return y + image.getWidth()/2;
        }

        /**
         * Vráti koľko percent HP zostáva.
         * @return
         */
        public double healthPercentage() {
            return health/fullHealth;
        }

        /**
         * Vráti drop zbrane na danej pozícii.
         * @return
         */
        public DropGun dropGun() {
            if (rand.nextDouble() < gunChance) {
                return new DropGun(centerX(), centerY(), 0, gunDrops.get(rand.nextInt(gunDrops.size())));
            }
            return null;
        }

        /**
         * Vráti drop perku na danej pozícii.
         * @return
         */
        public DropPerk dropPerk() {
            if (rand.nextDouble() < perkChance) {
                return new DropPerk(centerX(), centerY(), 0, perkDrops.get(rand.nextInt(perkDrops.size())));
            }
            return null;
        }

    }

    class EnemyZombie extends Enemy {
        /**
         * Podtrieda prvého nepriateľa.
         * @param corner
         */

        public EnemyZombie(int corner) {
            super(corner);
            this.image = new Image("imgs/enemies/enemyZombie.gif");
            this.imageIdle = images.get("zombieIdle");
            this.health = 50;
            this.fullHealth = health;
            this.damage = 0.4;
            this.speed = (double) width / (600-rand.nextInt(250));
            this.dropGun = Map.of(new Gun1(), 0.15, new Gun2(), 0.1);
            this.dropPerk = Map.of(new PerkFastReload(), 0.1, new PerkBonusDamage(), 0.9, new PerkInvincibility(), 0.8);
            this.gunDrops = List.of(new Gun1(), new Gun2());
            this.perkDrops = List.of(new PerkBonusDamage());
            this.gunChance = 0.125;
            this.perkChance = 0.1;
        }

    }

    class EnemyGun extends Enemy {
        /**
         * Podtrieda druhého nepriateľa.
         * @param corner
         */

        public EnemyGun(int corner) {
            super(corner);
            this.image = new Image("imgs/enemies/enemyGun.gif");
            this.imageIdle = images.get("gunIdle");
            this.health = 75;
            this.fullHealth = health;
            this.damage = 0.6;
            this.speed = (double) width / (500-rand.nextInt(200));
            this.dropGun = Map.of(new Gun2(), 0.2, new Gun3(), 0.1);
            this.dropPerk = Map.of(new PerkFastReload(), 0.1);
            this.gunDrops = List.of(new Gun2(), new Gun3());
            this.perkDrops = List.of(new PerkFastReload(), new PerkBonusDamage());
            this.gunChance = 0.1;
            this.perkChance = 0.075;
            this.spawnedTick = tick;
        }

    }

    class EnemySpider extends Enemy {
        /**
         * Podtrieda tretieho nepriateľa.
         * @param corner
         */

        public EnemySpider(int corner) {
            super(corner);
            this.image = new Image("imgs/enemies/enemySpider.gif");
            this.imageIdle = images.get("spiderIdle");
            this.health = 100;
            this.fullHealth = health;
            this.damage = 0.9;
            this.speed = (double) width / (400-rand.nextInt(200));
            this.dropGun = Map.of(new Gun3(), 0.15);
            this.dropPerk = Map.of(new PerkFastReload(), 0.15);
            this.gunDrops = List.of(new Gun3());
            this.perkDrops = List.of(new PerkInvincibility());
            this.gunChance = 0.15;
            this.perkChance = 0.1;
        }

    }

    class Drop {
        /**
         * Trieda dropu (zbraň alebo perk).
         * @param corner
         */

        protected final double x, y;
        protected double angle;
        protected Image img;
        protected Gun gun;
        protected Perk perk;
        protected int tickDespawn;
        private final int size = 60;

        public Drop(double x, double y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.tickDespawn = tick + rand.nextInt(framerate*2) + framerate*4;
        }

        /**
         * Zvyšuje uhol pre rotáciu obrázku na zemi.
         */
        public void rotate() {
            angle += 2;
        }

        /**
         * Pomer výšky / šírky obrázku.
         * @return
         */
        public double whRatio() {
            return img.getHeight() / img.getWidth();
        }

        private double width() {
            return size;
        }

        private double height() {
            return size * whRatio();
        }

    }

    class DropGun extends Drop {
        /**
         * Podtrieda dropu zbrane.
         * @param x
         * @param y
         * @param angle
         * @param gun
         */

        public DropGun(double x, double y, double angle, Gun gun) {
            super(x, y, angle);
            this.gun = gun;
            this.img = gun.img;
        }

    }

    class DropPerk extends Drop {
        /**
         * Podtrieda dropu perku.
         * @param x
         * @param y
         * @param angle
         * @param perk
         */

        public DropPerk(double x, double y, double angle, Perk perk) {
            super(x, y, angle);
            this.perk = perk;
            this.img = perk.img;
        }

    }

    abstract static class Perk {
        /**
         * Trieda perku.
         */

        protected int startTick;
        protected double duration;
        protected Image img;

        /**
         * Vráti, či perk vypršal.
         * @return
         */
        public boolean expired() {
            return tick == startTick + duration*framerate;
        }

        /**
         * Aktivuje perk, zapamätá si kedy bol aktivovaný.
         * @param startTick
         * @param gun
         */
        public abstract void start(int startTick, Gun gun);

        /**
         * Po vypršaní perku vráti originálne vlastnosti.
         */
        public abstract void reset();

    }

    class PerkFastReload extends Perk {
        /**
         * Podtrieda perku pre rýchle nabíjanie.
         */

        private Gun gun;

        public PerkFastReload() {
            this.duration = 4+rand.nextInt(2);
            this.img = images.get("perkReload");
        }

        @Override
        public void start(int startTick, Gun gun) {
            this.gun = gun;
            gun.reloadTime = gun.reloadTimeOrig/2;
            this.startTick = startTick;
        }

        public void reset() {
            if (gun != null) gun.reloadTime = gun.reloadTimeOrig;
        }

    }

    class PerkBonusDamage extends Perk {
        /**
         * Podtrieda perku pre zvýšený damage.
         */

        private Gun gun;

        public PerkBonusDamage() {
            this.duration = 4+rand.nextInt(2);
            this.img = images.get("perkDmg");
        }

        public void start(int startTick, Gun gun) {
            this.gun = gun;
            gun.dmg = gun.dmgOrig*2;
            this.startTick = startTick;
        }

        public void reset() {
            if (gun != null) gun.dmg = gun.dmgOrig;
        }

    }

    class PerkInvincibility extends Perk {
        /**
         * Podtrieda perku pre nesmrteľnosť.
         */

        public PerkInvincibility() {
            this.duration = 3+rand.nextInt(1);
            this.img = images.get("perkInv");
        }

        public void start(int startTick, Gun gun) {
            this.startTick = startTick;
        }

        public void reset() { }

    }

    static class Projectile {
        /**
         * Trieda pre vystrelené projektily zo zbraní.
         */

        private double dx, dy;
        private double x, y;
        private final double speed;
        private final double angle;
        private boolean enemy;
        Image image;

        public Projectile(double x, double y, double angle, double speed, Image image, double angleDegrees, boolean enemy) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.dx = Math.cos(angle);
            this.dy = Math.sin(angle);
            this.angle = angleDegrees;
            this.image = image;
            this.enemy = enemy;
            if (enemy) {
                dx /= 2.5;
                dy /= 2.5;
            }
        }

        /**
         * Pohyb projektilu.
         */
        public void move() {
            x += dx*speed;
            y += dy*speed;
        }
    }

    class Level {
        /**
         * Trieda pre údaje o leveloch.
         */

        protected int round;
        protected int totalEnemies;
        protected int currEnemies;
        protected double respawnInterval;
        protected int spawnedEnemies;
        protected List<Integer> enemyTypes = new ArrayList<>();

        public Level(int round) {
            this.round = round;
            loadValues();
        }

        /**
         * Načíta údaje o leveli z textového súboru.
         */
        public void loadValues() {
            // totalEnemies curr Enemies respawnInterval
            // enemyTypeNum1 enemyTypeNum3 enemyTypeNum2
            // ...
            try {
                File myObj = new File("src\\levels.txt");
                Scanner myReader = new Scanner(myObj);
                for (int i = 0; i < (round-1)*2; i++) {
                    myReader.nextLine();
                }
                String line = myReader.nextLine();
                List<String> vals = Arrays.asList(line.split(" "));
                totalEnemies = rand.nextInt(Integer.parseInt(vals.get(0))) + Integer.parseInt(vals.get(1));
                currEnemies = totalEnemies;
                respawnInterval = Double.parseDouble(vals.get(2));
                line = myReader.nextLine();
                for (String x : line.split(" ")) {
                    enemyTypes.add(Integer.parseInt(x));
                }
                spawnedEnemies = 0;
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        /**
         * Načíta údaje pre nasledujúci level.
         */
        public void advanceLevel() {
            round++;
            loadValues();
        }

        /**
         * Odčíta počet nepriateľov.
         */
        public void killEnemy() {
            currEnemies--;
        }

        /**
         * Pripočíta počet spawnutých nepriateľov.
         */
        public void spawnEnemy() {
            spawnedEnemies++;
        }

    }

}