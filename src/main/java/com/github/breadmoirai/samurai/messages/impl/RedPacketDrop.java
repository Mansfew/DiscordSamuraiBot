/*
 *       Copyright 2017 Ton Ly
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.github.breadmoirai.samurai.messages.impl;

import com.github.breadmoirai.samurai.SamuraiDiscord;
import com.github.breadmoirai.samurai.items.Inventory;
import com.github.breadmoirai.samurai.items.Item;
import com.github.breadmoirai.samurai.items.ItemFactory;
import com.github.breadmoirai.samurai.messages.base.DynamicMessage;
import com.github.breadmoirai.samurai.messages.base.Reloadable;
import com.github.breadmoirai.samurai.messages.listeners.ReactionListener;
import com.typesafe.config.ConfigFactory;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RedPacketDrop extends DynamicMessage implements ReactionListener, Reloadable {
    private static final long REACTION = ConfigFactory.load("items").getLong("emote.envelope");
    private static final Color COLOR = new Color(242, 36, 71);
    private static final String TITLE = "\u7ea2\u5305";

    private Instant endTime;
    private int[] drops;
    private int[] dropqueue;
    private int dropsGiven;
    private Set<Long> dropsReceived;
    private transient String dropDisplay;
    private transient ScheduledFuture<?> scheduledFuture;

    public RedPacketDrop() {
    }

    public RedPacketDrop(Duration duration, int[] drops, int[] dropqueue) {
        this.endTime = Instant.now().plus(duration);
        this.drops = drops;
        this.dropqueue = dropqueue;
        dropsGiven = 0;
        dropsReceived = new HashSet<>(dropqueue.length);
    }

    @Override
    protected Message initialize() {
        return new MessageBuilder().append("RedPacket Incoming").build();
    }

    @Override
    protected void onReady(Message message) {
        message.addReaction(message.getJDA().getEmoteById(REACTION)).queue();
        message.editMessage(buildMessage()).queue();
        scheduledFuture = message.getTextChannel().getMessageById(getMessageId()).queueAfter(ChronoUnit.SECONDS.between(Instant.now(), endTime), TimeUnit.SECONDS, message1 -> {

            unregister();
            message1.clearReactions().queue();
            message1.editMessage(buildEndMessage()).queue();
        }, ignored -> {
        });
    }

    private String getDropDisplay() {
        if (dropDisplay == null) {
            final Map<Item, Integer> dropMap = new HashMap<>(drops.length / 2 + 1);
            for (int i = 0; i < drops.length - 1; i += 2) {
                dropMap.put(ItemFactory.getItemById(drops[i]), drops[i + 1]);
            }
            dropDisplay = dropMap.entrySet().stream().sorted((o1, o2) -> {
                final Item o1Key = o1.getKey();
                final Item o2Key = o2.getKey();
                final int o1rare = o1Key.getData().getRarity().ordinal();
                final int o2rare = o2Key.getData().getRarity().ordinal();
                if (o1rare == o2rare) {
                    final Integer o1v = o1.getValue();
                    final Integer o2v = o2.getValue();
                    if (Objects.equals(o1v, o2v)) {
                        return o2.getKey().getData().getItemId() - o1.getKey().getData().getItemId();
                    } else return o2v - o1v;
                } else return o1rare - o2rare;
            }).map(itemIntegerEntry -> {
                final String emote = itemIntegerEntry.getKey().getData().getEmote().getAsMention();
                return IntStream.range(0, itemIntegerEntry.getValue()).mapToObj(value -> emote).collect(Collectors.joining());
            }).collect(Collectors.joining("\n"));
        }
        return dropDisplay;
    }

    private Message buildMessage() {
        return new MessageBuilder().setEmbed(
                new EmbedBuilder()
                        .setColor(COLOR)
                        .setTitle(TITLE)
                        .addField("Available Gifts", getDropDisplay(), false)
                        .setFooter("Ends At", null)
                        .setTimestamp(endTime)
                        .build()
        ).build();
    }

    @Override
    public void onReaction(MessageReactionAddEvent event) {
        if (dropsReceived.add(event.getUser().getIdLong())) {
            Inventory.ofMember(event.getGuild().getIdLong(), event.getUser().getIdLong()).addItem(ItemFactory.getItemById(dropqueue[dropsGiven++]));
        }
        if (dropsGiven == dropqueue.length) {
            if (scheduledFuture.cancel(false)) {
                event.getTextChannel().editMessageById(getMessageId(), buildEndMessage()).queue();
                event.getTextChannel().clearReactionsById(getMessageId()).queue();
            }
        } else event.getTextChannel().editMessageById(getMessageId(), new EmbedBuilder()
                .setColor(COLOR)
                .setTitle(TITLE)
                .addField("Available Gifts", getDropDisplay(), false)
                .addField("Gifts claimed", String.valueOf(dropsGiven), false)
                .setFooter("Ends At", null)
                .setTimestamp(endTime)
                .build()).queue();
    }

    public Message buildEndMessage() {
        return new MessageBuilder()
                .setEmbed(new EmbedBuilder()
                        .setTitle("~~" + TITLE + "~~")
                        .addField("~~Available Gifts~~",
                                IntStream.range(dropsGiven, dropqueue.length).map(operand -> dropqueue[operand]).mapToObj(ItemFactory::getItemById).map(item -> item.getData().getEmote().getAsMention()).collect(Collectors.joining()), false)
                        .addField("Gifts claimed",
                                IntStream.range(0, dropsGiven).map(operand -> dropqueue[operand]).mapToObj(ItemFactory::getItemById).map(item -> item.getData().getEmote().getAsMention()).collect(Collectors.joining()),
                                false)
                        .setFooter("Ended at", null)
                        .setTimestamp(endTime)
                        .build())
                .build();
    }

    @Override
    public void reload(SamuraiDiscord samuraiDiscord) {
        if (endTime.isAfter(Instant.now())) {
            replace(samuraiDiscord.getMessageManager(), getMessageId());
        } else {
            final TextChannel textChannel = samuraiDiscord.getMessageManager().getClient().getTextChannelById(getChannelId());
            if (textChannel != null) {
                textChannel.editMessageById(getMessageId(), buildEndMessage()).queue(null, ignored -> {
                });
            }
        }
    }

    @Override
    public boolean isExpired() {
        return false;
    }
}