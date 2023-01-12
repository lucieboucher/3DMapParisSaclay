import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class projet extends PApplet {

// Déclaration des variables globales
WorkSpace workspace;
Camera camera;
Hud hud;
Map3D map;
Land land;
Gpx gpx;
Railways railways;
Roads roads;
Buildings buildings;
Poi poi;
boolean toggleShader;
PShader myShader;

/** settings
 * met en place les paramètres du sketch
 */
public void settings(){
    // Désactive une propriété du système pour que ça marche
    System.setProperty("jogl.disable.openglcore", "true");
    // Antianialasing
    smooth(16);
    // Créationn de la fenêtre
    fullScreen(P3D);
}

/** setup
 * initialise les variables
 */
public void setup() {
    // Met en place le Head Up Display
    this.hud = new Hud();
    // 3D camera (X+ right / Z+ top / Y+ Front)
    this.camera = new Camera(PI/3,PI/3,width * 2.0f);
    // Prépare le systèmme de coordonées locales, la grid et le gizmo
    this.workspace = new WorkSpace(250);
    // Vitesse d'affichage souhaitée
    frameRate(60);
    // Background initial
    background(0x40);
    // Facilite les mouvements de caméra
    hint(ENABLE_KEY_REPEAT);
    // Charge la Height Map
    this.map = new Map3D("paris_saclay.data");
    // Charge les point of interest
    this.poi = new Poi(this.map, "heat.geojson");
    // Crée land
    this.land = new Land(this.map, "paris_saclay.jpg", this.poi);
    // Crée gpx
    this.gpx = new Gpx(this.map, "trail.geojson");
    // Crée railways
    this.railways = new Railways(this.map, "railways.geojson");
    // Crée roads
    this.roads = new Roads(this.map, "roads.geojson");
    // Crée buildings
    this.buildings = new Buildings(this.map);
    // Ajoute buildings
    this.buildings.add("buildings_city.geojson", 0xFFaaaaaa);
    this.buildings.add("buildings_IPP.geojson", 0xFFCB9837);
    this.buildings.add("buildings_EDF_Danone.geojson", 0xFF3030FF);
    this.buildings.add("buildings_CEA_algorithmes.geojson", 0xFF30FF30);
    this.buildings.add("buildings_Thales.geojson", 0xFFFF3030);
    this.buildings.add("buildings_Paris_Saclay.geojson", 0xFFee00dd);
    // set shaders off
    this.toggleShader = false;
    // Charge les shaders
    myShader = loadShader("texture_fragment.glsl", "texture_vertex.glsl");
}

/** draw
 * Boucle principale
 */
public void draw(){
    // On remet à zéro l'arrière plan
    background(0x40);
    // Tant que tout n'a pas chargé, on affiche un écran de chargement
    if (frameCount == 1) {
        textSize(12);
        textAlign(CENTER, CENTER);
        text("Loading " + String.valueOf(round(millis()/100.0f)/10.0f), width/2, height/2);
    } else {
        // On met à jour les différents éléments successivement
        this.workspace.update();
        this.camera.update();
        // On fait bien attention à mettre à jour les shaders juste pour le terrain
        myShader.set("on_off", this.toggleShader);
        this.land.update(myShader);
        resetShader();
        this.railways.update();
        this.roads.update();
        this.buildings.update();
        this.gpx.update();
        //Il faut mettre le hud en dernier pour être sur qu'il soit affiché au bon endroit
        this.hud.update();
    }
}

/** keyPressed
 * Récupère les actions au clavier de l'utilisateur pour gérer les contrôles du sketch
 */
public void keyPressed(){
    if (key == CODED) {
        switch (keyCode) {
            // Oriente vers le dessus
            case UP:
                this.camera.adjustColatitude(-PI/100);
                break;
            // Oriente vers le dessous
            case DOWN:
                this.camera.adjustColatitude(PI/100);
                break;
            // Tourne vers la gauche
            case LEFT:
                this.camera.adjustLongitude(-PI/100);
                break;
            // Tourne vers la droite
            case RIGHT:
                this.camera.adjustLongitude(PI/100);
                break;
        }
    } else {
        switch (key) {
            // Zoom
            case '+':
            case 'p':
                this.camera.adjustRadius(-10);
                break;
            // Dézoom
            case '-':
            case 'm':
                this.camera.adjustRadius(10);
                break;
            // Affiche grid & Gizmo
            case 'w':
            case 'W':
                this.workspace.toggle();
                break;
            // Affiche Land
            case 't':
            case 'T':
                this.land.toggle();
                break;
            //On/off lights
            case 'l':
            case 'L':
                this.camera.toggle();
                break;
            // Affiche track
            case 'x':
            case 'X':
                this.gpx.toggle();
                break;
            // Affiche railways et roads
            case 'r':
            case 'R':
                this.railways.toggle();
                this.roads.toggle();
                break;
            // Affiche help
            case 'h':
            case 'H':
                this.hud.toggleHelp();
                break;
            // Affiche buildings
            case 'b':
            case 'B':
                this.buildings.toggle();
                break;
            // Déplace vers l'avant
            case 'z':
            case 'Z':
                this.camera.moveZS(10);
                break;
            // Déplace vers l'arrière
            case 's':
            case 'S':
                this.camera.moveZS(-10);
                break;
            // Déplace vers la gauche
            case 'q':
            case 'Q':
                this.camera.moveQD(10);
                break;
            // Déplace vers la droite
            case 'd':
            case 'D':
                this.camera.moveQD(-10);
                break;
            // Affiche shader
            case 'o':
            case 'O':
                this.toggleShader = !this.toggleShader;
                break;
        }
    }
}

/** mousePressed
 * Récupère de clic à la souris de l'utilisateur pour gérer les contrôles du sketch
 */
public void mousePressed(){
    // Affiche les description des épingles
    if (mouseButton == LEFT){
        this.gpx.clic(mouseX, mouseY);
    }
}

/** mouseWheel
 * Récupère les actions de molette de la souris clavier de l'utilisateur pour gérer les contrôles du sketch
 */
public void mouseWheel(MouseEvent event) {
   float ec = event.getCount();
   // Zoom
   this.camera.adjustRadius(20*ec);
}

/** mouseDragged
 * Récupère les actions de cliquer-glisser à la souris au clavier de l'utilisateur pour gérer les contrôles du sketch
 */
public void mouseDragged() {
    // Tourne la caméra
   if (mouseButton == CENTER) {
       // Camera Horizontal
       float dx = mouseX - pmouseX;
       this.camera.adjustLongitude((dx/10)*(-PI)/100);

       // Camera Vertical
       float dy = mouseY - pmouseY;
       this.camera.adjustColatitude((dy/10)*PI/100);
   }
   // Déplace l'origine
   if (mouseButton == LEFT){
       // Déplace droite gauche
       this.camera.moveQD(mouseX - pmouseX);

       // Déplace avant arrière
       this.camera.moveZS(mouseY - pmouseY);
   }
}

/** radToDegree
 * Fonction de conversion de radian vers degrés
 */
public float radToDegree(float angle){
    return angle*180/PI;
}
class Buildings{
    //initialisation des arguments de la classe
    Map3D map;
    PShape buildings;

    /**Constructeur de Buildings
     * @param  map       Map3D
     **/
    Buildings(Map3D map){
        //Association du paramêtre map à l'argument de la classe
        this.map = map;
        //Création de la shape buildings: on utilise une shape de type GROUP
        this.buildings = createShape(GROUP);
    }

    /**add
     * @param  fileName       string
     * @param  c              color
     **/
    public void add(String fileName, int c){

        int levels;
        float top0, top1;

        // Vérifie que le fichier existe bien
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: GeoJSON file " + fileName + " not found.");
            return;
        }
        // Charge le geoJSON et récupère la collection qu'il contient
        JSONObject geojson = loadJSONObject(fileName);
        if (!geojson.hasKey("type")) {
            println("WARNING: Invalid GeoJSON file.");
            return;
        } else if (!"FeatureCollection".equals(geojson.getString("type", "undefined"))) {
            println("WARNING: GeoJSON file doesn't contain features collection.");
            return;
        }

          // Sépare les différents éléments du JSON
        JSONArray features =  geojson.getJSONArray("features");
        if (features == null) {
            println("WARNING: GeoJSON file doesn't contain any feature.");
            return;
        }

        for (int f=0; f<features.size(); f++) {
            JSONObject feature = features.getJSONObject(f);
            if (!feature.hasKey("geometry"))
                break;
            levels = feature.getJSONObject("properties").getInt("building:levels", 1);
            JSONObject geometry = feature.getJSONObject("geometry");
            switch (geometry.getString("type", "undefined")) {
                case "Polygon":
                    // roads
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    if (coordinates != null){
                        for (int p = 0; p < coordinates.size(); p++){
                            JSONArray polygon;
                            polygon = coordinates.getJSONArray(p);
                            // Création de la forme pour les toits
                            PShape roof;
                            roof = createShape();
                            roof.beginShape();
                            roof.emissive(0x60);
                            roof.fill(c);
                            roof.stroke(c);
                            // Création de la forme pour les murs
                            PShape walls;
                            walls = createShape();
                            walls.beginShape(QUAD);
                            walls.emissive(0x30);
                            walls.fill(c);
                            walls.stroke(c);
                            PVector vOrtho0, vOrtho1, vOrtho2;
                            Map3D.ObjectPoint r0, r1;
                            Map3D.GeoPoint g;
                            JSONArray point;
                            point = polygon.getJSONArray(0);
                            g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                            if (g.elevation > 0){
                                //on recupere l'élévation du premier point pour la hauteur du toit
                                r0 =  this.map.new ObjectPoint(g);
                                top0 = r0.z;
                                top0 += Map3D.heightScale * 3.0f * (float)levels;
                                for (int i = 0; i < polygon.size(); i++) {
                                    point = polygon.getJSONArray((i + 1) % polygon.size());
                                    g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                                    if(g.elevation > 0){
                                        r1 =  this.map.new ObjectPoint(g);
                                        top1 = r1.z + Map3D.heightScale * 3.0f * (float)levels;

                                        vOrtho0 = new PVector(r1.x-r0.x, r1.y-r0.y, r1.z-r0.z);
                                        vOrtho1 = new PVector(0,0,top0-r0.z);
                                        //ajout des vertex et des normal pour les toits
                                        vOrtho2 = vOrtho0.cross(vOrtho1);
                                        roof.normal(0.0f, 0.0f, 1.0f);
                                        roof.vertex(r0.x, r0.y, top0);
                                        roof.normal(0.0f, 0.0f, 1.0f);
                                        roof.vertex(r1.x, r1.y, top1);

                                        //ajout des vertex et des normal pour les murs
                                        walls.normal(vOrtho2.x, vOrtho2.y, vOrtho2.z);
                                        walls.vertex(r0.x, r0.y, r0.z);
                                        walls.normal(vOrtho2.x, vOrtho2.y, vOrtho2.z);
                                        walls.vertex(r0.x, r0.y, top0);
                                        walls.normal(vOrtho2.x, vOrtho2.y, vOrtho2.z);
                                        walls.vertex(r1.x, r1.y, top1);
                                        walls.normal(vOrtho2.x, vOrtho2.y, vOrtho2.z);
                                        walls.vertex(r1.x, r1.y, r1.z);
                                        r0 = r1;
                                        top0 = top1;
                                    }
                                }
                                //On ajout les PShape walls et roof à la PShape building de type GROUP
                                this.buildings.addChild(walls);
                                this.buildings.addChild(roof);
                                walls.endShape();
                                roof.endShape(CLOSE);
                            }
                        }
                    }
                    break;
                default:
                    println("WARNING: GeoJSON '" + geometry.getString("type", "undefined") + "' geometry type not handled.");
                    break;
            }
        }
    }

    /** MAJ de Buildings
      */
    public void update(){
        shape(this.buildings);
    }
    /** Toggle Buildings visibility.
      */
    public void toggle(){
        this.buildings.setVisible(!this.buildings.isVisible());
    }
}
class Camera {
    float colatitude;
    float longitude;
    float radius;
    float x;
    float y;
    float z;
    float rootX;
    float rootY;
    boolean lightning;
    /** Constructeur de la Camera
      */
    Camera(float colatitude, float longitude, float radius){
        this.colatitude = colatitude;
        this.longitude = longitude;
        this.radius = radius;
        this.x = radius*sin(colatitude)*cos(longitude);
        this.y = radius*sin(colatitude)*sin(longitude);
        this.z = radius*cos(colatitude);
        this.rootX = 0;
        this.rootY = 0;
        this.lightning = true;
    }

    /** update
     * met à jour la position de la caméra et de l'origine
     */
    public void update(){
        // Déplace le repère en fonction des coordonénes de l'origine
        translate(this.rootX,this.rootY,0);
        // Positionne la caméra, 3D camera (X+ right / Z+ top / Y+ Front)
        camera(this.x, -this.y, this.z,
                0, 0, 0,
                0, 0, -1);
        // Replace le repère en fonction des coordonénes de l'origine
        translate(-this.rootX,-this.rootY);
        // Sunny vertical lightning
        ambientLight(0x7F, 0x7F, 0x7F);
        if (lightning){
            // Change la lumière pour une lumière qui éclaire vers le bas depuis  0, 0
            directionalLight(0xA0, 0xA0, 0x60, 0, 0, -1);
        }
        // Définie l'évanouissement de la lumière
        lightFalloff(0.0f, 0.0f, 1.0f);
        lightSpecular(0.0f, 0.0f, 0.0f);

    }

    /** toggle
     * active ou désactive la lumière
     */
    public void toggle(){
        this.lightning = !this.lightning;
    }

    /** adjustRadius
     * Gère le zoom de la caméra
     */
    public void adjustRadius(float offset){
        // Défini une limite au rayon minimal et maximal
        if (radius+offset < width * 3.0f && radius+offset > width * 0.5f){
            this.radius += offset;
        }
        // Recalcule la position de la caméra en fonction du nouveau rayon
        this.x = radius*sin(colatitude)*cos(longitude);
        this.y = radius*sin(colatitude)*sin(longitude);
        this.z = radius*cos(colatitude);
    }
    /** adjustLongitude
     * Gère la longitude de la caméra (orientation horizontale)
     */
    public void adjustLongitude(float delta){
        // La longitude n'est pas limitée, on souhaite pouvoir tourner autant qu'on veut autour de la carte
        this.longitude += delta;
        // Recalcule la position de la caméra en fonction de la nouvelle longitude
        this.x = radius*sin(colatitude)*cos(longitude);
        this.y = radius*sin(colatitude)*sin(longitude);
        this.z = radius*cos(colatitude);
    }

    /** adjustColatitude
     * Gère le colatitude de la caméra (orientation verticale)
     */
    public void adjustColatitude(float delta){
        // Défini une limite à la colatitude minimale et maximale, pour ne pas s'enfoncer sous le sol ou dépasser la verticale
        if (colatitude+delta < HALF_PI-delta && colatitude+delta > -HALF_PI-delta && colatitude+delta > 0-delta){
            this.colatitude += delta;
        }
        // Recalcule la position de la caméra en fonction de la nouvelle colatitude
        this.x = radius*sin(colatitude)*cos(longitude);
        this.y = radius*sin(colatitude)*sin(longitude);
        this.z = radius*cos(colatitude);
    }

    /** moveZS
     * Gère le déplacement de l'origine dans la direction nord-sud
     */
    public void moveZS(float r){
        // Calcule la vaeur des décalges en x et y
        float tmpx = -cos(this.longitude)*r;
        float tmpy = sin(this.longitude)*r;
        // Si on ne sort pas des bornes de la carte, on modifie l'origine
        if (this.rootX+tmpx < 2500 && this.rootX+tmpx > - 2500){
            this.rootX += tmpx;
        }
        if (this.rootY+tmpy < 1500 && this.rootY+tmpy > - 1500){
            this.rootY += tmpy;
        }
    }

    /** moveQD
     * Gère le déplacement de l'origine dans la direction est-ouest
     */
    public void moveQD(float r){
        // Calcule la vaeur des décalges en x et y
        float tmpx = sin(this.longitude)*r;
        float tmpy = cos(this.longitude)*r;
        // Si on ne sort pas des bornes de la carte, on modifie l'origine
        if (this.rootX+tmpx < 2500 && this.rootX+tmpx > - 2500){
            this.rootX += tmpx;
        }
        if (this.rootY+tmpy < 1500 && this.rootY+tmpy > - 1500){
            this.rootY += tmpy;
        }
    }

}
class Gpx{
    JSONArray features;
    JSONArray trail;
    Map3D map;
    PShape track;
    PShape thumbtacks;
    PShape posts;
    int currentPostNb;

    /** Constructeur du tracé GPX
     * @param Map3D     map
     * @param String    fileName
     */
    Gpx(Map3D map, String fileName){
        this.map = map;
        // initialise la légende des épingles à -1
        this.currentPostNb = -1;
        // Vérifie que le fichier existe bien
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: GeoJSON file " + fileName + " not found.");
            return;
        }
        // Charge le geoJSON et récupère la collection qu'il contient
        JSONObject geojson = loadJSONObject(fileName);
        if (!geojson.hasKey("type")) {
            println("WARNING: Invalid GeoJSON file.");
            return;
        } else if (!"FeatureCollection".equals(geojson.getString("type", "undefined"))) {
            println("WARNING: GeoJSON file doesn't contain features collection.");
            return;
        }

         // Sépare les différents éléments du JSON
        JSONArray features =  geojson.getJSONArray("features");
        this.features = features;
        if (features == null) {
            println("WARNING: GeoJSON file doesn't contain any feature.");
            return;
        }
        // Création de la forme pour les poteaux
        this.posts = createShape();
        this.posts.beginShape(LINES);
        this.posts.noFill();
        this.posts.stroke(0xffFF0000);
        this.posts.strokeWeight(2.f);
        // Créeation de la forme pour les têtes d'épingle
        this.thumbtacks = createShape();
        this.thumbtacks.beginShape(POINTS);
        this.thumbtacks.noFill();
        this.thumbtacks.stroke(0xffFF0000);
        this.thumbtacks.strokeWeight(8.f);
        for (int f=0; f<features.size(); f++) {
            JSONObject feature = features.getJSONObject(f);
            if (!feature.hasKey("geometry"))
                break;
            JSONObject geometry = feature.getJSONObject("geometry");
            switch (geometry.getString("type", "undefined")) {
                // Si c'est une série de points, on crée un nouveau chemin
                case "LineString":
                    // GPX Track
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    if (coordinates != null){
                        // Création du nouveau chemin
                        this.track = createShape();
                        this.track.beginShape();
                        this.track.noFill();
                        this.track.stroke(0xffFF0000);
                        this.track.strokeWeight(1.5f);
                        Map3D.ObjectPoint v;
                        Map3D.GeoPoint g;
                        JSONArray point;

                        // Pour chaque point dans les coordonnées
                        for (int p=0; p < coordinates.size(); p++) {
                            point = coordinates.getJSONArray(p);
                            // On le converti en GeoPoint
                            g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                            //  On le place un peu au dessus de la grille
                            g.elevation += 8;
                            // Puis en ObjectPoint
                            v = this.map.new ObjectPoint(g);
                            // Et on l'ajoute au chemin
                            this.track.vertex(v.x, v.y, v.z);
                        }
                        this.track.endShape();
                    }
                    break;
                // Si c'est un point, on l'ajoute en tant qu'épingle
                case "Point":
                    // GPX WayPoint
                    if (geometry.hasKey("coordinates")) {
                        JSONArray point = geometry.getJSONArray("coordinates");
                        String description = "Pas d'information.";
                        if (feature.hasKey("properties")) {
                            description = feature.getJSONObject("properties").getString("desc", description);
                        }
                        //println("WayPoint", point.getDouble(0), point.getDouble(1), description);
                        Map3D.ObjectPoint v0;
                        Map3D.GeoPoint g0;
                        // On fait le même processus de conversion
                        g0 = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                        g0.elevation += 7.9f;
                        v0 = this.map.new ObjectPoint(g0);
                        this.posts.vertex(v0.x, v0.y, v0.z);
                        this.posts.vertex(v0.x, v0.y, v0.z + 25.f);
                        this.thumbtacks.vertex(v0.x, v0.y, v0.z + 25.f);
                    }
                    break;
                // Sinon on sort de la boucle
                default:
                    println("WARNING: GeoJSON '" + geometry.getString("type", "undefined") + "' geometry type not handled.");
                    break;
            }
        }
        this.posts.endShape();
        this.thumbtacks.endShape();
    }

    /** update
     * met à jour l'affichage du chemin
     */
    public void update(){
        shape(this.track);
        shape(this.posts);
        shape(this.thumbtacks);
        // Si une épingle est sélectionnée, on affiche sa légende
        if (this.currentPostNb >= 0)
            this.displayDescription(currentPostNb, camera);
    }

    /** toggle
     * Active et désactive le tracé du chemin
     */
    public void toggle(){
        this.track.setVisible(!this.track.isVisible());
        this.posts.setVisible(!this.posts.isVisible());
        this.thumbtacks.setVisible(!this.thumbtacks.isVisible());
    }

    /** clic
     * si la souris est assez proche d'une épingle quand on clic, sélectionne cette épingle
     * pour afficher sa légende
     * @param int       mouseX position de la souris en x
     * @param int       mouseY position de la souris en y
     */
    public void clic(int mouseX, int mouseY){
        PVector currentPost;
        boolean wasFound = false;
        // On parcourt l'ensemble des épingles
        for (int i = 0;  i < this.thumbtacks.getVertexCount(); i++){
            currentPost = this.thumbtacks.getVertex(i);
            // Si la distance entre la souris et la position à l'écran de l'épingle est inférieure à 20
            if (dist(mouseX, mouseY, screenX(currentPost.x, currentPost.y, currentPost.z), screenY(currentPost.x, currentPost.y, currentPost.z)) < 20){
                // On indique avoir trouvé une épingle
                wasFound = true;
                // On donne sa position à l'objet pour l'affichage de la légende
                this.currentPostNb = i;
                // On réinitialise la couleur des épingles
                for (int j = 0;  j < this.thumbtacks.getVertexCount(); j++){
                    this.thumbtacks.setStroke(j,0xFFFF3F3F);
                }
                // On change la couleur de l'épingle choisie
                this.thumbtacks.setStroke(i, 0xFF3FFF7F);
                break;
            }
        }
        // Si on n'en a pas trouvé, on déselectionne toutes les épingles
        if (!wasFound){
            for (int j = 0;  j < this.thumbtacks.getVertexCount(); j++){
                this.thumbtacks.setStroke(j,0xFFFF3F3F);
            }
            this.currentPostNb = -1;
        }
    }

    /** displayDescription
     * Affiche la description d'une épingle donnée
     * @param int           nb numéro de la légende à afficher
     * @param Camera        camera Caméra pour orienter la légende
     */
    public void displayDescription(int nb, Camera camera){
        pushMatrix();
            noLights();
            fill(0xFFFFFFFF);
            // On récupère l'épingle correspondant au numéro donné
            PVector currentPost = this.thumbtacks.getVertex(nb);
            // On se déplace à sa position et on rotate en fonction de la caméra
            translate(currentPost.x, currentPost.y, currentPost.z + 10.0f);
            rotateZ(-camera.longitude-HALF_PI);
            rotateX(-camera.colatitude);
            hint(PConstants.DISABLE_DEPTH_TEST);
            textMode(SHAPE);
            textSize(48);
            textAlign(LEFT, CENTER);
            String description = "";
            // On récupère la description correspondante
            description = this.features.getJSONObject(nb+1).getJSONObject("properties").getString("desc", description);
            // et on l'affiche
            text(description, 25, 0);
            hint(PConstants.ENABLE_DEPTH_TEST);
        // on rétabli le repère original
        popMatrix();
    }
}
class Hud {
    private PMatrix3D hud;
    private boolean helper;

    /** Constructeur du Hud
     */
    Hud() {
        // Should be constructed just after P3D size() or fullScreen()
        this.hud = g.getMatrix((PMatrix3D) null);
        // booléen testé pour afficher l'aide aux contrôles
        this.helper = true;
    }

    /** begin
     * Modifie les paramètre du sketch pour l'affichage du Hud
      */
    private void begin() {
        g.noLights();
        g.pushMatrix();
        g.hint(PConstants.DISABLE_DEPTH_TEST);
        g.resetMatrix();
        g.applyMatrix(this.hud);
    }

    /** end
     * Rétabli les paramètre du sketch après l'affichage du Hud
     */
    private void end() {
        g.hint(PConstants.ENABLE_DEPTH_TEST);
        g.popMatrix();
    }

    /** displayFPS
     * Affiche le nombre de fps dans le coin inférieur gauche
     */
    private void displayFPS() {
        // Bottom left area
        noStroke();
        fill(96);
        rectMode(CORNER);
        // On place le cadre en 10, height-30 avec une largeur de 60 et une hauteur de 20
        rect(10, height-30, 60, 20, 5, 5, 5, 5);
        // Value
        fill(0xF0);
        textMode(SHAPE);
        textSize(14);
        // On alligne le texte au milieu horizontalement et verticalement
        textAlign(CENTER, CENTER);
        // On écrit le nombre de fps au bon endroit
        text(String.valueOf((int)frameRate) + " fps", 40, height-20);
    }

    /** displayHelp
     * Affiche les différents raccourcis clavier
     * qui permettent de choisir d'afficher certains éléments en haut à gauche
     * et ceux qui permettent de controller la caméra en bas à gauche
     */
    private void displayHelp() {
        // Top right area
        noStroke();
        fill(96);
        rectMode(CORNER);
        // On place le cadre en width-220, 10 avec une largeur de 210 et une hauteur de 190
        rect(width-220, 10, 210, 190, 5, 5, 5, 5);
        // Value
        fill(0xF0);
        textMode(SHAPE);
        textSize(14);
        // On alligne le texte à gauche horizontalement et au milieu verticalement
        textAlign(LEFT, CENTER);
        // On place nos différents raccourcis claviers dans la box, avec le titre de la boîte un peu décalé
        text("Help :", width-205, 20);
        text("h : display help", width-210, 40);
        text("w : Hide/Show grid & Gizmo" , width-210, 60);
        text("t : Hide/show Land", width-210, 80);
        text("l : lights switch", width-210, 100);
        text("x : Hide/show track", width-210, 120);
        text("r : Hide/show railways", width-210, 140);
        text("b : Hide/show buildings", width-210, 160);
        text("o : Hide/show shader", width-210, 180);


        // Bottom right area
        noStroke();
        fill(96);
        rectMode(CORNER);
        // On place le cadre en width-260, height-160 avec une largeur de 250 et une hauteur de 150
        rect(width-260, height-160, 250, 150, 5, 5, 5, 5);
        // Value
        fill(0xF0);
        textMode(SHAPE);
        textSize(14);
        textAlign(LEFT, CENTER);
        // On place nos différents raccourcis claviers dans la box, avec le titre de la boîte un peu décalé
        text("Camera controls :", width-245, height-150);
        text("zqsd : move origin", width-250, height-130);
        text("haut/bas : ajuste la colatitude", width-250, height-110);
        text("droite/gauche : ajuste la longitude" , width-250, height-90);
        text("p/+ : zoom", width-250, height-70);
        text("m/- dézoom", width-250, height-50);
        text("click molette : ajuste position", width-250, height-30);

    }

    /** displayCamera
     * Affiche les informations relatives au positionnement de la caméra
     */
    private void displayCamera(Camera  camera){
        // Bottom left area
        noStroke();
        fill(96);
        rectMode(CORNER);
        // On place le cadre en 10, 10 avec une largeur de 150 et une hauteur de 150
        rect(10,10,150, 150, 5, 5, 5, 5);
        // Value
        fill(0xF0);
        textMode(SHAPE);
        textSize(14);
        textAlign(LEFT, CENTER);
        // On place les informations dans la box, avec le titre de la boîte un peu décalé
        text("Camera :", 25, 20);
        text("Longitude : "+String.valueOf((int)radToDegree(camera.longitude%(2*PI))+"°") , 20, 40);
        text("Colatitude : "+String.valueOf((int)radToDegree(camera.colatitude)+"°") , 20, 60);
        text("Radius : "+String.valueOf((int)camera.radius+"m") , 20, 80);
        text("Lightning : "+String.valueOf(camera.lightning) , 20, 100);
        text("X : "+String.valueOf(camera.rootX),20,120);
        text("Y : "+String.valueOf(camera.rootY),20,140);

    }

    /** update
     * Met à jour le hud
     */
    public void update(){
        // Ouvre l'environnement du hud
        this.begin();
        // Affiche les fps
        this.displayFPS();
        // Affiche les informations de caméra
        this.displayCamera(camera);
        // On affiche les raccourcis claviers si l'aide est activée
        if (this.helper)
            this.displayHelp();
        // Ferme l'environnement du hud
        this.end();
    }

    /** toggleHelp
     * Modifie le booléen d'affiche d'aide
     */
    public void toggleHelp(){
        this.helper = !this.helper;
    }
}
class Land{
    //initialisation des arguments de la classe
    Map3D map;
    PShape shadow;
    PShape wireFrame;
    PShape satellite;
    Poi interest;

     /**Constructeur de Land
      * Prepares les shapes land shadow, wireframe et satelite.
      * @param  map       Map3D
      * @param  filename  String Nom du fichier de la texture
      * @param  poi       POI Liste de points d'interet
      **/
    Land(Map3D map, String fileName, Poi poi){
        //Association des paramêtres aux arguments de la classe
        this.interest = poi;
        this.map = map;

        //Récupération de l'image et on le stock dans un PImage
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {                   //On verifie que le fichier existe
            println("ERROR: Land texture file " + fileName + " not found.");
            exitActual();
        }
        PImage uvmap = loadImage(fileName);

        final float tileSize = 12.5f;
        float w = (float)Map3D.width;
        float h = (float)Map3D.height;
        float lambda = uvmap.width/w;
        final float textureSize = tileSize*lambda;

        // Shadow Shape
        this.shadow = createShape();                //Creation d'une Shape Shadow
            this.shadow.beginShape(QUADS);          //On utilise une Shape de type QUADS
            this.shadow.fill(0x992F2F2F);
            this.shadow.noStroke();
            this.shadow.vertex(-w/2.f, -h/2.f, -10);  //On positionne les 4 vertex aux 4 extrémités
            this.shadow.vertex(w/2.f, -h/2.f, -10);
            this.shadow.vertex(w/2.f, h/2.f, -10);
            this.shadow.vertex(-w/2.f, h/2.f, -10);
        this.shadow.endShape();                     //Fin de la Shape

        //Satellite shape
        this.satellite = createShape();             //Creation d'une Shape satelite
            this.satellite.beginShape(QUADS);       //On utilise une Shape de type QUADS
            this.satellite.texture(uvmap);          //On applique la texture
            this.satellite.noFill();
            this.satellite.noStroke();
            this.satellite.emissive(0xD0);

        // Wireframe shape
        this.wireFrame = createShape();             //Creation d'une Shape wireframe
            this.wireFrame.beginShape(QUADS);       //On utilise une Shape de type QUADS
            this.wireFrame.noFill();
            this.wireFrame.stroke(0xff888888);
            this.wireFrame.strokeWeight(0.5f);

            //Points de tracé
            Map3D.ObjectPoint v0, v1, v2, v3;
            PVector n0, n1, n2, n3;

            //Information pour la chaleur
            float d_bench, d_pic_nic;
            //Coordonnées texture
            float u = 0, v;
            for (float x = -w/2.f; x < w/2.f; x += tileSize){
                //remet v en bas de l'image
                v = 0;
                for (float y = -h/2.f; y < h/2.f; y += tileSize){
                    v0 = this.map.new ObjectPoint(x,y);             // on créer un ObjectPoint
                    n0 = v0.toNormal();                             // l'ObjectPoint normalisé
                    v1 = this.map.new ObjectPoint(x+tileSize, y);
                    n1 = v1.toNormal();
                    v2 = this.map.new ObjectPoint(x+tileSize, y+tileSize);
                    n2 = v2.toNormal();
                    v3 = this.map.new ObjectPoint(x, y+tileSize);
                    n3 = v3.toNormal();

                    //point actuel
                    d_bench = this.dist_min(v0, this.interest.l_bench);
                    d_pic_nic = this.dist_min(v0, this.interest.l_pic_nic);
                    this.satellite.attrib("heat", d_bench, d_pic_nic);
                    //On utilise les ObjectPoint pour le wireframe
                    this.wireFrame.vertex(v0.x, v0.y, v0.z);
                    //le vecteur normal pour la lumière
                    this.satellite.normal(n0.x, n0.y, n0.z);
                    //on associe le point la texture (u,v), a v0
                    this.satellite.vertex(v0.x, v0.y, v0.z, u, v);
                    //point +x
                    d_bench = this.dist_min(v1, this.interest.l_bench);
                    d_pic_nic = this.dist_min(v1, this.interest.l_pic_nic);
                    this.satellite.attrib("heat", d_bench, d_pic_nic);

                    this.wireFrame.vertex(v1.x, v1.y, v1.z);
                    this.satellite.normal(n1.x, n1.y, n1.z);
                    this.satellite.vertex(v1.x, v1.y, v1.z, u+textureSize, v);
                    //point +x +y
                    d_bench = this.dist_min(v2, this.interest.l_bench);
                    d_pic_nic = this.dist_min(v2, this.interest.l_pic_nic);
                    this.satellite.attrib("heat", d_bench, d_pic_nic);

                    this.wireFrame.vertex(v2.x, v2.y, v2.z);
                    this.satellite.normal(n2.x, n2.y, n2.z);
                    this.satellite.vertex(v2.x, v2.y, v2.z, u+textureSize, v+textureSize);
                    //point +y
                    d_bench = this.dist_min(v3, this.interest.l_bench);
                    d_pic_nic = this.dist_min(v3, this.interest.l_pic_nic);
                    this.satellite.attrib("heat", d_bench, d_pic_nic);

                    this.wireFrame.vertex(v3.x, v3.y, v3.z);
                    this.satellite.normal(n3.x, n3.y, n3.z);
                    this.satellite.vertex(v3.x, v3.y, v3.z, u, v+textureSize);
                    //incrémente v
                    v += textureSize;
                }
                //incrémente u
                u += textureSize;
            }
        this.wireFrame.endShape();      //Fin de la Shape
        this.satellite.endShape();      //Fin de la Shape

        // Shapes initial visibility
        this.shadow.setVisible(true);
        this.wireFrame.setVisible(false);
        this.satellite.setVisible(true);
    }

    /**dist_min
     * Calcule la distance minimum entre un ObjectPoint v et les ObjectPoint d'une liste
     * @param  v         Map3D.ObjectPoint
     * @param  l         ArrayList<Map3D.ObjectPoint>
     * @return d         float
     **/
    public float dist_min(Map3D.ObjectPoint v, ArrayList<Map3D.ObjectPoint> l){
        float min = sqrt(pow(5000,2) + pow(3000,2));
        float d;
        Map3D.ObjectPoint coord;
        for (int i=0; i < l.size(); i++){
            coord = l.get(i);
            d = dist(v.x, v.y, v.z, coord.x, coord.y, coord.z);
            if (d<min)
                min = d;
        }
        return min;
    }

    /**MAJ du Land
      */
    public void update(PShader myShader){
        if (this.shadow.isVisible())
            shape(this.shadow);
        shader(myShader);
        if (this.wireFrame.isVisible())
            shape(this.wireFrame);
        
        if (this.satellite.isVisible())
            shape(this.satellite);
    }

    /** Toggle Land visibility.
      */
    public void toggle() {
        this.satellite.setVisible(!this.satellite.isVisible());
        this.wireFrame.setVisible(!this.wireFrame.isVisible());
    }

}
/** //<>// //<>//
 * Map3D Class
 * @version  1.0.0
 * @author   Yves BLAKE (2021/Jan/30)
 * Handles Elevation 3D Map computations from IGN RGE Alti open data.
 * @see https://geoservices.ign.fr/ressources_documentaires/Espace_documentaire/MODELES_3D/RGE_ALTI/DC_RGEALTI_2-0.pdf
 * Convert coordinates beetween WGS84 GPS geographic coordinates, Lambert93 map projection coordinates and Object space coordinates systems
 * @see http://pp.ige-grenoble.fr/pageperso/picardgh/enseignement/fichiers/Teledetection_M1/ResumeCours/intro-gis.pdf
 */

public class Map3D {

    /**
    * Elevation data cell size, in meters
    */
    final static double cellSize = 5.00d;
    /**
    * Number of cells columns
    */
    final static int columns = 1000;
    /**
    * Map width
    */
    final static double width = Map3D.columns * Map3D.cellSize;
    /**
    * Number of cells rows
    */
    final static int rows = 600;
    /**
    * Map height
    */
    final static double height = Map3D.rows *  Map3D.cellSize;
    /**
    * Lower left X origin
    */
    final static double xllCorner = 637500.00d;
    /**
    * Lower Left Y origin
    */
    final static double yllCorner = 6844000.00d;
    /**
    * Default value when no elevation data found
    */
    final static double noData = -327.68d;
    /**
    * Object height scale applied to real elevation data
    */
    final static float heightScale = 2.5f;
    /**
    * Elevation data
    */
    final byte data[];
    /**
    * Set 3D (true) or 2D (false) mode
    */
    final boolean mode3D;
    /**
    * South West WGS84 coordinates
    */
    Map3D.GeoPoint SouthWest;
    /**
    * North East WGS84 coordinates
    */
    Map3D.GeoPoint NorthEast;

    /**
    * Returns a Map3D object.
    *
    * @param  fileName  IGN Alti data file name
    */
    Map3D(String fileName) {
        // Check ressources
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: Map3D elevation file " + fileName + " not found.");
            exitActual();
    }

    // Load RGE Alti elevation heightmap
    this.data = loadBytes(fileName);
    // Force flat projection if false (for debug purposes)
    this.mode3D = true;

    // Computes geographic bounding box
    Map3D.MapPoint mp;
    mp = new Map3D.MapPoint(Map3D.xllCorner, Map3D.yllCorner);
    this.SouthWest = new Map3D.GeoPoint(mp);
    mp = new Map3D.MapPoint(Map3D.xllCorner + Map3D.width, Map3D.yllCorner + Map3D.height);
    this.NorthEast = new Map3D.GeoPoint(mp);
}

    /**
    * Retrieve elevation found at map projection coordinates.
    * @param  xm        X coordinate, Lambert 93 projection (meters)
    * @param  ym        Y coordinate, Lambert 93 projection (meters)
    * @return           Elevation (meters)
    */
    private double getElevation(double xm, double ym) {
        int column = (int) ((xm - Map3D.cellSize/2.0d - Map3D.xllCorner) / Map3D.cellSize);
        int row = (int) ((ym - Map3D.cellSize/2.0d - Map3D.yllCorner) / Map3D.cellSize);
        if (row < 0 || row >= Map3D.rows || column < 0 || column >= Map3D.columns)
            return Map3D.noData;
        else {
            if (this.mode3D) {
                // Default mode 3D heightmap (Big-endian)
                int index = 2 * ((row * Map3D.columns) + column);
                return (double)((Map3D.this.data[index] << 8) | (Map3D.this.data[index+1] & 0xFF))/100.0d;
            } else
                // 2D near flat simulation (debug)
                return 0.01d;
        }
    }

    /**
     * GeoPoint class.
     * WGS84 Geographic coordinates
     * The WGS84 coordinates system is used by GPS
     * French RGF93 coordinate system is equivalent
     */
class GeoPoint {

    /**
     * WGS84 longitude, in decimal degrees
     */
    double longitude;
    /**
     * WGS84 latitude, in decimal degrees
     */
    double latitude;
    /**
     * Elevation, in meters. Contains noData value when not found.
     */
    double elevation;

    /**
     * Create Geographic coordinates from WGS84 longitude and latitude
     * Elevation is retrieved from IGN data
     * @param  longitude   Longitude (DD decimal degrees)
     * @param  latitude    Latitude (DD decimal degrees)
     */
    GeoPoint(double longitude, double latitude) {
        this(longitude, latitude, 0.0d);
    }

    /**
     * Create Geographic coordinates object from WGS84 longitude, latitude and elevation
     * When not specified, elevation is retrieved from IGN data
     * @param  longitude  Longitude (DD decimal degrees)
     * @param  latitude   Latitude (DD decimal degrees)
     * @param  elevation  Elevation (meters)
     */
    GeoPoint(double longitude, double latitude, double elevation) {
        this.longitude = longitude;
        this.latitude = latitude;
        if (elevation > 0.0d)
            this.elevation = elevation;
        else {
            this.elevation = 0;
            MapPoint mp = new MapPoint(this);
            this.elevation = mp.em;
        }
    }

    /**
     * Create Geographic coordinates object from Map projection coordinates
     * When not specified, elevation is retrieved from IGN data
     * @param  mp        Map projection coordinates
     */
    GeoPoint(MapPoint mp) {
        double[] gps = Geodesie.wgs84(mp.xm, mp.ym);
        this.longitude = Math.round(gps[0] * 1e7d) / 1e7d;
        this.latitude = Math.round(gps[1] * 1e7d) / 1e7d;
        if (mp.em > 0.0d)
            this.elevation = mp.em;
        else
            mp = new MapPoint(mp.xm, mp.ym);
        this.elevation = mp.em;
    }

    /**
     * Create Geographic coordinates from Object space coordinates
     * When not specified, elevation is retrieved from IGN data
     * @param  op        Object space coordinates
     */
    GeoPoint(ObjectPoint op) {
        this(new MapPoint(op));
    }

    /**
     * Check if current point is inside valid Geographic coordinates area
     * @return           true if current point is inside Map
     */
    public boolean inside() {
        //return new MapPoint(this).inside();
        return
            this.longitude >= Map3D.this.SouthWest.longitude
            && this.longitude <= Map3D.this.NorthEast.longitude
            && this.latitude >= Map3D.this.SouthWest.latitude
            && this.latitude <= Map3D.this.NorthEast.latitude;
    }

    /**
     * String representation
     * @return           GeoPoint String representation
     */
    public String toString() {
        return "longitude = "
            + String.valueOf(Math.round(this.longitude * 1e7f)/1e7f)
            + ", latitude = "
            + String.valueOf(Math.round(this.latitude * 1e7f)/1e7f)
            + ", elevation = "
            + String.valueOf(Math.round(this.elevation * 1e7f)/1e7f);
    }
}

/**
* MapPoint class.
* Lambert93 projection coordinates,
* X/Y Origins are LowerLeft coordinates (xllCorner, yllCorner)
*/
class MapPoint {

    /**
     * Lambert93 X coordinate, in meters
     */
    double xm;
    /**
     * Lambert93 Y coordinate, in meters
     */
    double ym;
    /**
     * Elevation, in meters. Contains noData value when not found.
     */
    double em;

    /**
     * Create Map projection coordinates object from tiles column and row
     * Elevation is retrieved from IGN data
     * @param  column    0 based column index (0 to columns-1)
     * @param  row       0 based row index (0 to rows-1)
     */
    MapPoint(int column, int row) {
        this(
            Map3D.xllCorner + (double)column * Map3D.cellSize,
            Map3D.yllCorner + (double)row * Map3D.cellSize,
            0.0d
            );
    }

    /**
     * Create Map projection coordinates object from projection X, Y coordinates
     * Elevation is retrieved from IGN data
     * @param  xm        X coordinate, Lambert 93 projection (meters)
     * @param  ym        Y coordinate, Lambert 93 projection (meters)
     */
    MapPoint(double xm, double ym) {
        this(xm, ym, 0.0d);
    }

    /**
     * Create Map projection coordinates object from projection X, Y coordinates & height
     * When not specified, elevation is retrieved from IGN data
     * @param  xm        X coordinate, Lambert 93 projection (meters)
     * @param  ym        Y coordinate, Lambert 93 projection (meters)
     * @param  em        Elevation (meters)
     */
    MapPoint(double xm, double ym, double em) {
        this.xm = xm;
        this.ym = ym;
        if (em > 0.0d)
            this.em = em;
        else
            this.em = Map3D.this.getElevation(this.xm, this.ym);
    }

    /**
     * Create Map projection coordinates object from WGS84 Geographic coordinates
     * When not specified, elevation is retrieved from IGN data
     * @param  gp        Geographic coordinates object
     */
    MapPoint(GeoPoint gp) {
        double[] lambert93 = Geodesie.lambert93(gp.longitude, gp.latitude);
        this.xm = Math.round(lambert93[0] * 1e2d) / 1e2d;
        this.ym = Math.round(lambert93[1] * 1e2d) / 1e2d;
        if (gp.elevation > 0.0d)
            this.em = gp.elevation;
        else
            this.em = Map3D.this.getElevation(this.xm, this.ym);
    }

    /**
     * Create Map projection coordinates object from Object coordinates
     * When not specified, elevation is retrieved from IGN data
     * @param  op        Object coordinates object
     */
    MapPoint(ObjectPoint op) {
        this.xm = (double)op.x + Map3D.xllCorner + Map3D.width/2.0d;
        this.ym = Map3D.yllCorner - (double)op.y + Map3D.height/2.0d;
        if (op.z > 0.0f)
            this.em = Math.round(100.0d * (double)op.z / Map3D.heightScale) / 100.0d;
        else
            this.em = Map3D.this.getElevation(this.xm, this.ym);
    }

    /**
     * Check if current point is inside valid Map projection coordinates area
     * @return           true if current point is inside Map
     */
    public boolean inside() {
        return
            this.xm >= Map3D.xllCorner
            && this.xm < Map3D.xllCorner + Map3D.width
            && this.ym >= Map3D.yllCorner
            && this.ym < Map3D.yllCorner + Map3D.height;
    }

    /**
     * String representation
     * @return           MapPoint String representation
     */
    public String toString() {
        return "xm = "
            + String.valueOf(Math.round(this.xm * 1e7f)/1e7f)
            + ", ym = "
            + String.valueOf(Math.round(this.ym * 1e7f)/1e7f)
            + ", em = "
            + String.valueOf(Math.round(this.em * 1e7f)/1e7f);
    }
}

/**
* ObjectPoint class.
* Object space coordinates, centered at 0,0,0
* Visible viewport bounds :
* X from -map width/2 (left) to +map width/2 (right)
* Y from -map height/2 (front) to +map height/2 (rear)
* Z from 0 (bottom) to maximum visible (up)
*/
class ObjectPoint {

    /**
     * Object space X coordinate, in meters
     */
    float x;
    /**
     * Object space Y coordinate, in meters
     */
    float y;
    /**
     * Scaled elevation, in meters. Contains noData value when not found.
     */
    float z;

    /**
     * Create Object space coordinates object from x,y coordinates
     * elevation is retrieved and scaled from IGN data
     * @param  x         Object space x
     * @param  y         Object space y
     */
    ObjectPoint(float x, float y) {
        this(x, y, 0.0f);
    }

    /**
     * Create Object space coordinates object from x,y,z coordinates
     * When not specified, elevation is retrieved and scaled from IGN data
     * @param  x         Object space x
     * @param  y         Object space y
     * @param  z         Object space z
     */
    ObjectPoint(float x, float y, float z) {
        this.x = x;
        this.y = y;
        if (z > 0.0f)
            this.z = z;
        else {
            this.z = 0;
            MapPoint mp = new MapPoint(this);
            this.z = (float)(Map3D.heightScale * mp.em);
        }
    }

    /**
     * Create Object space coordinates object from Geographic coordinates
     * elevation is scaled from IGN data
     * @param  gp        Geographic coordinates object
     */
    ObjectPoint(GeoPoint gp) {
        this(new MapPoint(gp));
    }

    /**
     * Create Object space coordinates object from Map projection coordinates
     * elevation is scaled from IGN data
     * @param  mp        Map projection coordinates object
     */
    ObjectPoint(MapPoint mp) {
        this.x = (float)(mp.xm - Map3D.xllCorner - Map3D.width/2.0d);
        this.y = (float)(-mp.ym + Map3D.yllCorner + Map3D.height/2.0d);
        this.z = (float)(Map3D.heightScale * mp.em);
    }

    /**
     * Returns Object space coordinates object as a PVector
     * @return           Object space coordinates as PVector
     */
    public PVector toVector() {
        return new PVector(this.x, this.y, this.z);
    }

    /**
     * Returns Object space normalized coordinates object as a PVector
     * @return           Object space normalized coordinates as PVector
     */
    public PVector toNormal() {
        return new PVector(this.x, this.y, this.z).normalize();
    }

    /**
     * Check if current point is inside valid Object space area
     * @return           true if current point is inside Map
     */
    public boolean inside() {
        return new MapPoint(this).inside();
    }

    /**
     * String representation
     * @return           ObjectPoint String representation
     */
    public String toString() {
        return "x = "
            + String.valueOf(Math.round(this.x * 1e2f)/1e2f)
            + ", y = "
            + String.valueOf(Math.round(this.y * 1e2f)/1e2f)
            + ", z = "
            + String.valueOf(Math.round(this.z * 1e2f)/1e2f);
    }
}
}

/**
 * Geodesie Class
 * Calculs de conversion longitude, latitude WGS84 - X, Y Lambert93
 * @see https://geodesie.ign.fr/contenu/fichiers/documentation/algorithmes/notice/NTG_71.pdf
 */
static class Geodesie {

  /**
   * Projection Class
   */
  static class Projection {
    /**
     * Paramètres de l'ellipsoïde de référence RGF93 (IAG GRS 80)
     */
    // 1/2 grand axe de l'ellipsoïde (mètre)
    static double ra = 6378137.0d;
    // Facteur d'aplatissement
    static double rf = 1.0d / 298.257222101d;
    // 1/2 petit axe de l'ellipsoïde (mètre) - Non utilisé directement
    static double rb = Projection.ra * (1.0d - Projection.rf);
    // Première excentricité de l'ellipsoïde de référence
    static double e = Math.sqrt((Math.pow(Projection.ra, 2.0d) - Math.pow(Projection.rb, 2.0d)) / Math.pow(Projection.ra, 2.0d));
    // Hauteur au dessus de l'ellipsoïde (en mètre)
    //static double rh;
    /**
     * Paramètres de la projection Lambert93
     * False Easting : Xo = 700 000 m (3° Est Greenwich)
     * False Northing : Yo = 6 600 000 m (46°30' N)
     */
    // Exposant de la projection
    static double n = 0.725607765d;
    // Constante de la projection
    static double c = 11754255.426d;
    // Coordonnées en projection du pôle
    static double xs = 700000.0d, ys = 12655612.05d;
    // Latitude du méridien d'origine
    //static double phi0 = Math.toRadians(46.5d); // latitude origine 46° 30' 0.0" N
    // Longitude du méridien d'origine
    static double lambda0 = Math.toRadians(3.0d); // Méridien central
    // Longitude du 1er parallèle automécoïque
    //static double phi1 = Math.toRadians(44.0d); // Parrallèle 1 44° 0' 0.0" N
    // Longitude du 2ème parallèle automécoïque
    //static double phi2  = Math.toRadians(49.0d); // Parrallèle 2 49° 0' 0.0" N
  }

    /**
    * Convert WGS84 longitude, latitude (decimal degrees) to Lambert93 Map projection x,y (meters)
    * @param  longitude  WGS84 longitude (Decimal degrees)
    * @param  latitude   WGS84 latitude (Decimal degrees)
    * @return            Lambert93 coordinates array (meters)
    */
    public static double[] lambert93(double longitude, double latitude) {
        double lambda = Math.toRadians(longitude);
        double phi = Math.toRadians(latitude);
        return alg03(lambda, phi, Projection.n, Projection.c, Projection.e, Projection.lambda0, Projection.xs, Projection.ys);
    }

  /**
   * Convert Lambert93 Map projection x,y (meters) into WGS84 longitude, latitude (decimal degrees)
   * @param  xm :      X coordinate, Lambert 93 projection (meters)
   * @param  ym :      Y coordinate, Lambert 93 projection (meters)
   * @return           WGS84 coordinates array (Decimal degrees)
   */
  public static double[] wgs84(double xm, double ym) {
    double[] ll = alg04(xm, ym, Projection.n, Projection.c, Projection.e, Projection.lambda0, Projection.xs, Projection.ys);
    ll[0] = Math.toDegrees(ll[0]);
    ll[1] = Math.toDegrees(ll[1]);
    return ll;
  }

  /**
   * Calcul de la latitude isométrique sur un ellipsoïde de première excentricité e au point de latitude ϕ.
   * @param    ϕ :  latitude.
   * @param    e :  première excentricité de l’ellipsoïde.
   * @return   L :  latitude isométrique.
   */
  static private double alg01(double phi, double e) {
    return Math.log( Math.tan(Math.PI/4.0d + phi/2.0d) * Math.pow((1.0d - e * Math.sin(phi)) / (1.0d + e * Math.sin(phi)), e/2.0d) );
  }

  /**
   * Calcul de la latitude ϕ à partir de la latitude isométrique L
   * @param    L :  latitude isométrique.
   * @param    e :  première excentricité de L’ellipsoïde.
   * @return   ϕ :  latitude en radian.
   */
  static private double alg02(double li, double e ) {
    double epsilon = 1.0e-10f;
    double lic, lif;
    lic = (2.0d * Math.atan(Math.exp(li)) - Math.PI/2.0d);
    do {
      lif = lic;
      lic = 2.0d * Math.atan(Math.pow(((1.0d + e * Math.sin(lif)) / (1.0d - e * Math.sin(lif))), e / 2.0d) * Math.exp(li)) - Math.PI / 2.0d;
    } while (Math.abs(lic - lif) > epsilon);
    return lic;
  }

  /**
   * Transformation de coordonnées géographiques en projection conique conforme de Lambert
   * @param    λ : longitude par rapport au méridien origine.
   * @param    ϕ : latitude.
   * @param    n : exposant de la projection.
   * @param    c : constante de la projection.
   * @param    e : première excentricité de l’ellipsoïde.
   * @param    λ0 : longitude de l’origine par rapport au méridien origine.
   * @param    Xs, Ys : coordonnées en projection du pôle.
   * @return   X, Y : coordonnées en projection du point.
   */
  static private double[] alg03(double lambda, double phi, double n, double c, double e, double lambda0, double xs, double ys ) {
    double lat = alg01(phi, e);
    double x = xs + c * Math.exp(-n * lat) * Math.sin(n * (lambda - lambda0));
    double y = ys - c * Math.exp(-n * lat) * Math.cos(n * (lambda - lambda0));
    double[] xy={x, y};
    return xy;
  }

  /**
   * Passage d'une projection Lambert vers des coordonnées géographiques
   * @param    X, Y : coordonnées en projection conique conforme de Lambert du point.
   * @param    n : exposant de la projection.
   * @param    c : constante de la projection.
   * @param    e : première excentricité de l’ellipsoïde.
   * @param    λ0 : longitude de l’origine par rapport au méridien origine.
   * @param    Xs, Ys : coordonnées en projection du pôle.
   * @return   λ : longitude par rapport au méridien origine.
   * @return   ϕ : latitude.
   */
  static private double[] alg04(double x, double y, double n, double c, double e, double lambda0, double xs, double ys ) {
    double gamma = Math.atan((x - xs) / (ys - y));
    double longitude = lambda0 + gamma / n;
    double r = Math.sqrt(Math.pow(x - xs, 2.0d) + Math.pow(y - ys, 2.0d));
    double li =  (-1.0d / n) * Math.log(Math.abs(r / c));
    double latitude = alg02(li, e);
    double[] ll={longitude, latitude};
    return ll;
  }

  /**
   * Tests unitaires (algorithmes et interface)
   */
  //public void tests() {

  //  double[] xy, ll;

  //  // alg01 - Calcul de la latitude isométrique sur ellipsoide de 1ère excentricité e au point de latitude Phi
  //  System.out.println("Attendu -> 1.00552653648");
  //  System.out.println("Obtenu  -> " + truncate(alg01(0.872664626d, 0.08199188998d), 11));
  //  System.out.println("Attendu -> -0.3026169006");
  //  System.out.println("Obtenu  -> " + truncate(alg01(-0.29999999997d, 0.08199188998d), 11));
  //  System.out.println("Attendu -> 0.2");
  //  System.out.println("Obtenu  -> " + truncate(alg01(0.19998903369d, 0.08199188998d), 11));

  //  // alg02 - Calcul de la latitude à partir de la latitude isométrique
  //  System.out.println("Attendu -> 0.872664626");
  //  System.out.println("Obtenu  -> " + truncate(alg02(1.00552653648d, 0.08199188998d), 11));
  //  System.out.println("Attendu -> -0.29999999997");
  //  System.out.println("Obtenu  -> " + truncate(alg02(-0.3026169006d, 0.08199188998d), 11));
  //  System.out.println("Attendu -> 0.19998903369");
  //  System.out.println("Obtenu  -> " + truncate(alg02(0.2d, 0.08199188998d), 11));

  //  double e, n, c, lambda0, xs, ys;

  //  // alg03 - Transformation de coordonnées géographiques en projection conique conforme de Lambert
  //  e = 0.0824832568d;
  //  n = 0.760405966d;
  //  c = 11603796.9767d;
  //  lambda0 = 0.04079234433d;
  //  xs = 600000.0d;
  //  ys = 5657616.674d;
  //  xy = alg03(0.145512099d, 0.872664626d, n, c, e, lambda0, xs, ys);
  //  System.out.println("Attendu -> X : 1029705.0818 m, Y : 272723.851 m");
  //  System.out.println("Obtenu  -> X : " + truncate(xy[0], 4) + " m, Y : " + truncate(xy[1], 4)  + " m");

  //  // alg04 - Passage d'une projection Lambert vers des coordonnées géographiques
  //  e = 0.0824832568d;
  //  n = 0.760405966d;
  //  c = 11603796.9767d;
  //  lambda0 = 0.04079234433d;
  //  xs = 600000.0d;
  //  ys = 5657616.674d;
  //  ll = alg04(1029705.083d, 272723.849d, n, c, e, lambda0, xs, ys);
  //  System.out.println("Attendu -> Lon : 0.1455120993 , Lat : 0.8726646257");
  //  System.out.println("Obtenu  -> Lon : " + truncate(ll[0], 10) + " , Lat : " + truncate(ll[1], 10));

  //  // lambert93 : Convertit un geopoint longitude, latitude (degrés décimaux wgs84) en coordonnées projetées X,Y (mètres Lambert93)
  //  xy = lambert93(2.1504057d, 48.7201061d);
  //  System.out.println("Attendu -> X : 637500.0 m, Y : 6847000.0 m");
  //  System.out.println("Obtenu  -> X : " + truncate(xy[0], 2) + " m, Y : " + truncate(xy[1], 2)  + " m");

  //  // wgs84 - Convertit un point de coordonnées projetées X,Y (mètres Lambert93) en longitude, latitude (degrés décimaux WGS84)
  //  ll = wgs84(637500.0d, 6847000.0d);
  //  System.out.println("Attendu -> Lon : 2.1504057 , Lat : 48.7201061");
  //  System.out.println("Obtenu  -> Lon : " + truncate(ll[0], 7) + " , Lat : " + truncate(ll[1], 7));
  //}
  //
  //// Troncature de valeurs doubles à n décimales
  //private double truncate(double x, int decimals) {
  //  double s = Math.pow(10.0d, decimals);
  //  return ( Math.round(x*s)/s );
  //}
}

/**
 * Tests modulaires (changements de repères)
 */
//void test() {

//  MapPoint mp;
//  ObjectPoint op;
//  GeoPoint gp;
//  mp = new MapPoint(this.xllCorner, this.yllCorner);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  mp = new MapPoint((int)0, (int)0);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  op = new ObjectPoint(mp);
//  println("op: ", op.x, op.y, op.z);
//  mp = new MapPoint(op);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  gp = new GeoPoint(mp);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  gp = new GeoPoint(op);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  mp = new MapPoint(gp);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  gp = new GeoPoint(2.1508442d, 48.6931245d);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  op = new ObjectPoint(gp);
//  println("op: ", op.x, op.y, op.z);
//  op = new ObjectPoint(-2500.0f, +1500f);
//  println("op: ", op.x, op.y, op.z);
//  gp = new GeoPoint(op);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  println("-------------------------------------");
//  op = new ObjectPoint(+2500.0f, -1500f);
//  println("op: ", op.x, op.y, op.z);
//  gp = new GeoPoint(op);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  println("-------------------------------------");
//  mp = new MapPoint(this.xllCorner+this.width-this.cellSize, this.yllCorner+height-this.cellSize);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  mp = new MapPoint(this.columns-1, this.rows-1);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  op = new ObjectPoint(mp);
//  println("op: ", op.x, op.y, op.z);
//  mp = new MapPoint(op);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  gp = new GeoPoint(mp);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  op = new ObjectPoint(gp);
//  println("op: ", op.x, op.y, op.z);
//  gp = new GeoPoint(op);
//  println("gp: ", gp.longitude, gp.latitude, gp.elevation);
//  mp = new MapPoint(gp);
//  println("mp: ", mp.xm, mp.ym, mp.em);
//  println("-------------------------------------");
//}
class Poi{
    Map3D map;
    ArrayList<Map3D.ObjectPoint> l_bench;
    ArrayList<Map3D.ObjectPoint> l_pic_nic;

    /** Constructeur de Poi
     * initialisation des variables
     * @param Map3D     map
     * @param String    fileName
     */
    Poi(Map3D map, String fileName){
        // On stocker la map
        this.map = map;
        // On initialise deux listes d'object points pour stocker les infos sur les poi
        this.l_bench = new ArrayList<Map3D.ObjectPoint>();
        this.l_pic_nic = new ArrayList<Map3D.ObjectPoint>();
        // On appelle la fonction qui récupère les coordonnées
        this.getPoint(fileName);
    }

    /** getPoint
     * Récupère les coordonnées des poi et les stocke dans les tableaux adaptés
     * @param String fileName  le nom du fichier contenant les poi
     */
    public void getPoint(String fileName){
        // Vérifie que le fichier existe bien
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: GeoJSON file " + fileName + " not found.");
            return;
        }
        // Charge le geoJSON et récupère la collection qu'il contient
        JSONObject geojson = loadJSONObject(fileName);
        if (!geojson.hasKey("type")) {
            println("WARNING: Invalid GeoJSON file.");
            return;
        } else if (!"FeatureCollection".equals(geojson.getString("type", "undefined"))) {
            println("WARNING: GeoJSON file doesn't contain features collection.");
            return;
        }
        // Sépare les différents éléments du JSON
        JSONArray features =  geojson.getJSONArray("features");
        if (features == null) {
            println("WARNING: GeoJSON file doesn't contain any feature.");
            return;
        }
        // On déclare nos points pour la conversion
        Map3D.GeoPoint geoi;
        Map3D.ObjectPoint poi;
        // Pour chaque poi dans le geoJSON
        for (int f=0; f<features.size(); f++) {
            JSONObject feature = features.getJSONObject(f);
            if (!feature.hasKey("properties") || !feature.hasKey("geometry")){
                break;
            }

            // On récupère ses propriétés et sa géométrie
            JSONObject properties = feature.getJSONObject("properties");
            JSONObject geometry = feature.getJSONObject("geometry");
            // On récupère ses coordonnées
            JSONArray coordinates = geometry.getJSONArray("coordinates");

            if (coordinates != null){
                // On convertit les coordonnées dans l'espace de travail
                geoi = this.map.new GeoPoint(coordinates.getDouble(0), coordinates.getDouble(1));
                poi = this.map.new ObjectPoint(geoi);
                // en fonction de la clé présente dans les propriété, on stocke dans la liste des banc ou des tables de pic-nic
                if (properties.hasKey("amenity") && properties.getString("amenity").equals("bench")){
                    this.l_bench.add(poi);
                }
                if(properties.hasKey("leisure") && properties.getString("leisure").equals("picnic_table")){
                    this.l_pic_nic.add(poi);
                }
            }
        }
    }



}
class Railways{
    Map3D map;
    PShape railways;

    /** Constructeur de voie ferrée
     * @param Map3D     map
     * @param String    fileName
     */
    Railways(Map3D map, String fileName){
        this.map = map;
        float laneWidth = 2.5f;
        // Vérifie que le fichier existe bien
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: GeoJSON file " + fileName + " not found.");
            return;
        }
        // Charge le geoJSON et récupère la collection qu'il contient
        JSONObject geojson = loadJSONObject(fileName);
        if (!geojson.hasKey("type")) {
            println("WARNING: Invalid GeoJSON file.");
            return;
        } else if (!"FeatureCollection".equals(geojson.getString("type", "undefined"))) {
            println("WARNING: GeoJSON file doesn't contain features collection.");
            return;
        }

        // Sépare les différents éléments du JSON
        JSONArray features =  geojson.getJSONArray("features");
        if (features == null) {
            println("WARNING: GeoJSON file doesn't contain any feature.");
            return;
        }
        // Création de la forme pour les rails, un groupe qui contiendra les différentes parties du rail
        this.railways = createShape(GROUP);
        boolean is_bridge;
        for (int f=0; f<features.size(); f++) {
            // On suppose que la feature n'est pas un pont
            is_bridge = false;
            // On vérifie que l'objet a bien des coordonnées
            JSONObject feature = features.getJSONObject(f);
            if (!feature.hasKey("geometry"))
                break;
            // Si les propriété ont une clé "bridge" on passe le booléen à true
            if (feature.getJSONObject("properties").hasKey("bridge"))
                is_bridge = true;
            JSONObject geometry = feature.getJSONObject("geometry");
            switch (geometry.getString("type", "undefined")) {
                // Si la géométrie est de type LineString
                case "LineString":
                    // railways
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    // Si les coordonnées ne sont pas vides
                    if (coordinates != null){
                        // On crée un nouveau tronçon de rail
                        PShape lane;
                        lane = createShape();
                        lane.beginShape(QUAD_STRIP);
                        lane.fill(0xff0000FF);
                        lane.stroke(0xff0000FF);
                        lane.strokeWeight(1.5f);
                        // On déclare les vecteurs et points que l'on va utiliser
                        PVector vOrtho01, vOrtho02;
                        Map3D.ObjectPoint r0, r1, r2;
                        Map3D.GeoPoint g, g1;
                        JSONArray point;
                        // On récupère les coordonnées des deux premiers points et on en fait des GeoPoint
                        point = coordinates.getJSONArray(0);
                        g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                        point = coordinates.getJSONArray(1);
                        g1 = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                        // Si ils sont bien dans notre espa&ce de travail
                        if(g.elevation > 0 && g1.elevation > 0){
                            // On les déplace un peu au dessus du terrain
                            g1.elevation+=7.5f;
                            g.elevation+=7.5f;
                            r0 = this.map.new ObjectPoint(g);
                            r1 = this.map.new ObjectPoint(g1);
                            // On calcule le vecteur orthogonal aux points
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.f);
                            // On place les deux premiers points du tronçon
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r0.x+vOrtho01.x, r0.y+vOrtho01.y, r0.z);
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r0.x-vOrtho01.x, r0.y-vOrtho01.y, r0.z);
                            // Pour tous les points suivants
                            for (int p=2; p < coordinates.size(); p++) {
                                // On converti les coordonnées en GeoPoint
                                point = coordinates.getJSONArray(p);
                                g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                                // et si il est dans l'espace de travail
                                if(g.elevation > 0){
                                    g.elevation+=7.5f;
                                    r2 = this.map.new ObjectPoint(g);
                                    // Si c'est un pont, on lui indique de prendre la hauteur du premier point du pont (elle sera donc conservée sur toute la longueur du pont)
                                    if (is_bridge){
                                        r1.z = r0.z;
                                    }
                                    // On calcule le vecteur orthogonal au point précedent et suivant pour savoir où placer les points de la shape
                                    vOrtho02 = new PVector(r0.y-r2.y, r2.x-r0.x).normalize().mult(laneWidth/2.f);
                                    // On place ces points
                                    lane.normal(0.0f, 0.0f, 1.0f);
                                    lane.vertex(r1.x+vOrtho02.x, r1.y+vOrtho02.y, r1.z);
                                    lane.normal(0.0f, 0.0f, 1.0f);
                                    lane.vertex(r1.x-vOrtho02.x, r1.y-vOrtho02.y, r1.z);
                                    // On décale nos variables
                                    r0 = r1;
                                    r1 = r2;
                                }
                            }
                            // Pour le dernier points, on utilise la même méthode que pour placer le premier
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.f);
                            // On place le dernier point
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r1.x+vOrtho01.x, r1.y+vOrtho01.y, r1.z);
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r1.x-vOrtho01.x, r1.y-vOrtho01.y, r1.z);
                        }
                        // On ajoute le tronçon à la shape générale et on fini le tronçon
                        this.railways.addChild(lane);
                        lane.endShape();
                    }
                    break;
                // Si ce n'est pas une LineString, on indique que le type n'est pas traité
                default:
                    println("WARNING: GeoJSON '" + geometry.getString("type", "undefined") + "' geometry type not handled.");
                    break;
            }
        }
    }

    /** update
     * Met à jour l'affichage des rails
     */
    public void update(){
        shape(this.railways);
    }

    /** toggle
     * Active et désactive l'affichage des rails
     */
    public void toggle(){
        this.railways.setVisible(!this.railways.isVisible());
    }
}
class Roads{
    Map3D map;
    PShape roads;

    /** Constructeur de routes
     * @param Map3D     map
     * @param String    fileName
     */
    Roads(Map3D map, String fileName){
        this.map = map;
        String laneKind = "unclassified";
        int laneColor = 0xFFFF0000;
        double laneOffset = 1.50d;
        float laneWidth = 0.5f;
        // Vérifie que le fichier existe bien
        File ressource = dataFile(fileName);
        if (!ressource.exists() || ressource.isDirectory()) {
            println("ERROR: GeoJSON file " + fileName + " not found.");
            return;
        }
        // Charge le geoJSON et récupère la collection qu'il contient
        JSONObject geojson = loadJSONObject(fileName);
        if (!geojson.hasKey("type")) {
            println("WARNING: Invalid GeoJSON file.");
            return;
        } else if (!"FeatureCollection".equals(geojson.getString("type", "undefined"))) {
            println("WARNING: GeoJSON file doesn't contain features collection.");
            return;
        }

        // Sépare les différents éléments du JSON
        JSONArray features =  geojson.getJSONArray("features");
        if (features == null) {
            println("WARNING: GeoJSON file doesn't contain any feature.");
            return;
        }
        // Création de la forme pour les routes, un groupe qui contiendra les différents tronçons des routes
        this.roads = createShape(GROUP);
        for (int f=0; f<features.size(); f++) {
            JSONObject feature = features.getJSONObject(f);
            // On vérifie que l'objet a bien des coordonnées
            if (!feature.hasKey("geometry"))
                break;
            laneKind = feature.getJSONObject("properties").getString("highway", "unclassified");
            switch (laneKind) {
                case "motorway":
                    laneColor = 0xFFe990a0;
                    laneOffset = 3.75d;
                    laneWidth = 8.0f;
                    break;
                case "trunk":
                    laneColor = 0xFFfbb29a;
                    laneOffset = 3.60d;
                    laneWidth = 7.0f;
                    break;
                case "trunk_link":
                case "primary":
                    laneColor = 0xFFfdd7a1;
                    laneOffset = 3.45d;
                    laneWidth = 6.0f;
                    break;
                case "secondary":
                case "primary_link":
                    laneColor = 0xFFf6fabb;
                    laneOffset = 3.30d;
                    laneWidth = 5.0f;
                    break;
                case "tertiary":
                case "secondary_link":
                    laneColor = 0xFFE2E5A9;
                    laneOffset = 3.15d;
                    laneWidth = 4.0f;
                    break;
                case "tertiary_link":
                case "residential":
                case "construction":
                case "living_street":
                    laneColor = 0xFFB2B485;
                    laneOffset = 3.00d;
                    laneWidth = 3.5f;
                    break;
                case "corridor":
                case "cycleway":
                case "footway":
                case "path":
                case "pedestrian":
                case "service":
                case "steps":
                case "track":
                case "unclassified":
                    laneColor = 0xFFcee8B9;
                    laneOffset = 2.85d;
                    laneWidth = 1.0f;
                    break;
                default:
                    laneColor = 0xFFFF0000;
                    laneOffset = 1.50d;
                    laneWidth = 0.5f;
                    println("WARNING: Roads kind not handled : ", laneKind);
                    break;
                }

                // Display threshold (increase  if more performance needed...)
                if (laneWidth < 1.0f)
                break;
            JSONObject geometry = feature.getJSONObject("geometry");
            switch (geometry.getString("type", "undefined")) {
                // Si la géométrie est de type LineString
                case "LineString":
                    // roads
                    JSONArray coordinates = geometry.getJSONArray("coordinates");
                    // Si les coordonnées ne sont pas vides
                    if (coordinates != null){
                        // On crée un nouveau tronçon de route
                        PShape lane;
                        lane = createShape();
                        lane.beginShape(QUAD_STRIP);
                        lane.fill(laneColor);
                        lane.noStroke();
                        // On déclare les vecteurs et points que l'on va utiliser
                        PVector vOrtho01, vOrtho12, vOrtho02;
                        Map3D.ObjectPoint r0, r1, r2;
                        Map3D.GeoPoint g, g1;
                        JSONArray point;
                        // On récupère les coordonnées des deux premiers points et on en fait des GeoPoint
                        point = coordinates.getJSONArray(0);
                        g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                        point = coordinates.getJSONArray(1);
                        g1 = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                        // Si ils sont bien dans notre espace de travail
                        if(g.elevation > 0 && g1.elevation > 0){
                            // On les déplace un peu au dessus du terrain
                            g1.elevation += laneOffset;
                            g.elevation += laneOffset;
                            r0 = this.map.new ObjectPoint(g);
                            r1 = this.map.new ObjectPoint(g1);
                            // On calcule le vecteur orthogonal aux points
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.f);
                            // On place les deux premiers points du tronçon
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r0.x+vOrtho01.x, r0.y+vOrtho01.y, r0.z);
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r0.x-vOrtho01.x, r0.y-vOrtho01.y, r0.z);
                            // Pour tous les points suivants
                            for (int p=2; p < coordinates.size(); p++) {
                                // On converti les coordonnées en GeoPoint
                                point = coordinates.getJSONArray(p);
                                g = this.map.new GeoPoint(point.getDouble(0), point.getDouble(1));
                                // et si il est dans l'espace de travail
                                if(g.elevation > 0){
                                    g.elevation += laneOffset;
                                    r2 = this.map.new ObjectPoint(g);
                                    // On calcule le vecteur orthogonal au point précedent et suivant pour savoir où placer les points de la shape
                                    vOrtho02 = new PVector(r0.y-r2.y, r2.x-r0.x).normalize().mult(laneWidth/2.f);
                                    // On place ces points
                                    lane.normal(0.0f, 0.0f, 1.0f);
                                    lane.vertex(r1.x+vOrtho02.x, r1.y+vOrtho02.y, r1.z);
                                    lane.normal(0.0f, 0.0f, 1.0f);
                                    lane.vertex(r1.x-vOrtho02.x, r1.y-vOrtho02.y, r1.z);
                                    // On décale nos variables
                                    r0 = r1;
                                    r1 = r2;
                                }
                            }
                            // Pour le dernier points, on utilise la même méthode que pour placer le premier
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.f);
                            // On place le dernier point
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r1.x+vOrtho01.x, r1.y+vOrtho01.y, r1.z);
                            lane.normal(0.0f, 0.0f, 1.0f);
                            lane.vertex(r1.x-vOrtho01.x, r1.y-vOrtho01.y, r1.z);
                        }
                        // On ajoute le tronçon à la shape générale et on fini le tronçon
                        this.roads.addChild(lane);
                        lane.endShape();
                    }
                    break;
                // Si ce n'est pas une LineString, on indique que le type n'est pas traité
                default:
                    println("WARNING: GeoJSON '" + geometry.getString("type", "undefined") + "' geometry type not handled.");
                    break;
            }
        }
    }

    /** update
     * Met à jour l'affichage des routes
     */
    public void update(){
        shape(this.roads);
    }

    /** toggle
     * Active et désactive l'affichage des routes
     */
    public void toggle(){
        this.roads.setVisible(!this.roads.isVisible());
    }
}
class WorkSpace {
    //initialisation des arguments de la classe
    PShape gizmo;
    PShape grid;

    /**Constructeur de WorkSpace
     * Prepares les shapes WorkSpace gizmo et grid
     * @param  size      la taille d'un coté d'une case de la grille
     **/
    WorkSpace(int size){
        // Gizmo : le repere XYZ

        this.gizmo = createShape();         //Creation de la Shape pour le gizmo
        this.gizmo.beginShape(LINES);       //On utilise une Shape de type LINES
        this.gizmo.noFill();
        this.gizmo.strokeWeight(5.0f);

        // L'axe X plus épais de l'origine à size
        this.gizmo.stroke(0xAAFF3F7F);      //Couleur rouge
        this.gizmo.vertex(0,0,0);
        this.gizmo.vertex(size, 0, 0);

        // L'axe Y plus épais de l'origine à size
        this.gizmo.stroke(0xAA3FFF7F);      //Couleur verte
        this.gizmo.vertex(0,0,0);
        this.gizmo.vertex(0, size, 0);

        // L'axe Z plus épais de l'origine à size
        this.gizmo.stroke(0xAA3F7FFF);      //Couleur bleue
        this.gizmo.vertex(0,0,0);
        this.gizmo.vertex(0, 0, size);

        this.gizmo.strokeWeight(0.5f);
        // L'axe X
        this.gizmo.stroke(0xAAFF3F7F);      //Couleur rouge
        this.gizmo.vertex(-size*100,0,0);
        this.gizmo.vertex(size*100, 0, 0);
        // L'axe Y
        this.gizmo.stroke(0xAA3FFF7F);      //Couleur verte
        this.gizmo.vertex(0,-size*100, 0);
        this.gizmo.vertex(0, size*100, 0);
                                            //Nous n'affichons pas l'axe Z volontairement
        this.gizmo.endShape();              //Fin de la Shape

        // Grille
        this.grid = createShape();                          //Creation d'une Shape pour la grille
        this.grid.beginShape(QUADS);                        //On utilise une Shape de type QUADS
        this.grid.noFill();
        this.grid.stroke(0x77836C3D);
        this.grid.strokeWeight(0.5f);
        for (int i=-100; i<=100; i++){                      //On realise une grille de 100*100 cases et chaque case est de taille size*size
            for(int j=-100; j<=100; j++){
                this.grid.vertex(i*size,j*size,0);
                this.grid.vertex(i*size,(j+1)*size,0);
                this.grid.vertex((i+1)*size,(j+1)*size,0);
                this.grid.vertex((i+1)*size,j*size,0);
            }
        }
        this.grid.endShape();                               //Fin de la Shape
    }

    /**MAJ du WorkSpace
      */
    public void update(){
        if(this.gizmo.isVisible())
            shape(this.gizmo);
            shape(this.grid);
    }

    /** Toggle Grid & Gizmo visibility.
      */
    public void toggle() {
        this.gizmo.setVisible(!this.gizmo.isVisible());
        this.grid.setVisible(!this.grid.isVisible());
    }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "projet" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
