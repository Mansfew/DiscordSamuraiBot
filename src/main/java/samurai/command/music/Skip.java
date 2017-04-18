package samurai.command.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import samurai.audio.GuildAudioManager;
import samurai.audio.SamuraiAudioManager;
import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.messages.base.FixedMessage;
import samurai.messages.base.SamuraiMessage;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author TonTL
 * @version 4/11/2017
 */
@Key("skip")
public class Skip extends Command {
    @Override
    protected SamuraiMessage execute(CommandContext context) {
        final Optional<GuildAudioManager> managerOptional = SamuraiAudioManager.retrieveManager(context.getGuildId());
        if (managerOptional.isPresent()) {
            final GuildAudioManager audioManager = managerOptional.get();
            final EmbedBuilder eb = new EmbedBuilder().appendDescription("Skipped: ");
            final List<AudioTrack> queue = audioManager.scheduler.getQueue();
            if (context.hasContent()) {
                if (context.getContent().equalsIgnoreCase("all")) {
                    audioManager.scheduler.clear();
                    FixedMessage.build(eb
                            .appendDescription("`all`").build());
                } else {

                    final List<Integer> argList = context.getIntArgs().boxed().collect(Collectors.toList());
                    final int size = queue.size();
                    final IntStream intStream = argList.stream().distinct().mapToInt(Integer::intValue).sorted().map(operand -> operand - 1).filter(integer -> integer >= 0 && integer < size);
                    final ArrayDeque<AudioTrack> skip = audioManager.scheduler.skip(argList.stream()).collect(Collectors.toCollection(ArrayDeque::new));
                    intStream.mapToObj(value -> String.format("\n`%d.` %s", value + 1, skip.removeLast())).forEachOrdered(eb::appendDescription);
                }
            } else {
                AudioTrack current = audioManager.player.getPlayingTrack();
                eb.appendDescription(Play.trackInfoDisplay(current.getInfo()));
                if (queue.size() > 0) {
                    eb.appendDescription("\nNow Playing: ").appendDescription(Play.trackInfoDisplay(queue.get(0).getInfo()));
                }
                audioManager.scheduler.nextTrack();
            }
            return FixedMessage.build(eb.build());
        }
        return null;
    }
}