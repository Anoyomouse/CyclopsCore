package org.cyclops.cyclopscore.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.cyclops.cyclopscore.CyclopsCore;
import org.cyclops.cyclopscore.helper.LocationHelpers;
import org.cyclops.cyclopscore.network.packet.RingOfFirePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Event hook for showing the ring of fire.
 * @author rubensworks
 *
 */
public class PlayerRingOfFire {

    // List of players that have a ring of fire
    private static final List<UUID> ALLOW_RING = new ArrayList<UUID>();
    static {
        ALLOW_RING.add(UUID.fromString("068d4de0-3a75-4c6a-9f01-8c37e16a394c")); // kroeserr
        ALLOW_RING.add(UUID.fromString("e1dc75c6-dcf9-4e0c-8fbf-9c6e5e44527c")); // _EeB_
        ALLOW_RING.add(UUID.fromString("3e13f558-fb72-4949-a842-07879924bc49")); // JonaBrackenwood
        ALLOW_RING.add(UUID.fromString("777e7aa3-9373-4511-8d75-f99d23ebe252")); // Davivs69
        ALLOW_RING.add(UUID.fromString("94b8bfe7-9102-405c-ab80-2c4468e918f9")); // JokerReaper
    }

    /**
     * When a player loggedin event is received.
     * @param event The received event.
     */
    @SubscribeEvent
    public void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		spawnRing(event.player);
    }
	
	/**
     * When a player respawn event is received.
     * @param event The received event.
     */
    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        spawnRing(event.player);
    }
    
    private void spawnRing(EntityPlayer player) {
        if(!player.worldObj.isRemote && player.getGameProfile() != null
    			&& ALLOW_RING.contains(player.getGameProfile().getId())) {
    		CyclopsCore._instance.getPacketHandler().sendToAllAround(new RingOfFirePacket(player),
                    LocationHelpers.createTargetPointFromLocation(player.worldObj, player.getPosition(), 50));
    	}
    }
    
}
