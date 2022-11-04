import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.math.ColorRGBA;

public class Arcade extends SimpleApplication implements PhysicsCollisionListener, ActionListener {
	
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
	private TerrainQuad terrain;
	private Material mat_terrain;
	private RigidBodyControl ball_phy;
	private Geometry ball_geo;
	private Mesh sphere;
	private Material ball_mat;
	private Random pos;
	private Random ranVel;
	private Random ranLoc = new Random();
	private static Arcade app;
	Node enemies [] ;
	private CapsuleCollisionShape golemShape[];
	private CharacterControl golemControl[];
	private BulletAppState bullet;
	private int points = 0;
	private int countBullet = 0;
	private float count;
	BillboardControl board = new BillboardControl();
	private float healthb1;
	private float healthb2;
	private float healthb3;
	private float healthb4;
	Quad h1 = new Quad(3f,0.2f);
    Quad h2 = new Quad(3f,0.2f);
    Quad h3 = new Quad(3f,0.2f);
    Quad h4 = new Quad(3f,0.2f);
    Geometry healthBarOne = new Geometry("Health Ball 1", h1 );
	Geometry healthBarTwo = new Geometry("Health Ball 2", h2); 
	Geometry healthBarThree = new Geometry("Health Ball 3", h3);
	Geometry healthBarFour = new Geometry("Health Ball 4", h4);
	private float takeheal = 3;
	Node enemyarea = new Node ();
	private BitmapText ch ;
    private BitmapText score;
    private BitmapText bulletcount;
	private float moveNow;
	public static java.io.File highscore = new java.io.File("Highscore.txt");
    public static java.io.PrintWriter output;
    String highS;
    int high = 0;
	private BitmapText Hscore;
	private BitmapText Instructions;
	
/*The main method that starts the applications and creates or finds textfile for highest score*/	
	public static void main(String[] args)
	{
		try 
		{
			Scanner file = new Scanner(highscore);
			app = new Arcade(file);
			app.start();
		} 
		catch (FileNotFoundException e) 
		{
			try
			{
				output = new java.io.PrintWriter(highscore);
				output.print("0");
				Scanner file = new Scanner(highscore);
				app = new Arcade(file);
				app.start();
				
			}catch(FileNotFoundException f)
			{
				System.out.println("No file");
			}
		}
	}
/*Constructor that takes in a scanner parameter to get the highest score*/
	public Arcade(Scanner Document)
	{
		if(Document.hasNext())
		{
			highS = Document.next();
			high = Integer.parseInt(highS);
		}
	}
	
/*Initializes the skeleton of the application*/
	public void simpleInitApp() 
	{
		bulletAppState = new BulletAppState();
		
	    stateManager.attach(bulletAppState);
	    
	    bulletAppState.getPhysicsSpace().addCollisionListener(this);
		flyCam.setMoveSpeed(100);
		enemies = new Node[4];
		golemShape = new CapsuleCollisionShape[4];
		golemControl = new CharacterControl[4];
		ch = new BitmapText(guiFont, false);
		score = new BitmapText(guiFont, false);
		Hscore = new BitmapText(guiFont, false);
		bulletcount = new BitmapText(guiFont, false);
		Instructions = new BitmapText(guiFont, false);
		
		registerInput();
		createTerrain();
		createPlayer();
		initCrossHairs();
		
		
		for(int i=0;i<enemies.length;i++)
		{
			enemy(i);
		}
		
		rootNode.attachChild(enemyarea);
		createHealth();
	}
	
	

	public void simpleUpdate(float tpf)
	{
		count+=tpf;
		moveNow+=tpf;
		
		walk();
		if(moveNow >= 3f)
		{
			moveGolemsRandom();
			moveNow = 0f;
		}
		
		
		score.setText("Score: " + points);
		bulletcount.setText("Shots Fired: " + countBullet);
		
		if(count >= 100f)
		{
			guiNode.detachAllChildren();
			bulletAppState.setEnabled(false);
			BitmapText finalscore = new BitmapText(guiFont, false);
			if(points>high)
				{
					finalscore.setText("Final Score:\t"+ points +"\n\n\nNew High Score ");
					finalscore.setLocalTranslation(settings.getWidth() / 2 - finalscore.getLineWidth()/2, settings.getHeight() / 2 + finalscore.getLineHeight()/2, 0);
					guiNode.attachChild(finalscore);
					try
					{
						output = new java.io.PrintWriter(highscore);
						output.print(points);
						output.close();
					}
					catch(FileNotFoundException p)
					{
						System.out.println("File Not Found");
					}
				}
			else
				{
					finalscore.setText("Final Score:\t"+ points);
					finalscore.setLocalTranslation(settings.getWidth() / 2 - finalscore.getLineWidth()/2, settings.getHeight() / 2 + finalscore.getLineHeight()/2, 0);
					guiNode.attachChild(finalscore);
				}
		}
		
	}

	
/*Checks which way the player is walking and moves the camera in the direction the player is walking*/	
	public void walk()
	{
		Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
	    Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
	    walkDirection.set(0, 0, 0);
	    if (left)  { walkDirection.addLocal(camLeft); }
	    if (right) { walkDirection.addLocal(camLeft.negate()); }
	    if (up)    { walkDirection.addLocal(camDir); }
	    if (down)  { walkDirection.addLocal(camDir.negate()); }
	    player.setWalkDirection(walkDirection);
	    cam.setLocation(player.getPhysicsLocation());
	}
/*Method that creates new key mapping strokes for the controls to be used in the game*/	
	 private void registerInput() 
	 {
		 	inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A));
		    inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
		    inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
		    inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
		    inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
		    inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_J));
		    inputManager.addListener(this, "Left");
		    inputManager.addListener(this, "Right");
		    inputManager.addListener(this, "Up");
		    inputManager.addListener(this, "Down");
		    inputManager.addListener(this, "Jump");
		    inputManager.addListener(this, "Shoot");
	  }

/*Performs the action when the key stoke is pushed*/	
	public void onAction(String whatMove, boolean value, float tpf) 
	{
		
		 if (whatMove.equals("Left")) 
			 {
			      if (value) { left = true; } else { left = false; }
			 } 
		 else if (whatMove.equals("Right")) 
			 {
			      if (value) { right = true; } else { right = false; }
			 }
		 else if (whatMove.equals("Up")) 
			 {
			      if (value) { up = true; } else { up = false; }
			 } 
		 else if (whatMove.equals("Down")) 
			 {
			      if (value) { down = true; } else { down = false; }
			 } 
		 else if (whatMove.equals("Jump"))
			 {
			    	player.jump();
			    	
			 }
		 
		 if(whatMove.equals("Shoot"))
			{
				Shoot();
			}
		 
	}
/*Method that creates the terrain for the level*/	
	public void createTerrain()
	{
		
	    mat_terrain = new Material(assetManager,"Common/MatDefs/Terrain/Terrain.j3md");
	 
	    
	    mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));
	 
	    
	    Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
	    grass.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("Tex1", grass);
	    mat_terrain.setFloat("Tex1Scale", 64f);
	 
	    
	    Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
	    dirt.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("Tex2", dirt);
	    mat_terrain.setFloat("Tex2Scale", 32f);
	 
	    
	    Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
	    rock.setWrap(WrapMode.Repeat);
	    mat_terrain.setTexture("Tex3", rock);
	    mat_terrain.setFloat("Tex3Scale", 128f);
	 
	    
	    AbstractHeightMap heightmap = null;
	    Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
	    heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
	    heightmap.load();
	    terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
	 
	    
	    terrain.setMaterial(mat_terrain);
	    terrain.setLocalTranslation(0, -100, 0);
	    terrain.setLocalScale(2f, 1f, 2f);
	    rootNode.attachChild(terrain);
	 
	    
	    List<Camera> cameras = new ArrayList<Camera>();
	    cameras.add(getCamera());
	    TerrainLodControl control = new TerrainLodControl(terrain, cameras);
	    terrain.addControl(control);
	 
	    
	    terrain.addControl(new RigidBodyControl(0));
		
		bulletAppState.getPhysicsSpace().add(terrain);
		
	}
/*Method that creates the player and adds physics to the player*/	
	private void createPlayer() 
	{
		
		CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
	    player = new CharacterControl(capsuleShape, 0.05f);
	    player.setJumpSpeed(20);
	    player.setFallSpeed(30);
	    player.setGravity(30);
	    player.setPhysicsLocation(new Vector3f(10, 125, 10));
	    player.setViewDirection(cam.getLocation());
	    bulletAppState.getPhysicsSpace().add(player);
	}
/*Method that allows the player to shoot spheres*/	
	private void Shoot()
	{
		
		sphere = new Sphere(22,22,0.4f,true,false);
	    ball_geo = new Geometry("ball", sphere);
	    ball_mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
	    ball_geo.setMaterial(ball_mat);
	    
	    rootNode.attachChild(ball_geo);
	    
	    
	    
	    Vector3f camLocation = app.getCamera().getLocation();
	    ball_geo.setLocalTranslation(app.getCamera().getDirection().scaleAdd(3,camLocation));
	    
	    ball_phy = new RigidBodyControl(1f);
	    
	    ball_geo.addControl(ball_phy);
	    bulletAppState.getPhysicsSpace().add(ball_phy);
	
	    int velocityR = randomV();
	    ball_phy.setLinearVelocity(cam.getDirection().mult(velocityR));
	    
	    ball_phy.setGravity(new Vector3f(0, -5, 0));
	    
	    countBullet++;
	}

	public int randomV()
	{
		int velocity = (int) (Math.random()*20) + 60;
		return velocity;
	}
/*method  that creates the HUD*/
	protected void initCrossHairs() 
	{
		    setDisplayStatView(false);
		    setDisplayFps(false);
		    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		    
		    score.setSize(guiFont.getCharSet().getRenderedSize()*1);
		    bulletcount.setSize(guiFont.getCharSet().getRenderedSize()*1);
		    Hscore.setSize(guiFont.getCharSet().getRenderedSize()*1);
		    ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		    Instructions.setSize(guiFont.getCharSet().getRenderedSize()*1);
		    
		    score.setText("Score: " + points);
		    bulletcount.setText("Shots Fired: " + countBullet);
		    Hscore.setText("Highscore: " + high);
		    ch.setText("+");
		    Instructions.setText("Use the Arrow Keys or WASD and Mouse to move."+"\n"+"Use the Space Bar to shoot."+"\n"+"Use J to jump.");

		    Instructions.move(settings.getWidth()/1.9f, settings.getHeight()/8, 1f);	
		    score.move(0f, settings.getHeight()/9, 1f);
		    bulletcount.move(0f, settings.getHeight()/12, 1f);
		    Hscore.move(0f, settings.getHeight()/20, 1f);
		    ch.setLocalTranslation(settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
		    
		    
		    guiNode.attachChild(score);
		    guiNode.attachChild(ch);
		    guiNode.attachChild(bulletcount);
		    guiNode.attachChild(Hscore);
		    guiNode.attachChild(Instructions);
		    
		    
			
			
		    
	 }
/*Method that creates the enemies with physics*/	
	private Node enemy(int i)
	{
			    Node golem = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
			    golem.scale(0.5f);
			    golem.setLocalTranslation(randomP(),5,randomP());
			    DirectionalLight sun = new DirectionalLight();
			    sun.setDirection(cam.getDirection());
			    sun.setColor(ColorRGBA.White);
			   
			    
				
				enemies[i] = golem;
				enemies[i].setName("Golem"+i);
				enemies[i].setUserData("Golem"+i, 100f);
				golemShape[i] = new CapsuleCollisionShape(3f,4f);
				golemControl[i] = new CharacterControl(golemShape[i],1f);
				golemControl[i].setFallSpeed(30);
			    golemControl[i].setGravity(30);
				enemies[i].addControl(golemControl[i]);
				enemies[i].addLight(sun);
				enemyarea.attachChild(enemies[i]);
				bulletAppState.getPhysicsSpace().add(enemies[i]);
			    return enemies[i];	  
	}
	

	
	private int randomP()
	{
		int pos = ranLoc.nextInt(60);
		return pos*-5;
	}
	
/*Method that checks for collosions and takes away enemy health*/
	public void collision(PhysicsCollisionEvent pe) 
	{
		healthb1 = (Float)enemies[0].getUserData("Golem0");
		healthb2 = (Float)enemies[1].getUserData("Golem1");
		healthb3 = (Float)enemies[2].getUserData("Golem2");
		healthb4 = (Float)enemies[3].getUserData("Golem3");
		
		if(pe.getObjectA().equals(golemControl[0]) && pe.getObjectB().equals(ball_phy))
		{
			enemies[0].setUserData("Golem0",healthb1 - takeheal );
			h1.updateGeometry(healthb1/100*4, .2f);
			
		}
		if(pe.getObjectA().equals(golemControl[1]) && pe.getObjectB().equals(ball_phy))
		{
			
			enemies[1].setUserData("Golem1",healthb2 - takeheal);
			h2.updateGeometry(healthb2/100*4, .2f);
		}
		if(pe.getObjectA().equals(golemControl[2]) && pe.getObjectB().equals(ball_phy))
		{
			
			enemies[2].setUserData("Golem2",healthb3 - takeheal);
			h3.updateGeometry(healthb3/100*4, .2f);
		}
		if(pe.getObjectA().equals(golemControl[3]) && pe.getObjectB().equals(ball_phy) )
		{
			
			enemies[3].setUserData("Golem3",healthb4 - takeheal);
			h4.updateGeometry(healthb4/100*4, .2f);
		}
		
		if(healthb1<=0)
		{
			points+=5;
			golemControl[0].warp(new Vector3f(randomP(),10,randomP()));
			
			enemies[0].setUserData("Golem0", 100f);
			
			
		}
		if(healthb2<=0)
		{
			points+=5;
			golemControl[1].warp(new Vector3f(randomP(),10,randomP()));;
			enemies[1].setUserData("Golem1", 100f);
			
		}
		if(healthb3<=0)
		{
			points+=5;
			golemControl[2].warp(new Vector3f(randomP(),10,randomP()));
			enemies[2].setUserData("Golem2", 100f);
			
		}
		if(healthb4<=0)
		{
			points+=5;
			golemControl[3].warp(new Vector3f(randomP(),10,randomP()));
			enemies[3].setUserData("Golem3", 100f);	
		}
		
	}
/*Creates health bars for the enemy*/
	public void createHealth()
	{
		Material mhealBar = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
		healthBarOne.setMaterial(mhealBar);
		healthBarTwo.setMaterial(mhealBar);
		healthBarThree.setMaterial(mhealBar);
		healthBarFour.setMaterial(mhealBar);
		
		healthBarOne.move(new Vector3f(0,enemies[0].getLocalTranslation().y+10f,0));
		healthBarTwo.move(new Vector3f(0,enemies[1].getLocalTranslation().y+10f,0));
		healthBarThree.move(new Vector3f(0,enemies[2].getLocalTranslation().y+10f,0));
		healthBarFour.move(new Vector3f(0,enemies[3].getLocalTranslation().y+10f,0));
		
		mhealBar.setColor("Color", ColorRGBA.Green);
		enemies[0].attachChild(healthBarOne);
		enemies[1].attachChild(healthBarTwo);
		enemies[2].attachChild(healthBarThree);
		enemies[3].attachChild(healthBarFour);
		enemyarea.addControl(board);
	}
/*Makes enemy jump up and down*/	
	public void moveGolemsRandom()
	{
		for(int m = 0; m<enemies.length;m++)
		{
			golemControl[m].jump();
			golemControl[m].setGravity(50);
			golemControl[m].setFallSpeed(50);
		}
	}
	
}
