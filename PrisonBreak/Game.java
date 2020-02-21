/***************************************************************\
* Name: Prison Escapes									        *
* Author: Created by Ari van Everdingen, Matt Smith, Tyson Chan	*
* Date: Oct 28, 2019							                *
* Purpose: to simulate a "real life" prison escape experience   *
\***************************************************************/


import javax.swing.*;

import java.lang.Object;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class Game extends Canvas {

		//private int [][] tileMap = new int [32][40];
		private MapMaker levelMap;
		private ArrayList <int[]> tileLocations = new ArrayList <int[]> ();
		private char[][] tileMap;

      	private BufferStrategy strategy;   // take advantage of accelerated graphics
        private boolean waitingForKeyPress = true;  // true if game held up until
                                                    // a key is pressed
        private boolean leftPressed = false;  // true if left arrow key currently pressed
        private boolean rightPressed = false; // true if right arrow key currently pressed
        private boolean firePressed = false; // true if firing (Mouse event) 
        private boolean jumpPressed = false; // true if w pressed
        private boolean shiftPressed = false;
        private boolean notBouncingAway = true;
        
        // variable regarding animation
        private String playerAnimate = "sprites/player1.png";
        private int animateCounterR = 0; 
        private int animateCounterL = 0; 
        private int copAnimateCounterR = 0; 
        private int copAnimateCounterL = 0; 
        private ArrayList <Long> lastCopPrint = new ArrayList <Long> (); 
        
        
        private static int mX = 0; // mouse click x coordinate
        private static int mY = 0; // mouse click y coordinate
        private static int mXConstant = 0; // mouse click x coordinate
        private static int mYConstant = 0; // mouse click y coordinate
        
        private double rotatingAngle = 0; // angle between player and mouse (constant) 
        private double angle = 0; // angle between player and mouse click
        private double xVelocity = 0; // x velocity based on angle 
        private double yVelocity = 0; // y velocity based on angle

        private boolean gameRunning = true; 
        private ArrayList <Entity> entities = new ArrayList<Entity>(); // list of entities in game
                                                      
        private ArrayList <Entity> removeEntities = new ArrayList<Entity>(); // list of entities
                                                         // to remove this loop
        private Entity player;  // the player 
        private Entity gunArm;  // the pointer of where shot will go
        private Entity key;  // the key to continue through levels
        private Entity cop;  // the cops take one life when you touch them
        private Entity door;  // the door to next level or end
        private Entity ammoBox;  // the door to next level or end
        private int copCount; // # of cops left on screen
        private long hitTime = 0;
        
        private long lastFire = 0; // time last shot fired
        private long firingInterval = 250; // interval between shots (ms)
        
        // jump related variables
        private long timeDYIs0 = 0; // keeps track of how long dy has been 0
        private int jumpRestrictor = 20; // how long dy has to be 0 to jump again
        
        // gravity related variables
        private long lastGravity = 0;
        private int gravityInterval = 10;
        private int gravity = 26;
        
        // acceleration related variables
        private int maxSpeed = 200;
        private int acceleration = 10;
        private int accelInterval = 400;
        private long lastAccel = 0;
        private double currentSpeed = 0;
        private boolean rightLastPressed;
        
        // respawning cops
        private ArrayList <Entity> deadCops = new ArrayList <Entity>();
        private ArrayList <Long> copDeathTimes = new ArrayList <Long>();
        private long respawnInterval = 15000; // 15 seconds
        
        private String message = "sprites/PrisonEscapeTitle.png"; // message to display while waiting
                                     // for a key press
       
        //Counters
        public int keyCounter = 0; //amount of keys player collected
        public int lives = 3; //amount of lives
        public int ammoCount = 6; //amount of lives
        public int currentLevel = 1; // current level player is on;
       
        //misc
        private Sprite level;// image displaying current level
        static Image image; // variable for background image
        private boolean atStart = true;// true if you are at the start or menu
        private boolean readInstructions = false;//true if you go to the instructions tab
        
        private boolean logicRequiredThisLoop = false; // true if logic needs to be applied on this loop
                                                       

    	/*
    	 * Construct our game and set it running.
    	 */
    	public Game() {
    		// create a frame to contain game
    		JFrame container = new JFrame("Prison Escape");
    
    		// get hold the content of the frame
    		JPanel panel = (JPanel) container.getContentPane();
    	
    		// set up the resolution of the game
    		panel.setPreferredSize(new Dimension(1000,800));
    		panel.setLayout(null);
    
    		// set up canvas size (this) and add to frame
    		setBounds(0,0,1000,800);
    		panel.add(this);
    
    		// Tell AWT not to bother repainting canvas since that will
            // be done using graphics acceleration
    		setIgnoreRepaint(true);
    
    		// make the window visible
    		container.pack();
    		container.setResizable(false);
    		container.setVisible(true);
    
    
            // if user closes window, shutdown game and jre
    		container.addWindowListener(new WindowAdapter() {
    			public void windowClosing(WindowEvent e) {
    				System.exit(0);
    			} // windowClosing
    		});
    
    		// add key listener to this canvas
    		addKeyListener(new KeyInputHandler());
    		// add mouse listener to this canvas
    		addMouseListener(new MouseListenerExample());
    		addMouseMotionListener(new MouseListenerExample());
    		// request focus so key events are handled by this canvas
    		requestFocus();

    		// create buffer strategy to take advantage of accelerated graphics
    		createBufferStrategy(2);
    		strategy = getBufferStrategy();
    		
    		// draws the blocks on the map
    		initMap();
    		
    		// initialize entities
    		initEntities();
    		// start the game
    		gameLoop();
        } // constructor
    	
    	
        /* initEntities
         * input: none
         * output: none
         * purpose: Initialize the starting state of the player and cop entities.
         *          Each entity will be added to the array of entities in the game.
    	 */
    	private void initEntities() {
    		copCount = 0; // resets copCount
    		
    		// loops through the map, checking each space for input
	        for(int i = 0; i < tileMap.length; i++) {
	        	for(int j = 0; j < tileMap[i].length; j++) {
	        		// place player
	        		if(tileMap[i][j] == 'p') {
	        			player = new PlayerEntity(this, playerAnimate, j * 25, i * 25 - 5, tileLocations);
	        			entities.add(player);
	        			  
	        			// place key
	        		} else if(tileMap[i][j] == 'k') {
            			key = new KeyEntity(this, "sprites/key.png", j * 25, i * 25);
            			entities.add(key);
            			  
            			// place cop
            		} else if(tileMap[i][j] == 'c') {
            			int lWall = 0;
            			int rWall = 0;
            			
            			// determine cop's boundaries
            			for(int k = 0; k < tileMap[i].length; k++) {
            				if(tileMap[i][k] == 'l') {
            					lWall = k * 25;
            				} else if(tileMap[i][k] == 'r') {
            					rWall = k* 25;
            				} // else if
            			} // for k
            			cop = new CopEntity(this, "sprites/Cop1.png", j * 25, (i * 25) - 25, lWall, rWall);
            			entities.add(cop);
            			copCount++;
            			
            			// place door
            		} else if(tileMap[i][j] == 'd') {
            			door = new DoorEntity(this, "sprites/door.png", j * 25 - 20, i * 25 - 14);
            			entities.add(door);
            			// place ammoBox
            		}  else if(tileMap[i][j] == 'a') {
            			ammoBox = new AmmoEntity(this, "sprites/ammoBox.png", j * 25 - 18, i * 25 + 5);
            			entities.add(ammoBox);
            		}
	        	} // for j
	        } // for i
            
	        // add player's arm
            gunArm = new GunArm(this, "sprites/gunArm.png", player.getX() + 13, player.getY() + 26);
            entities.add(gunArm);
            
            for (int i = 0; i <= (copCount+4); i++) {
            	lastCopPrint.add((long) 0); 
            }
            
    	} // initEntities

    	public void initMap () { 
    		levelMap = new MapMaker(currentLevel);
    		tileMap = levelMap.getTileMap();
    		//print2DArray(tileMap);
    		
            // define tile locations
    		for (int i = 0; i < tileMap.length; i++) {
    			for (int j = 0; j < tileMap[i].length; j++) {
    				if (tileMap[i][j] == '1') {
    					int[] temp = {j * 25, i * 25};
    					tileLocations.add(temp);
    				} // if
    			} // for j
    		} // for i
    		
    	} //  drawMap 

        /* Notification from a game entity that the logic of the game
         * should be run at the next opportunity 
         */
         public void updateLogic() {
           logicRequiredThisLoop = true;
         } // updateLogic

         /* Remove an entity from the game.  It will no longer be
          * moved or drawn.
          */
         public void removeEntity(Entity entity) {
           removeEntities.add(entity);
         } // removeEntity
          
         public void notifyDeath() {
        	 deadCops.clear();
        	 copDeathTimes.clear();
        	 message = "sprites/deathScreen.png";
        	 keyCounter = 0;
        	 ammoCount = 6; 
        	 atStart = false;
        	 waitingForKeyPress = true;
         } // notifyDeath
         
         
         public void notifyNextLevel() {
        	 deadCops.clear();
        	 copDeathTimes.clear();
        	 message = "sprites/nextLevel.png";
             keyCounter = 0;
             ammoCount = 6; 
             atStart = false;
             waitingForKeyPress = true;
         } // notifyNextLevel
         
         
         public void notifyWin(){
           message = "sprites/winScreen.png";
           atStart = false;
           keyCounter = 0;
           currentLevel = 1;
           ammoCount = 6; 
           waitingForKeyPress = true;
         } // notifyWin
         
        /* Notification than a cop has been killed
         */
         public void notifyCopKilled(Entity cop) {
        	 copCount--;
        	 deadCops.add(cop);
        	 copDeathTimes.add(System.currentTimeMillis());
         } // notifyCopKilled
         
       //respawnCops
       public void respawnCops() { 
    	   for(int i = 0; i < deadCops.size(); i++) {
    		   if(System.currentTimeMillis() - copDeathTimes.get(i) > respawnInterval) {
    			   entities.add(deadCops.get(i));
    			   copCount++;
    			   
    			   lastCopPrint.remove(i); 
    			   deadCops.remove(i);
    			   copDeathTimes.remove(i);
    		   } // if
    	   } // for i
         } // tryToRespawnCops
         
        /* Attempt to fire.*/
        public void tryToFire() {
        	if (ammoCount > 0) {
	          // check that we've waited long enough to fire
	          if (System.currentTimeMillis() - lastFire < firingInterval){
	            return;
	          } // if
	          
	          	angle = Math.atan2(mX - (gunArm.getX() + 5), mY - (gunArm.getY() -15)); // calculates angle
	        	yVelocity = (300) * Math.cos(angle); // calculates y velocity based on angle
	        	xVelocity = (300) * Math.sin(angle); // calculates x velocity based on angle
	
	            // otherwise add a shot
	            lastFire = System.currentTimeMillis();
	            BulletEntity shot = new BulletEntity(this, "sprites/shot.png", 
	                              gunArm.getX() - 7, gunArm.getY() -15 , xVelocity, yVelocity, tileLocations);
	            ammoCount--; 
	            entities.add(shot); 
        	} // if 
        } // tryToFire
        
        public void tryToJump() {
        	//System.out.println(timeDYIs0 + " " + jumpRestrictor);
        	if(player.getVerticalMovement() != 0 || System.currentTimeMillis() - timeDYIs0 < jumpRestrictor) {//////////////////////////////////////////////////////
        		return;
        	} // if
        	
        	// otherwise, jump
        	player.setVerticalMovement(-666);
        	

        } // tryToJump

        //---------------------------------------------
        //bounceAway
        //when called the players direction is reversed
        //as well as they jump
        //---------------------------------------------
        public void bounceAway(long x) {
        	double reverse = 1;
        	notBouncingAway = false;
        	if(cop.dx < -1) {
        		reverse = -1;
        	}
        	player.setVerticalMovement(-600);
        	player.setHorizontalMovement(150 * reverse);
    		hitTime = x;
    		
        } // bounceAway
        
        public void updateSpeed() {
        	if(System.currentTimeMillis() - lastAccel >= accelInterval && currentSpeed < maxSpeed) {
        		currentSpeed += acceleration;
    			if(currentSpeed > maxSpeed) {
    				currentSpeed = maxSpeed;
    			} // if
        	} // if
		} // getSpeed
        
        //------------------------------------
        //backgroundDraw
        //draws image over whole screen
        //------------------------------------
        public void backgroundDraw (String imageURL, Graphics g) {
        	URL url = Game.class.getResource(imageURL); 
            image = Toolkit.getDefaultToolkit().getImage(url);
        	Frame frame = new Frame();
        	Graphics2D g2d = (Graphics2D)g;
        	g2d.drawImage(image, 0, 0, this);
        	frame.setSize(1000, 800);
        }
        
        //------------------------------------
        //copAnimate
        //animates the cops walking
        //------------------------------------
        public void copAnimate(Entity entity, int i) {
            if (entity instanceof CopEntity && System.currentTimeMillis() - lastCopPrint.get(i) > 80) {
	         	   if (entity.dx > 0) {
	         		   if (copAnimateCounterR % 2 == 0) {
	         			   entity.setSprite("sprites/Cop1.png"); 
	         		   } else {
	         			   entity.setSprite("sprites/Cop2.png");
	         		   } // if/else
	         		   lastCopPrint.add(System.currentTimeMillis());
	         		   copAnimateCounterR++;
	         	   	   } else if (entity.dx < 0) {
	         	   		   if (copAnimateCounterL % 2 == 0) {
	         	   			   entity.setSprite("sprites/Cop3.png"); 
	         	   		   } else {
	         	   			   entity.setSprite("sprites/Cop4.png");
	         	   		   } // if/else
	         	   	   lastCopPrint.add(System.currentTimeMillis());
	         	   		   copAnimateCounterL++;
	         	   	   } // else if 
	            } // if 
        } // copAnimate

        
        
	/*
	 * gameLoop
         * input: none
         * output: none
         * purpose: Main game loop. Runs throughout game play.
         *          Responsible for the following activities:
	 *           - calculates speed of the game loop to update moves
	 *           - moves the game entities
	 *           - draws the screen contents (entities, text, map)
	 *           - updates game events
	 *           - checks input
	 */
	public void gameLoop() {
          long lastLoopTime = System.currentTimeMillis();

          // keep loop running until game ends
          while (gameRunning) {
            
            // calc. time since last update, will be used to calculate
            // entities movement
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // get graphics context for the accelerated surface and make it black
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            backgroundDraw ("sprites/background.png",g);
           // g.setColor(Color.GRAY);
           // g.fillRect(0, 0, 1000, 800);
            
            // move each entity
            if (!waitingForKeyPress) {
              
              for (int i = 0; i < entities.size(); i++) {
                Entity entity = (Entity) entities.get(i);
                entity.move(delta);
              } // for i 
            } // if

            // draw all entities
            for (int i = 0; i < entities.size(); i++) {
               Entity entity = (Entity) entities.get(i);
               if (gunArm instanceof Entity) {
            	   Graphics2D g2 = (Graphics2D) g.create();
            	   rotatingAngle = (Math.atan2(mXConstant - (player.getX() + 10), mYConstant - (player.getY() + 26)) * -1);
            	   entity.rotate(g2, rotatingAngle, player.getX() + 12.5, player.getY() + 26);
            	   entity.draw(g2);
            	   g2.dispose(); 
               } else {
            	   entity.draw(g);
                   } // else
               
            } // for i
            
            // draw map
    		for (int i = 0; i < tileLocations.size(); i++) {
    			Sprite sprite = (SpriteStore.get()).getSprite("sprites/block.png");
    			sprite.draw(g, tileLocations.get(i)[0], tileLocations.get(i)[1]);
    		} // for
    		
    		//draws level x on each level where x is the number of current level
    		if(currentLevel < 5) {
	    		level = (SpriteStore.get()).getSprite("sprites/level" + currentLevel + ".png");
	    		
	    		level.draw(g, 200, 75);
    		}
    		
    		// draw hearts 
    		for (int i = 0; i < lives; i++) {
    			Sprite heart = (SpriteStore.get()).getSprite("sprites/health.png");
    			heart.draw(g, (i * 38) + 25, 30);
    		} // for
    		
    		// draw hearts 
    		for (int i = 0; i < ammoCount; i++) {
    			Sprite ammoCounter = (SpriteStore.get()).getSprite("sprites/ammoCounter.png");
    			ammoCounter.draw(g, (i * 22) + 840, 30);
    		} // for
    		
    		if (ammoCount == 0) {
    			Sprite ammoText = (SpriteStore.get()).getSprite("sprites/outOfAmmo.png");
    			ammoText.draw(g, 800, 25);
    		}
    		
            // brute force collisions, compare every entity
            // against every other entity.  If any collisions
            // are detected notify both entities that it has
            // occurred
    		for (int i = 0; i < entities.size(); i++) {
    			for (int j = i + 1; j < entities.size(); j++) {
    				Entity me = (Entity)entities.get(i);
    				Entity him = (Entity)entities.get(j);

    				if (me.collidesWith(him)) {
    					me.collidedWith(him);
    					him.collidedWith(me);
    				} // if
    			} // for j 
    		} // for i 

    		// remove dead entities
    		entities.removeAll(removeEntities);
    		removeEntities.clear();       
           
    		// run logic if required
    		for(int i = 0; i < entities.size(); i++) {
    			Entity entity = (Entity) entities.get(i);
    			if(entity.getLogicReq()) {
    				entity.doLogic();
    				entity.setLogicReq(false);
    			} // if
    		} // for
           
    		// if waiting for "any key press", draw message
    		if (waitingForKeyPress) {
    			Sprite title = (SpriteStore.get()).getSprite(message);
    			Sprite play = (SpriteStore.get()).getSprite("sprites/play.png");
    			Sprite instructions = (SpriteStore.get()).getSprite("sprites/instructions.png");
    			Sprite fullInstructions = (SpriteStore.get()).getSprite("sprites/fullInstructions.png");
    			Sprite fullInstructions2 = (SpriteStore.get()).getSprite("sprites/controlsRight.png");
    			Sprite back =  (SpriteStore.get()).getSprite("sprites/back.png");
    			lives = 3;
    			
    			g.setColor(Color.black);
    			g.fillRect(0, 0, 1000, 800); 
    			title.draw(g, 160, 200);
    			
    			
    			//if at start screen and not reading the instructions draw the commands for play and instructions
    			if(atStart && !readInstructions) {
    				play.draw(g, 250, 300);
    				instructions.draw(g, 250, 350);
    				
    			}//if
    			else if(readInstructions) {
    				fullInstructions.draw(g, 140, 300);
    				fullInstructions2.draw(g, 140, 590);
    				back.draw(g, 30, 30);
    			}
    		}  // if

            // clear graphics and flip buffer
            g.dispose();
            strategy.show();
            
            respawnCops();
            
            // player should not move without user input
        	if(System.currentTimeMillis() >= hitTime + 500) {
        		notBouncingAway = true;
        	} // if
        	
        	// if user is running
            if (shiftPressed) {
            	maxSpeed = 300;
            } else {
            	maxSpeed = 200;
            } // else
            
        	// update current speed if: should be reset to 0/ over maxSpeed
            currentSpeed = Math.abs(player.getHorizontalMovement());
            if (currentSpeed > maxSpeed) {
            	currentSpeed = maxSpeed;
            } // if
            
            // stop player sliding around if hit
            if(notBouncingAway) {
            	player.setHorizontalMovement(0);
            } // if
            
            if(player.getVerticalMovement() == 0 && timeDYIs0 == 0) {
            	timeDYIs0 = System.currentTimeMillis();
            } else if(player.getVerticalMovement() != 0) {
            	timeDYIs0 = System.currentTimeMillis();
            } // if

            
            //------------------------
            // CONTROLS
            //------------------------
            
            if (rotatingAngle > 0) {
     		   gunArm.setSprite("sprites/gunArm2.png");
     		   player.setSprite("sprites/player3.png");
     	   } else {
     		   gunArm.setSprite("sprites/gunArm.png");
     		   player.setSprite("sprites/player1.png");
     	   }
            
            // respond to user moving player left
            if ((leftPressed) && (!rightPressed)) {
            	
            	// animate player going left
            	if (animateCounterL % 2 == 0) { 
            		player.setSprite("sprites/player3.png"); 
            	} else {
            		player.setSprite("sprites/player4.png");  
            	} // else
            	animateCounterL++; 
            	
            	// change speed
            	updateSpeed();
            	player.setHorizontalMovement(-currentSpeed);
            	rightLastPressed = false;
            	
            	// respond to user moving player right
            } else if ((rightPressed) && (!leftPressed)) {
            	
            	// animate player going right
            	if (animateCounterR % 2 == 0) { 
            		player.setSprite("sprites/player1.png"); 
            	} else {
            		player.setSprite("sprites/player2.png");  
            	} // else
            	animateCounterR++;
            	
            	// change speed
            	updateSpeed();
            	player.setHorizontalMovement(currentSpeed);
            	rightLastPressed = true;
            } // else if 
            
            // respond to user trying to jump
            if (jumpPressed) {
            	tryToJump();
            } // else if

            // gravity
            if (System.currentTimeMillis() - lastGravity >= gravityInterval) {
            	player.setVerticalMovement(player.getVerticalMovement() + gravity);
            	lastGravity = System.currentTimeMillis();
            } // if
            
            // if mouse pressed, try to fire
            if (firePressed) {
                tryToFire();
            } // if
            
             
            // Cop Animation
            for (int i = 0; i < entities.size(); i++) { 
	            Entity entity = (Entity) entities.get(i);
	            copAnimate(entity, i);
            } // for 
            
            gunArm.setX(player.getX() + 10);
            gunArm.setY(player.getY() + 26);
            // pause
            try { Thread.sleep(6); } catch (Exception e) {}
          } // while

	} // gameLoop
		
		//---------------------------------------
		//instructions
		//opens the instruction menu when called.
		//---------------------------------------
		public void instructions(){
			message = "sprites/instructionsTitle.png";
			readInstructions = true;
			waitingForKeyPress = true;
		}//instructions
	
        /* startGame
         * input: none
         * output: none
         * purpose: start a fresh game, clear old data
         */
         public void startGame() { /////////////////////////////////////////////// START GAME ///////////////////////////////////////
            // clear out any existing entities and initalize a new set
            entities.clear();
            tileLocations.clear();
            
            initMap();
            
            /*
            print2DArray(tileMap);
            print2DArray(tileLocations);
            System.out.println("-----------------------");
            */
            
            initEntities();
            
            // blank out any keyboard settings that might exist
            leftPressed = false;
            rightPressed = false;
            firePressed = false;
            jumpPressed = false; 
            shiftPressed = false;
         } // startGame

        /* inner class MouseListenerExample
         * handles mouse input from the user
         */
         
    //listens to mouse commands
    private class MouseListenerExample extends Frame implements MouseListener, MouseMotionListener {

        MouseListenerExample() {
            addMouseListener(this);
        }

        public void mouseClicked(java.awt.event.MouseEvent ev) {} // mouse clicked 

        public void mouseEntered(MouseEvent e) {} //mouseEntered

        public void mouseExited(MouseEvent e) {} //mouseExited
        
        public void mouseMoved(MouseEvent e) {
        	mXConstant = e.getX();//mouse X coordinate
        	mYConstant = e.getY();//mouse Y coordinate
        }
        public void mouseDragged(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {
        	firePressed = true;
        	mX = e.getX();//mouse X coordinate
        	mY = e.getY();//mouse Y coordinate
        } //mousePressed

        public void mouseReleased(MouseEvent e) {
        	firePressed = false;
        } //mouseReleased
    }
    
    /* inner class KeyInputHandler
     * handles keyboard input from the user
     */
    
	private class KeyInputHandler extends KeyAdapter {
                 
                 private int pressCount = 1;  // the number of key presses since
                                              // waiting for 'any' key press

                /* The following methods are required
                 * for any class that extends the abstract
                 * class KeyAdapter.  They handle keyPressed,
                 * keyReleased and keyTyped events.
                 */
		public void keyPressed(KeyEvent e) {

                  // if waiting for keypress to start game, do nothing
                  if (waitingForKeyPress) {
                    return;
                  } // if
                  // respond to move left, right or fire
                  if (e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = true;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = true;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_W) {
                      jumpPressed = true;
                  } // if
                  
                  if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                	  shiftPressed = true;
                  } // if
                  
                  if (e.getKeyCode() == KeyEvent.VK_N) {
                	  if (currentLevel < 4) {
                		  currentLevel++;
                		  notifyNextLevel();
                	  } else {
                		  notifyWin(); 
                	  } // if/else
           
                  } // if
                  

		} // keyPressed

		public void keyReleased(KeyEvent e) {
                  // if waiting for keypress to start game, do nothing
                  if (waitingForKeyPress) {
                    return;
                  } // if
                  
                  // respond to move left, right or fire
                  if (e.getKeyCode() == KeyEvent.VK_A) {
                    leftPressed = false;
                  } // if

                  if (e.getKeyCode() == KeyEvent.VK_D) {
                    rightPressed = false;
                  } // if
      
                  if (e.getKeyCode() == KeyEvent.VK_W) {
                      jumpPressed = false;
                  } // if
                  
                  if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                	  shiftPressed = false;
                  }

		} // keyReleased

 	        public void keyTyped(KeyEvent e) {
 	        	
 	        	
                   // if waiting for key press to start game
 	           if (waitingForKeyPress) {
 	        	  //starts game when player presses 1
 	        	  if (e.getKeyChar() == 49 && !readInstructions) {
                      waitingForKeyPress = false;
                      startGame();
                      pressCount = 0;
 	        	  }//else if
 	        	  //shows instructions when player presses 2
 	        	  else if(e.getKeyChar() == 50 && !readInstructions){
 	        		  instructions();
 	        	  }//else if
 	        	 //if player is at instructions and press b they go back to menu
 	        	  else if((e.getKeyChar() == 66 || e.getKeyChar() == 98) && readInstructions) {
 	        		  message = "sprites/PrisonEscapeTitle.png";
 	        		  readInstructions = false;
 	        	  }//else if
 	        	  

                   } // if waitingForKeyPress

                   // if escape is pressed, end game
                   if (e.getKeyChar() == 27) {
                     System.exit(0);
                   } // if escape pressed

		} // keyTyped

	} // class KeyInputHandler


	/**
	 * Main Program
	 */
	public static void main(String [] args) {
        // instantiate this object
		new Game();
	} // main
	
	//---------
	// testing methods
	//---------
	public static void print2DArray(char [][] a) {
		for(int i = 0; i < a.length; i ++) {
			printArray(a[i]);
			System.out.print("\n");
		} // for
	} // print2DArray
	public static void printArray(char [] a) {
		for(int i = 0; i < a.length; i ++) {
			System.out.print(a[i]);
		} // for
	} // printArray
	
	public static void print2DArray(ArrayList <int[]> a) {
		for(int i = 0; i < a.size(); i ++) {
			printArray(a.get(i));
			System.out.print("\n");
		} // for
	} // print2DArray
	public static void printArray(int [] a) {
		for(int i = 0; i < a.length; i ++) {
			System.out.print(a[i] + " ");
		} // for
	} // printArray
	
} // Game
