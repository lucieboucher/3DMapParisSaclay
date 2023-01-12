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
            this.shadow.vertex(-w/2., -h/2., -10);  //On positionne les 4 vertex aux 4 extrémités
            this.shadow.vertex(w/2., -h/2., -10);
            this.shadow.vertex(w/2., h/2., -10);
            this.shadow.vertex(-w/2., h/2., -10);
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
            this.wireFrame.stroke(#888888);
            this.wireFrame.strokeWeight(0.5f);

            //Points de tracé
            Map3D.ObjectPoint v0, v1, v2, v3;
            PVector n0, n1, n2, n3;

            //Information pour la chaleur
            float d_bench, d_pic_nic;
            //Coordonnées texture
            float u = 0, v;
            for (float x = -w/2.; x < w/2.; x += tileSize){
                //remet v en bas de l'image
                v = 0;
                for (float y = -h/2.; y < h/2.; y += tileSize){
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
    float dist_min(Map3D.ObjectPoint v, ArrayList<Map3D.ObjectPoint> l){
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
    void update(PShader myShader){
        shape(this.shadow);
        shape(this.wireFrame);
        shader(myShader);
        shape(this.satellite);
    }

    /** Toggle Land visibility.
      */
    void toggle() {
        this.satellite.setVisible(!this.satellite.isVisible());
        this.wireFrame.setVisible(!this.wireFrame.isVisible());
    }

}
