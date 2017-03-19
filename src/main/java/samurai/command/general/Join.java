package samurai.command.general;

import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.messages.base.FixedMessage;
import samurai.messages.base.SamuraiMessage;

/**
 * @author TonTL
 * @version 4.2
 */
@Key("join")
public class Join extends Command {

    @Override
    public SamuraiMessage execute(CommandContext context) {
        return FixedMessage.build("https://discord.gg/yAMdGU9");
    }
}
