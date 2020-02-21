/* ShipEntity.java
 * March 27, 2006
 * Represents player's ship
 */

import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class PlayerEntity extends Entity {
  private long lastDeath = 0; // time last shot fired
  private long deathInterval = 500;
  private Game game; // the game in which the player exists
  private ArrayList <int[]> tileLocations; // location of each solid map tile
  /* construct the player's ship
   * input: game - the game in which the player is being created
   *        ref - a string with the name of the image associated to
   *              the sprite for the player
   *        x, y - initial location of player
   */
  public PlayerEntity(Game g, String r, int newX, int newY, ArrayList tileLocations) {
    super(r, newX, newY);  // calls the constructor in Entity
    game = g;
    this.tileLocations = tileLocations;
  } // constructor

  /* move
   * input: delta - time elapsed since last move (ms)
   * purpose: move player 
   */
  public void move (long delta){
	  
	isOnBlock = false;
    // stop if going into solid tile
    for(int i = 0; i < tileLocations.size(); i ++) {
    	double tileX = tileLocations.get(i)[0];
    	double tileY = tileLocations.get(i)[1];
    	double playX = x + (delta * dx) / 1000;
    	double playY = y + (delta * dy) / 1000;
    	
    	// if going down/up into tile
    	if(dy != 0 && playY >= tileY - 50 && playY <= tileY + 25
    			   && x <= tileX + 25 && x >= tileX - 25) {
    		if(dy > 0) {
    			isOnBlock = true;
    		} // if
    		dy = 0;
    	} // if
    	
    	// if moving right/left into tile
    	if(dx > 0 && y >= tileY - 50 && y <= tileY + 25
    			  && playX >= tileX - 25 && playX <= tileX + 25) {
    		dx = 0;
    	} // if
    	
    	if (dx < 0 && y >= tileY - 50 && y <= tileY + 25
    	           && playX <= tileX + 25 && playX >= tileX - 25) {
    		dx = 0; 
    	} // if

    } // for
    super.move(delta);  // calls the move method in Entity
  } // move
  
  public void rotate (Graphics2D g, double angle, double xRotate, double yRotate) {} // Rotate 

  /* collidedWith
   * input: other - the entity with which the player has collided
   * purpose: notification that the player has collided
   *          with something
   */
   public void collidedWith(Entity other) {
     if (other instanceof CopEntity) {
    	 if (System.currentTimeMillis() - lastDeath < deathInterval){
             return;
         } // if
         game.bounceAway(System.currentTimeMillis());
         
         lastDeath = System.currentTimeMillis();
         game.lives--;
    	 if(game.lives == 0) {
    		 game.notifyDeath();
    	 } // if   
     } // if
     // if player collides with key add a key to keyCounter
     if (other instanceof KeyEntity) {
			game.keyCounter++;
			game.removeEntity(other);
     } // if
     
     //if player collides with door they proceed to next level
     //if they have a key and if they have 4 keys the level is 4
     //they win
     if (other instanceof DoorEntity) {
    	 if(game.currentLevel == 5) {
    		 game.notifyWin();
    	 }//if
    	 if(game.keyCounter == game.currentLevel) {
    		 game.currentLevel++;
    		 game.notifyNextLevel();
    	 }//if
     } // if
     
     //if player collides with AmmoEntity they refill there ammo
     if (other instanceof AmmoEntity) {
    		 game.ammoCount = 6;  	
     } // if
   } // collidedWith    

} // PlayerEntity class