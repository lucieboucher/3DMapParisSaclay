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
    void add(String fileName, color c){

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
    void update(){
        shape(this.buildings);
    }
    /** Toggle Buildings visibility.
      */
    void toggle(){
        this.buildings.setVisible(!this.buildings.isVisible());
    }
}
