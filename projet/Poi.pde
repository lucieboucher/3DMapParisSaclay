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
    void getPoint(String fileName){
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
