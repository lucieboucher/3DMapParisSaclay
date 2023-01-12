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
    void update(){
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
    void toggle(){
        this.lightning = !this.lightning;
    }

    /** adjustRadius
     * Gère le zoom de la caméra
     */
    void adjustRadius(float offset){
        // Défini une limite au rayon minimal et maximal
        if (radius+offset < width * 3.0 && radius+offset > width * 0.5){
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
    void adjustLongitude(float delta){
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
    void adjustColatitude(float delta){
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
    void moveZS(float r){
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
    void moveQD(float r){
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
