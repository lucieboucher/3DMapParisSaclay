class Railways{
    Map3D map;
    PShape railways;

    /** Constructeur de voie ferrée
     * @param Map3D     map
     * @param String    fileName
     */
    Railways(Map3D map, String fileName){
        this.map = map;
        float laneWidth = 2.5;
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
                        lane.fill(#0000FF);
                        lane.stroke(#0000FF);
                        lane.strokeWeight(1.5);
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
                            g1.elevation+=7.5;
                            g.elevation+=7.5;
                            r0 = this.map.new ObjectPoint(g);
                            r1 = this.map.new ObjectPoint(g1);
                            // On calcule le vecteur orthogonal aux points
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.);
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
                                    g.elevation+=7.5;
                                    r2 = this.map.new ObjectPoint(g);
                                    // Si c'est un pont, on lui indique de prendre la hauteur du premier point du pont (elle sera donc conservée sur toute la longueur du pont)
                                    if (is_bridge){
                                        r1.z = r0.z;
                                    }
                                    // On calcule le vecteur orthogonal au point précedent et suivant pour savoir où placer les points de la shape
                                    vOrtho02 = new PVector(r0.y-r2.y, r2.x-r0.x).normalize().mult(laneWidth/2.);
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
                            vOrtho01 = new PVector(r0.y-r1.y, r1.x-r0.x).normalize().mult(laneWidth/2.);
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
    void update(){
        shape(this.railways);
    }

    /** toggle
     * Active et désactive l'affichage des rails
     */
    void toggle(){
        this.railways.setVisible(!this.railways.isVisible());
    }
}
