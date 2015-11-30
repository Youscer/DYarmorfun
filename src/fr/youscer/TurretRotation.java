package fr.youscer;

import org.bukkit.Bukkit;
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

public class TurretRotation implements Runnable{
	private funfunfun plugin;
	private int idshowup = -1;
	private int id = -1;
	private ArmorStand asProp; // Missile
	private ArmorStand asCore; // Base
	private double xrot;
	private double yrot;
	private double zrot;
	private Vector vec = new Vector(0,0,0);
	private Location lastpos; 
	private long index = 0;
	private float indexShowup = 0; 
	private Vector nearestPlayerDirection;
	private String nearestPlayer;
	private boolean following = false;
	private boolean active = false;
	Objective debug;
	
	public TurretRotation (funfunfun plugin, ArmorStand as, ArmorStand as2, double x, double y, double z){
		this.plugin = plugin;
		this.asProp = as2;
		this.asCore = as;
		xrot = x;
		yrot = y;
		zrot = z;
		lastpos = asCore.getLocation();
		debug = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.SIDEBAR);
		showUp();
	}
	
	private void showUp() {
		final double yawOrigine = asCore.getHeadPose().getY();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){public void run(){
			idshowup = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){public void run(){
				Bukkit.broadcastMessage("§7Index : §6"+indexShowup+" §8| §7 active : §6"+active);
				if(indexShowup<Math.PI*2){
					asCore.setHeadPose(new EulerAngle(0, yawOrigine+indexShowup, 0));
					asProp.setHeadPose(new EulerAngle(0, yawOrigine+indexShowup, 0));
				}else{
					asProp.setHeadPose(new EulerAngle(indexShowup, yawOrigine, 0));
				}
				indexShowup += Math.PI/12;
				if(indexShowup>Math.PI*4){ active = true; Bukkit.getScheduler().cancelTask(idshowup);}
			}}, 0L, 1L);
		}}, 14L);

	}

	public void run() {
		if(active){
			if(index>0)index=Long.MIN_VALUE;index++;
			calcVecTrajectory();
//			asCore.setVelocity( asCore.getVelocity().add( new Vector(   vec.getX()*yrot,   vec.getY()*yrot,   vec.getZ()*yrot    ) ));
			
//			double vitesseXZ = Math.pow(lastpos.getX()-asCore.getLocation().getX(), 2) + Math.pow(lastpos.getZ()-asCore.getLocation().getZ(), 2);
//			Bukkit.broadcastMessage("§8Vitesse X/Z : §7" + vitesseXZ);
			calcCoreRotation();
//			asProp.setHeadPose(asProp.getHeadPose().add(xrot, yrot, zrot));
			ParticleEffect.SMOKE_NORMAL.display(new Vector(0,-1.5,0), 0.1F, asCore.getLocation().add(0,0.25,0), 30);
	/*		if(following){
				if(index%2==0){
					asCore.getLocation().getWorld().playSound(asCore.getLocation(), Sound.LAVA_POP, (float) 0.1, (float) (Math.random()/2+1.5));
				}
			}*/
			lastpos = asCore.getLocation();
		}
	}
	
	public void calcCoreRotation(){
		Vector vec1 = nearestPlayerDirection.clone().normalize();
		double targetedYaw = (Math.atan2(vec1.getX(), -vec1.getZ()));
		double currentYaw = asCore.getHeadPose().getY()-Math.PI;
		double targetedPitch = yrot; 
		double currentPitch = asProp.getHeadPose().getX();
		debug.getScore("CurrentPitch").setScore((int)(currentPitch*100));
		debug.getScore("TargetedPitch").setScore((int)(targetedPitch*100));
		debug.getScore("cp-tp").setScore((int)((currentPitch-targetedPitch)*100));
		debug.getScore("CurrentYaw").setScore((int)(currentYaw*100));
		debug.getScore("TargetedYaw").setScore((int)(targetedYaw*100));
		debug.getScore("cy-ty").setScore((int)((currentYaw-targetedYaw)*100));
		while(currentYaw-targetedYaw > Math.PI){currentYaw -= Math.PI*2;}
		while(currentYaw-targetedYaw < -Math.PI){currentYaw += Math.PI*2;}
		while(currentPitch-targetedPitch > Math.PI){currentPitch -= Math.PI*2;}
		while(currentPitch-targetedPitch < -Math.PI){currentPitch += Math.PI*2;}
		double toaddYaw = currentYaw < targetedYaw ? Math.abs(currentYaw-targetedYaw)*Math.PI/24 : -Math.abs(currentYaw-targetedYaw)*Math.PI/24;
		double toaddPitch = currentPitch < targetedPitch ? Math.abs(currentPitch-targetedPitch)*Math.PI/24 : -Math.abs(currentPitch-targetedPitch)*Math.PI/24;
		asCore.setHeadPose(new EulerAngle(0,currentYaw+toaddYaw+Math.PI,0));
		asProp.setHeadPose(new EulerAngle(currentPitch+toaddPitch,currentYaw+toaddYaw-Math.PI,asProp.getHeadPose().getZ()));
	}
	public void calcVecTrajectory() {
		Entity eproche = null;
		double distance = 3600;
		following = false;
		for(Entity e : asCore.getNearbyEntities(15, 15, 15)){
			double distanceTemp = e.getLocation().distanceSquared(asCore.getLocation());
			if( (e instanceof Player) && distanceTemp < distance ){
				nearestPlayer = e.getName();
				distance = distanceTemp;
				eproche = e;
				following = true;
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
	public Location getLastpos() {
		return lastpos;
	}
	public void setLastpos(Location lastpos) {
		this.lastpos = lastpos;
	}
	public long getIndex() {
		return index;
	}
	public void setIndex(long index) {
		this.index = index;
	}
	public Vector getNearestPlayerDirection() {
		return nearestPlayerDirection;
	}
	public void setNearestPlayerDirection(Vector nearestPlayerDirection) {
		this.nearestPlayerDirection = nearestPlayerDirection;
	}
	public Objective getDebug() {
		return debug;
	}
	public void setDebug(Objective debug) {
		this.debug = debug;
	}

}
