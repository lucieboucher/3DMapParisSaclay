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
void settings(){
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
void setup() {
    // Met en place le Head Up Display
    this.hud = new Hud();
    // 3D camera (X+ right / Z+ top / Y+ Front)
    this.camera = new Camera(PI/3,PI/3,width * 2.0);
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
void draw(){
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
void keyPressed(){
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
void mousePressed(){
    // Affiche les description des épingles
    if (mouseButton == LEFT){
        this.gpx.clic(mouseX, mouseY);
    }
}

/** mouseWheel
 * Récupère les actions de molette de la souris clavier de l'utilisateur pour gérer les contrôles du sketch
 */
void mouseWheel(MouseEvent event) {
   float ec = event.getCount();
   // Zoom
   this.camera.adjustRadius(20*ec);
}

/** mouseDragged
 * Récupère les actions de cliquer-glisser à la souris au clavier de l'utilisateur pour gérer les contrôles du sketch
 */
void mouseDragged() {
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
float radToDegree(float angle){
    return angle*180/PI;
}
