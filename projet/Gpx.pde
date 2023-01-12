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
        this.posts.stroke(#FF0000);
        this.posts.strokeWeight(2.f);
        // Créeation de la forme pour les têtes d'épingle
        this.thumbtacks = createShape();
        this.thumbtacks.beginShape(POINTS);
        this.thumbtacks.noFill();
        this.thumbtacks.stroke(#FF0000);
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
                        this.track.stroke(#FF0000);
                        this.track.strokeWeight(1.5);
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
                        g0.elevation += 7.9;
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
    void update(){
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
    void toggle(){
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
    void clic(int mouseX, int mouseY){
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
    void displayDescription(int nb, Camera camera){
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
