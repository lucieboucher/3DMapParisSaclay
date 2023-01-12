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
        color laneColor = 0xFFFF0000;
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
                                    g.elevation += laneOffset;
                                    r2 = this.map.new ObjectPoint(g);
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
    void update(){
        shape(this.roads);
    }

    /** toggle
     * Active et désactive l'affichage des routes
     */
    void toggle(){
        this.roads.setVisible(!this.roads.isVisible());
    }
}
