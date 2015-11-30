package fr.youscer;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import fr.youscer.libs.ParticleEffect;


public class ArmorRotation implements Runnable{
	private int id = -1;
	private ArmorStand asProp;
	private ArmorStand asCore;
	private double xrot;
	private double yrot;
	private double zrot;
	private Vector vec = new Vector(0,0,0);
	private Location lastpos; 
	private int index = -Integer.MAX_VALUE;
	private Vector nearestPlayerDirection;
	Objective debug;
	
	public ArmorRotation (ArmorStand as, ArmorStand as2, double x, double y, double z){
		this.asProp = as;
		this.asCore = as2;
		xrot = x;
		yrot = y;
		zrot = z;
		lastpos = asCore.getLocation();
		debug = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.SIDEBAR);
	}
	
	public void run() {
		index++;
		calcVecTrajectory();
		asCore.setVelocity( asCore.getVelocity().add( new Vector(   vec.getX()*yrot,   vec.getY()*yrot,   vec.getZ()*yrot    ) ));
		
//		double vitesseXZ = Math.pow(lastpos.getX()-asCore.getLocation().getX(), 2) + Math.pow(lastpos.getZ()-asCore.getLocation().getZ(), 2);
//		Bukkit.broadcastMessage("§8Vitesse X/Z : §7" + vitesseXZ);
		calcCoreRotation();
		asProp.setHeadPose(asProp.getHeadPose().add(xrot, yrot, zrot));
		if(yrot<0.45){
			yrot += 0.005;
		}
		ParticleEffect.SMOKE_NORMAL.display(new Vector(0,-1.5,0), 0.1F, asCore.getLocation().add(0,0.25,0), 30);	
		if(index%2==0){
			asCore.getLocation().getWorld().playSound(asCore.getLocation(), Sound.LAVA_POP, (float) 0.1, (float) (Math.random()/2+1.5));
		}
		lastpos = asCore.getLocation();
	}

	
	public void calcCoreRotation(){
		Vector vec1 = nearestPlayerDirection.clone().normalize();
		double targetedYaw = (Math.atan2(vec1.getX(), -vec1.getZ()));
		double currentYaw = asCore.getHeadPose().getY();
		debug.getScore("CurrentYaw").setScore((int)(currentYaw*100));
		debug.getScore("TargetedYaw").setScore((int)(targetedYaw*100));
		debug.getScore("cy-ty").setScore((int)((currentYaw-targetedYaw)*100));
		while(currentYaw-targetedYaw > Math.PI){
			currentYaw -= Math.PI*2;
			Bukkit.broadcastMessage("yaw--");
		}
		while(currentYaw-targetedYaw < -Math.PI){
			currentYaw += Math.PI*2;
			Bukkit.broadcastMessage("yaw++");
		}
		double toadd = currentYaw < targetedYaw ? Math.abs(currentYaw-targetedYaw)*Math.PI/48 : -Math.abs(currentYaw-targetedYaw)*Math.PI/48;
		asCore.setHeadPose(new EulerAngle(0,currentYaw+toadd,0));
	}
	public void calcVecTrajectory() {
		Entity eproche = null;
		double distance = 3600;
		for(Entity e : asCore.getNearbyEntities(60, 60, 60)){
			double distanceTemp = e.getLocation().distanceSquared(asCore.getLocation());
			if( (e instanceof Player || (e instanceof ArmorStand && e.getUniqueId() != asProp.getUniqueId())) && distanceTemp < distance ){
				if(! (e instanceof ArmorStand && distanceTemp > 10)){
					distance = distanceTemp;
					eproche = e;
				}
			}
		}
		double speedmultiplier = 0.1;
		if(eproche != null){
			Location eloc = eproche.getLocation().add(0,1,0);
			if(eproche instanceof Player){
				Vector direction =  eloc.clone().subtract(asCore.getLocation().clone()).toVector();
				nearestPlayerDirection = direction.clone();
				direction.normalize().multiply(speedmultiplier);
				direction = eloc.distanceSquared(asCore.getLocation()) > 16 ? direction : direction.multiply(-1);
				if( eloc.getY() > asCore.getLocation().getY() ) {direction.setY(0.2);} else direction.setY(0.13); 
				vec = direction;
				
			}else
			if(eproche instanceof ArmorStand && eloc.distanceSquared(asCore.getLocation()) < 10){
				Vector direction =  eloc.clone().subtract(asCore.getLocation().clone()).toVector();
				direction.normalize().multiply(-speedmultiplier);
				if( eloc.getY() > asCore.getLocation().getY() ) {direction.setY(0.2);} else direction.setY(0.13); 
				vec = direction;
			}
			
		}else{
			vec = new Vector(0,0,0);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArmorStand getAsProp() {
		return asProp;
	}

	public void setAsProp(ArmorStand asProp) {
		this.asProp = asProp;
	}

	public ArmorStand getAsCore() {
		return asCore;
	}

	public void setAsCore(ArmorStand asCore) {
		this.asCore = asCore;
	}

	public double getXrot() {
		return xrot;
	}

	public void setXrot(double xrot) {
		this.xrot = xrot;
	}

	public double getYrot() {
		return yrot;
	}

	public void setYrot(double yrot) {
		this.yrot = yrot;
	}

	public double getZrot() {
		return zrot;
	}

	public void setZrot(double zrot) {
		this.zrot = zrot;
	}

	public Vector getVec() {
		return vec;
	}

	public void setVec(Vector vec) {
		this.vec = vec;
	}

}
