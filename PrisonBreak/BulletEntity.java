import java.awt.Graphics2D;
import java.util.ArrayList;

/* ShotEntity.java
 * March 27, 2006
 * Represents player's ship
 */
public class BulletEntity extends Entity {

  private double moveSpeed = -300; // vert speed shot moves
  private boolean used = false; // true if shot hits something
  private Game game; // the game in which the ship exists
  private ArrayList <int[]> tileLocations; // location of each solid map tile
  /* construct the shot
   * input: game - the game in which the shot is being created
   *        ref - a string with the name of the image associated to
   *              the sprite for the shot
   *        x, y - initial location of shot
   */
  public BulletEntity(Game g, String r, int newX, int newY, double x, double y, ArrayList tileLocations) {
    super(r, newX, newY);  // calls the constructor in Entity
    game = g;
    xVelocity = x;  
    yVelocity = y;
    dx = xVelocity;
    dy = yVelocity;
    this.tileLocations = tileLocations;
  } // constructor

  /* move
   * input: delta - time elapsed since last move (ms)
   * purpose: move shot
   */
  public void move (long delta){
	 
    super.move(delta);  // calls the move method in Entity with dx and dy set to xVelocity and yVelocity
    
    // if shot moves off top of screen, remove it from entity list
    if (y < -100) {
      game.removeEntity(this);
    } // if
    
    
    //Make bullets disappear
    //when they hit the tiles
    for(int i = 0; i < tileLocations.size(); i ++) {
    	double tileX = tileLocations.get(i)[0];
    	double tileY = tileLocations.get(i)[1];
    	double playX = x;
    	double playY = y;
    	
    	// if going down/up into tile
    	if(dy != 0 && playY >= tileY - 5 && playY <= tileY + 5
    			   && x <= tileX + 25 && x >= tileX - 25) {
    		game.removeEntity(this);
    	} // if
 
    	// if moving right/left into tile
    	if(dx > 0 && y >= tileY && y <= tileY
    			  && playX >= tileX - 20 && playX <= tileX + 20) {
    		game.removeEntity(this);
    	} // if
    	
    	if (dx < 0 && y >= tileY && y <= tileY
    	           && playX <= tileX + 20 && playX >= tileX - 20) {
    		game.removeEntity(this); 
    	} // if

    } // for
  

  } // move

  /* collidedWith
   * input: other - the entity with which the shot has collided
   * purpose: notification that the shot has collided
   *          with something
   */
   public void collidedWith(Entity other) {
     // prevents double kills
     if (used) {
       return;
     } // if
     
     // if it has hit an alien, kill it!
     if (other instanceof CopEntity) {
       // remove affect entities from the Entity list
       game.removeEntity(this);
       game.removeEntity(other);
       
       // notify the game that the alien is dead
       game.notifyCopKilled(other);
       used = true;
     } // if
   } // collidedWith

   public void rotate(Graphics2D g, double angle, double xRotate, double yRotate) {}

} // ShipEntity class

