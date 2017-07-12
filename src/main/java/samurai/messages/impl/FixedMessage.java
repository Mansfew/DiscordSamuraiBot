/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
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
package samurai.messages.impl;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import samurai.messages.MessageManager;
import samurai.messages.base.SamuraiMessage;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A messages object that has no further options
 * Created by TonTL on 2/13/2017.
 */
public class FixedMessage extends SamuraiMessage {

    private Message message;
    private Consumer<Message> consumer;
    private transient Duration delay;


    public static FixedMessage build(String s) {
        if (s == null) return null;
        final Queue<Message> messages = new MessageBuilder().append(s).buildAll(MessageBuilder.SplitPolicy.NEWLINE, MessageBuilder.SplitPolicy.SPACE, MessageBuilder.SplitPolicy.ANYWHERE);
        final Message first = messages.poll();
        if (messages.isEmpty())
            return new FixedMessage().setMessage(first);
        else return new FixedMessage().setMessage(first).setConsumer(message1 -> {
            final MessageChannel channel = message1.getChannel();
            while (!messages.isEmpty()) {
                channel.sendMessage(messages.poll()).queue();
            }
        });
    }

    public static FixedMessage build(MessageEmbed e) {
        if (e == null) return null;
        return new FixedMessage().setMessage(new MessageBuilder().setEmbed(e).build());
    }

    public static FixedMessage build(Message m) {
        return new FixedMessage().setMessage(m);
    }

    public Message getMessage() {
        return message;
    }

    public FixedMessage setMessage(Message message) {
        this.message = message;
        return this;
    }

    public Consumer<Message> getConsumer() {
        return consumer;
    }

    public FixedMessage setConsumer(Consumer<Message> consumer) {
        this.consumer = consumer;
        return this;
    }

    public FixedMessage appendConsumer(Consumer<Message> consumer) {
        if (getConsumer() == null) setConsumer(consumer);
        else this.consumer = getConsumer().andThen(consumer);
        return this;
    }

    @Override
    public void send(MessageManager messageManager) {
        if (delay == null)
            messageManager.getClient().getTextChannelById(getChannelId()).sendMessage(message).queue(consumer, null);
        else
            messageManager.getClient().getTextChannelById(getChannelId()).sendMessage(message).queueAfter(delay.getSeconds(), TimeUnit.SECONDS, consumer);
    }

    @Override
    protected Message initialize() {
        return message;
    }

    @Override
    protected void onReady(Message message) {

    }

    public FixedMessage setDelay(int time, TemporalUnit unit) {
        this.delay = Duration.of(time, unit);
        return this;
    }
}
