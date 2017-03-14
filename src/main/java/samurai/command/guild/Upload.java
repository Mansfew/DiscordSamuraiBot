package samurai.command.guild;

import samurai.command.Command;
import samurai.command.annotations.Key;
import samurai.entities.base.FixedMessage;
import samurai.entities.base.SamuraiMessage;
import samurai.entities.dynamic.ConflictMerge;
import samurai.data.SamuraiStore;

/**
 * @author TonTL
 * @version 4.0
 * @since 2/20/2017
 */
@Key("upload")
public class Upload extends Command {
    @Override
    protected SamuraiMessage buildMessage() {
        if (attaches.size() != 1 || !attaches.get(0).getFileName().endsWith(".db")) {
            return FixedMessage.build("❌ No valid attachment found.");
        } else if (attaches.get(0).getFileName().equalsIgnoreCase("scores.db")) {
            return new ConflictMerge(SamuraiStore.readScores(SamuraiStore.downloadFile(attaches.get(0))), guild.getScoreMap(), guild.getUser(Long.parseLong(author.getUser().getId())));
        }
        return null;
    }
}