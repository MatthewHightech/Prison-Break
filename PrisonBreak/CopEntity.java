import java.awt.Graphics;
import java.awt.Graphics2D;

/* AlienEntity.java
 * March 27, 2006
 * Represents one of the aliens
 */
public class CopEntity extends Entity {

  private double moveSpeed = 75; // horizontal speed
  private Game game; // the game in which the cop exists
  private double lWall, rWall;

  
  /* construct a new alien
   * input: game - the game in which the cop is being created
   *        r - the image representing the cop
   *        x, y - initial location of cop
   */
  public CopEntity(Game g, String r, int newX, int newY, int lWall, int rWall) {
    super(r, newX, newY);  // calls the constructor in Entity
    game = g;
    dx = -moveSpeed;  // start off moving left
    this.lWall = lWall;
    this.rWall = rWall;
  } // constructor

   /* move
   * input: delta - time elapsed since last move (ms)
   * purpose: move alien
   */
  public void move (long delta){
    // if we reach left wall and are moving left
    // request logic update
    if ((dx < 0) && (x < lWall)) {
        //game.updateLogic();   // logic deals with moving entities
                              // in other direction
    	logicReq = true;
    } // if

    // if we reach right wall and are moving right
    // request logic update
    if ((dx > 0) && (x > rWall)) {
        //game.updateLogic();
    	logicReq = true;
    } // if
    
    // proceed with normal move
    super.move(delta);//-------------Cop is currently not moving for test purposes 
  } // move


  /* doLogic
   * Updates the game logic related to the aliens,
   * ie. move it down the screen and change direction
   */
  public void doLogic() {
    // swap horizontal direction and move down screen 10 pixels
    dx *= -1;
    //y += 10; commented so no move down

    // if bottom of screen reached, player dies
    if (y > 970) {
      //game.notifyDeath();
    } // if
  } // doLogic


  /* collidedWith
   * input: other - the entity with which the alien has collided
   * purpose: notification that the alien has collided
   *          with something
   */
   public void collidedWith(Entity other) {} // collidedWith

   public void rotate(Graphics2D g, double angle, double xRotate, double yRotate) {}
  
} // AlienEntity class
