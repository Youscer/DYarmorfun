package fr.youscer;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;


public class funfunfun extends JavaPlugin{
	Logger log = Logger.getLogger("Minecraft");
	private LinkedList<ArmorRotation> ars = new LinkedList<ArmorRotation>();
	private LinkedList<TurretRotation> trs = new LinkedList<TurretRotation>();
	
	public void onEnable()
	{
		log.info("[plugin] : Enabled");
	}
	
	public void onDisable()
	{
		log.info("[plugin] : Disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player p = (Player)sender;
			if (label.equalsIgnoreCase("dyfun"))
			{
				if (args.length == 0){
					p.sendMessage("project plugin");
				}else{
					if ( args[0].equalsIgnoreCase("test1")){
						spawnHelico(p.getLocation().add(Math.random(), Math.random(), Math.random()));
					}
					if ( args[0].equalsIgnoreCase("test2")){
						Vector d = p.getLocation().getDirection().multiply(2.5);
						spawnTurret(p.getLocation().add(d.getX()+Math.random(), Math.random(), d.getZ()+Math.random()));
					}
					if (args.length > 4 && args[0].equalsIgnoreCase("rot")){
						ars.get(Integer.parseInt(args[1])).getAsCore().setHeadPose(new EulerAngle(Double.parseDouble(args[2]),Double.parseDouble(args[3]),Double.parseDouble(args[4])));
					}
					if (args[0].equalsIgnoreCase("setp") ){
						for(TurretRotation tr : trs)
							tr.setYrot(Double.parseDouble(args[1]));
					}
				}
			}
		}
		return false;
	}
	
	public void spawnTurret(Location loc){
		double yaw = loc.getYaw()/360*Math.PI*2 + Math.PI;
		loc.setYaw(0);
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.setBasePlate(false);
		as.setGravity(false);
		as.setVisible(false);
		as.setHeadPose(new EulerAngle(0, yaw, 0));
		as.setHelmet(new ItemStack(Material.CARPET, 1, (short) 11));
//		as.setChestplate(new ItemStack(Material.CARPET, 1, (short) 15));
		
		ArmorStand as2 = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as2.setBasePlate(false);
		as2.setGravity(true);
		as2.setVisible(false);
		as2.setHeadPose(new EulerAngle(0, yaw, 0));
		as2.setHelmet(new ItemStack(Material.CARPET, 1, (short) 12));
//		as2.setChestplate(new ItemStack(Material.CARPET, 1, (short) 15));
		
		as2.setPassenger(as);
		TurretRotation tr = new TurretRotation(this, as, as2, 0, 0, 0);
		tr.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, tr, 0L, 1L));
		
		trs.add(tr);
	}
	
	public void spawnHelico(Location loc){
		boolean small = false; // Math.random() > .5 ? true : false;
		
		loc.setYaw(180);
		ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as.setBasePlate(false);
		as.setGravity(false);
		as.setVisible(false);
		as.setHeadPose(new EulerAngle(0, 0, 0));
		as.setHelmet(new ItemStack(Material.CARPET, 1, (short) 14));
//		as.setChestplate(new ItemStack(Material.CARPET, 1, (short) 15));
		as.setSmall(small);
		
		ArmorStand as2 = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		as2.setBasePlate(false);
		as2.setGravity(true);
		as2.setVisible(false);
		as2.setHeadPose(new EulerAngle(0, 0, 0));
		as2.setHelmet(new ItemStack(Material.CARPET, 1, (short) 13));
//		as2.setChestplate(new ItemStack(Material.CARPET, 1, (short) 15));
		as2.setSmall(small);
		
		as2.setPassenger(as);
		ArmorRotation ar = new ArmorRotation(as, as2, 0, 0, 0);
		ar.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ar, 0L, 1L));
		
		ars.add(ar);
	}
}
