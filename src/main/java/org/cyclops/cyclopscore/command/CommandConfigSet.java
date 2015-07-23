package org.cyclops.cyclopscore.command;

import com.google.common.collect.Maps;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.config.Configuration;
import org.cyclops.cyclopscore.config.ConfigProperty;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;
import org.cyclops.cyclopscore.init.ModBase;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Command that can define {@link ConfigProperty}.
 * @author rubensworks
 *
 */
public class CommandConfigSet extends CommandConfig {
    
    private ConfigProperty config;
    private String name;

    /**
     * Make a new command for a {@link ConfigProperty}.
     * @param mod The mod.
     * @param name The identifier.
     * @param config The config to be defined.
     */
    public CommandConfigSet(ModBase mod, String name, ConfigProperty config) {
        super(mod);
        this.name = name;
        this.config = config;
    }

    @Override
    public String getFullCommand() {
        return super.getFullCommand() + " " + this.name;
    }

    @Override
    protected List<String> getAliases() {
        List<String> list = new LinkedList<String>();
        list.add(name);
        return list;
    }

    @Override
    protected Map<String, ICommand> getSubcommands() {
        return Maps.newHashMap();
    }

    @Override
    public void processCommandHelp(ICommandSender icommandsender, String[] astring) throws CommandException {
        printLineToChat(icommandsender, config.getComment());
        printLineToChat(icommandsender, config.getName() + " = " + config.getValue());
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] astring) throws CommandException {
        if (astring.length == 0) {
            processCommandHelp(icommandsender, astring);
        } else if (astring.length > 1) {
            icommandsender.addChatMessage(new ChatComponentText(L10NHelpers.localize("chat.cyclopscore.command.onlyOneValue")));
        } else {
            Object newValue = Helpers.tryParse(astring[0], config.getValue());
            if(newValue != null) {
            	Configuration configuration = getMod().getConfigHandler().getConfig();
            	configuration.load();
                config.getCallback().run(newValue);
                config.save(configuration, true);
                configuration.save();
                icommandsender.addChatMessage(new ChatComponentText(L10NHelpers.localize("chat.cyclopscore.command.updatedValue",
                        name, newValue.toString())));
            } else {
                icommandsender.addChatMessage(new ChatComponentText(L10NHelpers.localize("chat.cyclopscore.command.invalidNewValue")));
            }
        }
    }

}