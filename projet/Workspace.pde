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
    void update(){
        shape(this.gizmo);
        shape(this.grid);
    }

    /** Toggle Grid & Gizmo visibility.
      */
    void toggle() {
        this.gizmo.setVisible(!this.gizmo.isVisible());
        this.grid.setVisible(!this.grid.isVisible());
    }
}
