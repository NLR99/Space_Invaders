package Game;

import End_of_game.end_of_game;
import Objet.objet;
import Objet.objet_1J;
import Objet.objet_2J;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

/**
 * Gère les parties de jeu
 */
public class game{
    //utilisé pour connaître la position et le sens de déplacement du/des groupe(s) d'aliens
    public static int deplacement;
    public static int deplacement2;
    public static int pos_gr_alien;
    public static int pos_gr_alien2;

    //utilisé pour mémoriser l'espacement entre les tirs des joueurs
    public static int t;
    public static int t2;

    //variable d'état : pause ou jeu
    public static Boolean pause = false;

    //utilisés pour retenir le temps passé en pause (pause actuelle et somme des pauses)
    public static long tempause = 0;
    public static long tpa = 0;

    //adresses relatives du fond et de la musique
    private static final String fond_url = "file:src\\main\\resources\\Image_fond\\Image_fond_1.jpg";
    private static final String MusiqueUrl = "src\\main\\resources\\Musique\\Musique_1.mp3";

    static MediaPlayer player;
    //retient la direction des joueurs
    public static int dir_p1;
    public static int dir_p2;

    /**
     * Crée la musique
     * @param URL adresse relative de la musique à jouer
     */
    public static void music(String URL) {
        Media sound = new Media(Paths.get(URL).toUri().toString());
        player = new MediaPlayer(sound);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.getOnRepeat();
        player.play();
    }

    /**
     * Gère les parties à 1 Joueur
     * @param stage Stage
     * @param difficulté niveau de difficulté
     * @param URL_vaisseau adresse relative de l'image du vaisseau du joueur
     * @param URL_alien adresse relative de l'image des aliens
     * @param URL_tir_vaisseau adresse relative de l'image des tirs du vaisseau
     * @param URL_tir_alien adresse relative de l'image des tirs des aliens
     */
    public static void game_1_joueur(Stage stage,int difficulté,
                                     String URL_vaisseau, String URL_alien,
                                     String URL_tir_vaisseau, String URL_tir_alien) {

        //Initialisation du jeu

        double screen_width = 1200;
        double screen_height = 700;
        long temps_debut = System.currentTimeMillis();
        BorderPane root = new BorderPane(); //investigate Group root
        Scene scene = new Scene(root, screen_width, screen_height, Color.BLACK);


        //initialisation des variables
        pos_gr_alien=0;
        deplacement=1;
        tempause = 0;
        tpa = 0;
        dir_p1=0;
        t=0;

        //tente de mettre la musique de fond

        try {
            music(MusiqueUrl);
        }
        catch (Exception e) {
            System.out.println("Impossible de lancer la musique");
            //On choisit de ne pas utiliser de musique
        }


        //tente de mettre le fond
        try {
            //erreur capturé par JavaFX -> détection manuelle
            Image image_fond = new Image(fond_url);
            if (image_fond.isError()) throw new FileNotFoundException();
            scene.setFill(new ImagePattern(image_fond, 0, 0, 1, 1, true));
        }
        catch ( Exception e) {
            System.out.println("Impossible d'afficher le fond");
            //On n'affiche pas de fond
        }

        //initialisation des différents Objets
        Group tirs_joueurs = new Group();
        Group tirs_aliens = new Group();
        Group aliens = new Group();
        Group blocks = new Group();
        Group vie_blocks = new Group();

        objet_1J.init_aliens(aliens,URL_alien);
        objet_1J.init_blocks(blocks, vie_blocks);
        objet Player1 = objet_1J.init_Player(3,URL_vaisseau);

        //vie du joueur, affiché sur lui même
        Text vie_joueur = new Text(Player1.getAccessibleText());
        vie_joueur.setFill(Color.WHITE);

        //chrono
        Text temps = new Text(Float.toString((System.currentTimeMillis() - temps_debut) / 1000F));
        temps.setFont(Font.font("Verdana", 20));
        temps.setFill(Color.WHITE);
        temps.setX(20);
        temps.setY(680);

        //texte de pause
        Text text_pause = new Text("PAUSE");
        text_pause.setFont((Font.font("Verdana", 80)));
        text_pause.setFill(Color.WHITE);
        text_pause.setX(480);
        text_pause.setY(350);

        //texte de niveau
        Text niveau = new Text("Niveau " +Integer.toString(difficulté));
        niveau.setFont(Font.font("Verdana", 20));
        niveau.setFill(Color.WHITE);
        niveau.setX(1080);
        niveau.setY(680);

        // action dans le cas d'une touche pressée
        EventHandler<KeyEvent> keyListenerPressed = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                //changer la direction du joueur
                if ((e.getCode() == KeyCode.LEFT)) dir_p1 = -1;
                else if (e.getCode() == KeyCode.RIGHT) dir_p1=1;
                    //activer/désactiver l'écran de pause
                else if (e.getCode() == KeyCode.SPACE) {
                    if (pause == true) {
                        pause = false;
                        tempause = tempause + (System.currentTimeMillis() - tpa);
                        root.getChildren().remove(text_pause);
                        root.getChildren().addAll(aliens, Player1, tirs_joueurs, tirs_aliens, blocks, vie_joueur, vie_blocks, temps, niveau);
                    } else if (pause == false) {
                        pause = true;
                        tpa = System.currentTimeMillis();
                        root.getChildren().clear();
                        root.getChildren().add(text_pause);
                    }
                }
            }
        };
        // action dans le cas d'une touche lâchée
        EventHandler<KeyEvent> keyListenerReleased = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                // mettre à jour le sens de déplacement du joueur
                if ((e.getCode() == KeyCode.LEFT)) dir_p1 = 0;
                else if (e.getCode() == KeyCode.RIGHT) dir_p1=0;
            }
        };

        //action à réaliser périodiquement dans le jeu
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (!pause) {

                    //déplacement du joueur
                    objet_1J.dep_1_joueur(Player1, dir_p1, difficulté);

                    //pattern de déplacement des aliens
                    //pos_gr_alien : position sur l'écran, pour savoir quand faire demi-tour
                    //deplacement : sens de déplacement des aliens
                    int ret[];
                    ret = objet_1J.depalien(aliens, pos_gr_alien, deplacement, "DOWN",difficulté);
                    pos_gr_alien = ret[0];
                    deplacement = ret[1];

                    //tir du joueur tous les max(30,100-5*difficulté) mouvements
                    t = objet_1J.tir_joueur(Math.max(30,100-5*difficulté), t,Player1, tirs_joueurs, URL_tir_vaisseau);


                    //tir des aliens
                    objet_1J.tir_alien(aliens, tirs_aliens, URL_tir_alien,Math.max(10,50-5*difficulté));

                    //déplacement des tirs
                    objet_1J.Tir(tirs_joueurs, "UP",difficulté);
                    objet_1J.Tir(tirs_aliens, "DOWN",difficulté);

                    //enlever les tirs en dehors
                    tirs_joueurs.getChildren().removeIf(elem -> elem.getLayoutY() < 0);
                    tirs_aliens.getChildren().removeIf(elem -> elem.getLayoutY() > 900);


                    //gestion des collisions
                    objet_1J.Collision(aliens, tirs_joueurs, -50, 10, -15, 5);
                    objet_1J.Collision(tirs_aliens, tirs_joueurs, -30, 10, -10, 0);
                    objet_1J.Collision(tirs_aliens, blocks, -10, 80, -10, 10);
                    objet_1J.Collision(tirs_joueurs, blocks, -10, 80, -10, 10);
                    objet_1J.Collision_joueur(Player1, tirs_aliens, -20, 20, -20, 20);
                    //retirer si plus de vie
                    objet_1J.supp(aliens);
                    objet_1J.supp(tirs_joueurs);
                    objet_1J.supp(tirs_aliens);
                    objet_1J.supp(blocks);

                    //affichage des vies du joueur
                    vie_joueur.setX(Player1.getLayoutX());
                    vie_joueur.setY(Player1.getLayoutY());
                    vie_joueur.setText(Player1.getAccessibleText());

                    //MAJ de la vie des blocks
                    objet_1J.vie_blocks(blocks, vie_blocks);

                    //MAJ du chrono
                    temps.setText(Float.toString((System.currentTimeMillis() - temps_debut - tempause) / 1000F));

                    //s'il n'y a plus d'aliens -> Victoire
                    if (aliens.getChildren().isEmpty()) {   // GAGNE
                        //if (true) {
                        end_of_game.endOfGame_1_joueur(stage, 0,
                                (System.currentTimeMillis() - temps_debut-tempause) / 1000F,
                                0,
                                player,
                                difficulté,
                                URL_vaisseau,
                                URL_alien,
                                URL_tir_vaisseau,
                                URL_tir_alien);
                        stop();
                    }
                    else if (Integer.valueOf(Player1.getAccessibleText())<=0) {  // PERDU : le joueur est mort.
                        end_of_game.endOfGame_1_joueur(stage, 1,
                                (System.currentTimeMillis() - temps_debut-tempause) / 1000F,
                                aliens.getChildren().size(),
                                player,
                                difficulté,
                                URL_vaisseau,
                                URL_alien,
                                URL_tir_vaisseau,
                                URL_tir_alien);
                        stop();
                    }
                    else if (objet.test_fin_alien(aliens,500, "DOWN")) {    // PERDU : les aliens ont atteint la Terre.
                        end_of_game.endOfGame_1_joueur(stage, 3,
                                (System.currentTimeMillis() - temps_debut-tempause) / 1000F,
                                aliens.getChildren().size(),
                                player,
                                difficulté,
                                URL_vaisseau,
                                URL_alien,
                                URL_tir_vaisseau,
                                URL_tir_alien);
                        stop();
                    }
                }

            }
        };

        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyListenerPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, keyListenerReleased);
        root.getChildren().addAll(aliens, Player1, tirs_joueurs, tirs_aliens, blocks, vie_joueur, vie_blocks, temps, niveau);
        loop.start();
        stage.setTitle("Space Invaders");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show(); // everything happens everywhere at once
    }

    //-------------------------------------------------------------------------------------------------------------------------//

    /**
     * Gère les parties à 2 joueurs
     * @param stage Stage
     * @param difficulté Niveau de difficulté
     * @param URL_vaisseau1 adresse relative de l'image du vaisseau du joueur 1
     * @param URL_vaisseau2 adresse relative de l'image du vaisseau du joueur 2
     * @param URL_alien adresse relative de l'image des aliens
     * @param URL_alien_r adresse relative de l'image des aliens retournés
     * @param URL_tir_alien_up adresse relative de l'image des tirs des aliens vers le haut
     * @param URL_tir_alien_down adresse relative de l'image des tirs des aliens vers le bas
     * @param URL_tir1 adresse relative de l'image des tirs du joueur 1
     * @param URL_tir2 adresse relative de l'image des tirs du joueur 2
     */
    public static void game_2_joueurs(Stage stage, int difficulté, String URL_vaisseau1, String URL_vaisseau2,
                                      String URL_alien, String URL_alien_r, String URL_tir_alien_up,
                                      String URL_tir_alien_down, String URL_tir1, String URL_tir2) {

        //Initialisation du jeu

        double screen_width = 1200;
        double screen_height = 700;
        long temps_debut = System.currentTimeMillis();
        BorderPane root = new BorderPane(); //investigate Group root
        Scene scene = new Scene(root, screen_width, screen_height, Color.BLACK);

        //Initialisation des variables
        pos_gr_alien=0;
        pos_gr_alien2=0;
        deplacement=1;
        deplacement2=1;
        tempause = 0;
        tpa = 0;
        dir_p1=0;
        dir_p2=0;
        t=0;
        t2=0;

        //tente de mettre la musique de fond
        try {
            music(MusiqueUrl);
        }
        catch (Exception e) {
            System.out.println("Impossible de lancer la musique");
            //On choisit de ne pas utiliser de musique
        }


        //tente de mettre le fond
        try {
            //erreur capturée par JavaFX -> détection manuelle
            Image image_fond = new Image(fond_url);
            if (image_fond.isError()) throw new FileNotFoundException();
            scene.setFill(new ImagePattern(image_fond, 0, 0, 1, 1, true));
        }
        catch ( Exception e) {
            System.out.println("Impossible d'afficher le fond");
            //On n'affiche pas de fond
        }

        Group tirs_joueurs_1 = new Group();
        Group tirs_joueurs_2 = new Group();
        Group tirs_aliens_1 = new Group();
        Group tirs_aliens_2 = new Group();
        Group aliens_1 = new Group();
        Group aliens_2 = new Group();
        Group blocks = new Group();
        Group vie_blocks = new Group();


        objet_2J.init_aliens(aliens_1, "DOWN",URL_alien);
        objet_2J.init_aliens(aliens_2, "UP",URL_alien_r);
        objet_2J.init_blocks(blocks, vie_blocks);
        objet Player1 = objet_2J.init_Player("UP",3,URL_vaisseau2);
        objet Player2 = objet_2J.init_Player("DOWN",3,URL_vaisseau1);


        //représente la vie des joueurs
        Text vie_joueur_1 = new Text(Player1.getAccessibleText());
        vie_joueur_1.setFill(Color.WHITE);
        Text vie_joueur_2 = new Text(Player2.getAccessibleText());
        vie_joueur_2.setFill(Color.WHITE);

        //Chrono
        Text temps = new Text(Float.toString((System.currentTimeMillis() - temps_debut) / 1000F));
        temps.setFont(Font.font("Verdana", 20));
        temps.setFill(Color.WHITE);
        temps.setX(20);
        temps.setY(680);

        //Ecran de pause
        Text text_pause = new Text("PAUSE");
        text_pause.setFont((Font.font("Verdana", 80)));
        text_pause.setFill(Color.WHITE);
        text_pause.setX(480);
        text_pause.setY(350);

        //Niveau
        Text niveau = new Text("Niveau " +Integer.toString(difficulté));
        niveau.setFont(Font.font("Verdana", 20));
        niveau.setFill(Color.WHITE);
        niveau.setX(1080);
        niveau.setY(680);

        //Si une touche est pressée
        EventHandler<KeyEvent> keyListener = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                //Mettre à jour le déplacement des joueurs
                if ((e.getCode() == KeyCode.LEFT)) dir_p1 = -1;
                else if (e.getCode() == KeyCode.RIGHT) dir_p1=1;
                else if (e.getCode() == KeyCode.Q) dir_p2=-1;
                else if (e.getCode() == KeyCode.D) dir_p2=1;
                    //Activer / désactiver l'écran Pause
                else if (e.getCode() == KeyCode.SPACE) {
                    if (pause) {
                        pause = false;
                        tempause = tempause + (System.currentTimeMillis() - tpa);
                        root.getChildren().remove(text_pause);
                        root.getChildren().addAll(Player1, Player2, tirs_joueurs_1, tirs_joueurs_2, tirs_aliens_1, tirs_aliens_2, aliens_1, aliens_2, blocks, vie_blocks,temps,vie_joueur_1,vie_joueur_2, niveau);
                    } else if (!pause) {
                        pause = true;
                        tpa = System.currentTimeMillis();
                        root.getChildren().clear();
                        root.getChildren().add(text_pause);
                    }
                }
            }
        };
        //Si une touche est relâchée
        EventHandler<KeyEvent> keyListener2 = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                //Mise à jour du sens de déplacement
                if ((e.getCode() == KeyCode.LEFT)) dir_p1 = 0;
                else if (e.getCode() == KeyCode.RIGHT) dir_p1=0;
                else if (e.getCode() == KeyCode.Q) dir_p2=0;
                else if (e.getCode() == KeyCode.D) dir_p2=0;
            }
        };

        //Action à réaliser périodiquement
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (!pause) {
                    //pattern de déplacement des aliens
                    //pos_gr_alien : position sur l'écran, pour savoir quand faire demi-tour
                    //déplacement : sens de déplacement des aliens
                    int ret[];
                    int ret2[];
                    if (!aliens_1.getChildren().isEmpty()) {
                        ret = objet_2J.depalien(aliens_1, pos_gr_alien, deplacement, "DOWN", difficulté);
                        pos_gr_alien = ret[0];
                        deplacement = ret[1];
                    }
                    if (!aliens_2.getChildren().isEmpty()) {
                        ret2 = objet_2J.depalien(aliens_2, pos_gr_alien2, deplacement2, "UP", difficulté);
                        pos_gr_alien2 = ret2[0];
                        deplacement2 = ret2[1];
                    }

                    //tir du joueur tous les max(30,100-5*difficulté) mouvements
                    t = objet_2J.tir_joueur(Math.max(20,60-5*difficulté), t, Player1, tirs_joueurs_1, URL_tir2);
                    t2 = objet_2J.tir_joueur(Math.max(20,60-5*difficulté), t, Player2, tirs_joueurs_2, URL_tir1);


                    //tir des aliens
                    if (!aliens_1.getChildren().isEmpty()) {
                        objet_2J.tir_alien(aliens_1, tirs_aliens_1, URL_tir_alien_down, Math.max(10,50-5*difficulté));
                    }
                    if (!aliens_2.getChildren().isEmpty()) {
                        objet_2J.tir_alien(aliens_2, tirs_aliens_2, URL_tir_alien_up, Math.max(10,50-5*difficulté));
                    }

                    //déplacement des tirs
                    objet_2J.Tir(tirs_joueurs_1, "DOWN", difficulté);
                    objet_2J.Tir(tirs_joueurs_2, "UP", difficulté);
                    objet_2J.Tir(tirs_aliens_1, "DOWN", difficulté);
                    objet_2J.Tir(tirs_aliens_2, "UP", difficulté);


                    //enlever les tirs en dehors
                    tirs_joueurs_1.getChildren().removeIf(elem -> elem.getLayoutY() > 900);
                    tirs_joueurs_2.getChildren().removeIf(elem -> elem.getLayoutY() < 0);
                    tirs_aliens_1.getChildren().removeIf(elem -> elem.getLayoutY() > 900);
                    tirs_aliens_2.getChildren().removeIf(elem -> elem.getLayoutY() < 0);

                    //Déplacer les joueurs
                    objet_2J.dep_2_joueurs(Player1, Player2, dir_p1, dir_p2,difficulté);

                    //gestion des collisions
                    objet_2J.Collision(aliens_1, tirs_joueurs_2, -50, 10, -15, 5);
                    objet_2J.Collision(aliens_1, tirs_joueurs_1, -30, 30, 0, 20);
                    objet_2J.Collision(aliens_2, tirs_joueurs_2, -30, 30, 0, 20);
                    objet_2J.Collision(aliens_2, tirs_joueurs_1, -30, 30, 0, 20);
                    objet_2J.Collision(tirs_aliens_2, tirs_joueurs_1, -30, 30, -10, 10);
                    objet_2J.Collision(tirs_aliens_1, tirs_joueurs_2, -30, 30, -10, 10);
                    objet_2J.Collision(tirs_aliens_1, blocks, -10, 80, -10, 10);
                    objet_2J.Collision(tirs_aliens_2, blocks, -10, 80, -10, 10);
                    objet_2J.Collision(tirs_joueurs_1, blocks, -10, 80, -10, 10);
                    objet_2J.Collision(tirs_joueurs_2, blocks, -10, 80, -10, 10);
                    objet_2J.Collision(tirs_joueurs_1, tirs_joueurs_2, -20, 20, -20, 20);
                    objet_2J.Collision_joueur(Player2, tirs_aliens_1, -20, 20, -20, 20);
                    objet_2J.Collision_joueur(Player1, tirs_aliens_2, -20, 20, -20, 20);
                    objet_2J.Collision_joueur(Player1, tirs_joueurs_2, -20, 20, -20, 20);
                    objet_2J.Collision_joueur(Player2, tirs_joueurs_1, -20, 20, -20, 20);

                    //retirer si plus de vie
                    if (!aliens_1.getChildren().isEmpty()) {
                        objet_2J.supp(aliens_1);
                    }
                    if (!aliens_2.getChildren().isEmpty()) {
                        objet_2J.supp(aliens_2);
                    }
                    objet_2J.supp(tirs_joueurs_1);
                    objet_2J.supp(tirs_joueurs_2);
                    objet_2J.supp(tirs_aliens_1);
                    objet_2J.supp(tirs_aliens_2);
                    objet_2J.supp(blocks);


                    //affichage des vies du joueur et des blocks
                    vie_joueur_1.setX(Player1.getLayoutX());
                    vie_joueur_1.setY(Player1.getLayoutY());
                    vie_joueur_1.setText(Player1.getAccessibleText());
                    vie_joueur_2.setX(Player2.getLayoutX());
                    vie_joueur_2.setY(Player2.getLayoutY());
                    vie_joueur_2.setText(Player2.getAccessibleText());
                    objet_2J.vie_blocks(blocks, vie_blocks);

                    // Affichage du chrono
                    temps.setText(Float.toString((System.currentTimeMillis() - temps_debut - tempause) / 1000F));

                    // Conditions de victoire
                    if (aliens_1.getChildren().isEmpty() && aliens_2.getChildren().isEmpty()) {   // GAGNE : Il n'y a plus d'aliens
                        end_of_game.endOfGame_2_joueurs(stage, 0,
                                (System.currentTimeMillis() - temps_debut - tempause) / 1000F,
                                0,
                                player,
                                difficulté,
                                URL_vaisseau1,
                                URL_vaisseau2,
                                URL_alien,
                                URL_alien_r,
                                URL_tir1,
                                URL_tir2,
                                URL_tir_alien_up,
                                URL_tir_alien_down);
                        stop();
                    } else if (Integer.valueOf(Player1.getAccessibleText()) <= 0) {  // PERDU : le joueur 1 est mort.
                        end_of_game.endOfGame_2_joueurs(stage, 1,
                                (System.currentTimeMillis() - temps_debut - tempause) / 1000F,
                                aliens_1.getChildren().size() + aliens_2.getChildren().size(),
                                player,
                                difficulté,
                                URL_vaisseau1,
                                URL_vaisseau2,
                                URL_alien,
                                URL_alien_r,
                                URL_tir1,
                                URL_tir2,
                                URL_tir_alien_up,
                                URL_tir_alien_down);
                        stop();
                    } else if (Integer.valueOf(Player2.getAccessibleText()) <= 0) {  // PERDU : le joueur 2 est mort.
                        end_of_game.endOfGame_2_joueurs(stage, 2,
                                (System.currentTimeMillis() - temps_debut - tempause) / 1000F,
                                aliens_1.getChildren().size() + aliens_2.getChildren().size(),
                                player,
                                difficulté,
                                URL_vaisseau1,
                                URL_vaisseau2,
                                URL_alien,
                                URL_alien_r,
                                URL_tir1,
                                URL_tir2,
                                URL_tir_alien_up,
                                URL_tir_alien_down);
                        stop();
                    }
                    if (!aliens_1.getChildren().isEmpty()) {
                        if (objet.test_fin_alien(aliens_1, 500, "DOWN")) {    // PERDU : les aliens du J1 ont atteint la Terre.
                            end_of_game.endOfGame_2_joueurs(stage, 3,
                                    (System.currentTimeMillis() - temps_debut - tempause) / 1000F,
                                    aliens_1.getChildren().size()+aliens_2.getChildren().size(),
                                    player,
                                    difficulté,
                                    URL_vaisseau1,
                                    URL_vaisseau2,
                                    URL_alien,
                                    URL_alien_r,
                                    URL_tir1,
                                    URL_tir2,
                                    URL_tir_alien_up,
                                    URL_tir_alien_down);
                            stop();
                        }
                    }
                    if (!aliens_2.getChildren().isEmpty()) {
                        if (objet.test_fin_alien(aliens_2, 180, "UP")) {    // PERDU : les aliens du J2 ont atteint la Terre.
                            end_of_game.endOfGame_2_joueurs(stage, 4,
                                    (System.currentTimeMillis() - temps_debut - tempause) / 1000F,
                                    aliens_1.getChildren().size()+aliens_2.getChildren().size(),
                                    player,
                                    difficulté,
                                    URL_vaisseau1,
                                    URL_vaisseau2,
                                    URL_alien,
                                    URL_alien_r,
                                    URL_tir1,
                                    URL_tir2,
                                    URL_tir_alien_up,
                                    URL_tir_alien_down);
                            stop();
                        }
                    }
                }
            }
        };
        scene.addEventHandler(KeyEvent.KEY_PRESSED, keyListener);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, keyListener2);
        root.getChildren().addAll(Player1, Player2, tirs_joueurs_1, tirs_joueurs_2, tirs_aliens_1, tirs_aliens_2, aliens_1, aliens_2, blocks, vie_blocks,temps,vie_joueur_1,vie_joueur_2, niveau);
        loop.start();
        stage.setTitle("Space Invaders");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show(); // everything happens everywhere at once
    }
}