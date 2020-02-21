import java.awt.Graphics2D;

public class KeyEntity extends Entity{
	private Game game;
	
	public KeyEntity(Game g,String r, int newX, int newY) {
		super(r, newX, newY);
		game = g;
	}

	public void collidedWith(Entity other) {}

	public void rotate(Graphics2D g, double angle, double xRotate, double yRotate) {}

} // KeyEntity
