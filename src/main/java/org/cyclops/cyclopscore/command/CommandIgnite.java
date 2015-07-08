package org.cyclops.cyclopscore.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;

import java.util.LinkedList;
import java.util.List;

/**
 * Command for igniting players by name.
 * @author rubensworks
 *
 */
public class CommandIgnite extends CommandMod {
    
    public static final String NAME = "ignite";

    public CommandIgnite(ModBase mod) {
        super(mod);
    }

    @Override
    protected List<String> getAliases() {
        List<String> list = new LinkedList<String>();
        list.add(NAME);
        return list;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] parts, BlockPos blockPos) {
        return parts.length >= 1 ?
                CommandBase.getListOfStringsMatchingLastWord(parts, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] parts) {
        if(parts.length >= 1 && parts[0].length() > 0) {
            MinecraftServer minecraftserver = MinecraftServer.getServer();
            GameProfile gameprofile = minecraftserver.getPlayerProfileCache().getGameProfileForUsername(parts[0]);

            if (gameprofile == null) {
                sender.addChatMessage(new ChatComponentText(L10NHelpers.localize("chat.cyclopscore.command.invalidPlayer", parts[0])));
            } else {
                EntityPlayerMP player = minecraftserver.getConfigurationManager().getPlayerByUsername(parts[0]);
                int duration = 2;
                if(parts.length > 1) {
                    try {
                        duration = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        // Invalid duration amount, ignore.
                    }
                }
                player.setFire(duration);
                sender.addChatMessage(new ChatComponentText(L10NHelpers.localize("chat.cyclopscore.command.ignitedPlayer", parts[0], duration)));
            }
        }
    }
}
