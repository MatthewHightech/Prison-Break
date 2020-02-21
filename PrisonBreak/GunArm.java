import java.awt.Graphics2D;

public class GunArm extends Entity{
	private Game game;
	
	public GunArm(Game g,String r, int newX, int newY) {
		super(r, newX, newY);
		game = g;
	}
	
	 public void move (long delta){
		    // stop at left side of screen
		    if ((dx < 0) && (x < 10)) {
		      return;
		    } // if
		    // stop at right side of screen
		    if ((dx > 0) && (x > 935)) {
		      return;
		    } // if
		    super.move(delta);
	 } // move

	public void collidedWith(Entity other) {}

	public void rotate(Graphics2D g, double angle, double xRotate, double yRotate) {
		sprite.rotateSprite(g, angle, xRotate, yRotate); 
	} // rotate

} // KeyEntity