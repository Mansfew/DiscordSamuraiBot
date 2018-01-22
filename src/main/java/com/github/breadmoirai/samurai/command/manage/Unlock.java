/*    Copyright 2017 Ton Ly

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.samurai.command.manage;

import com.github.breadmoirai.samurai.command.Command;
import com.github.breadmoirai.samurai.command.CommandContext;
import com.github.breadmoirai.samurai.command.annotations.Key;
import com.github.breadmoirai.samurai.command.annotations.Source;
import com.github.breadmoirai.samurai.messages.base.SamuraiMessage;
import com.github.breadmoirai.samurai.messages.impl.FixedMessage;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Source
@Key("unlock")
public class Unlock extends Command {
    @Override
    protected SamuraiMessage execute(CommandContext context) {
        System.out.println("unlock: " + context.getContent());
        final Guild guild = context.getGuild();
        final Member author = context.getAuthor();
        final Member selfMember = context.getSelfMember();
        if (!context.hasContent()) {
            final String collect = guild.getTextChannels().stream().filter(textChannel -> selfMember.hasPermission(textChannel, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_WEBHOOKS)).map(textChannel -> {
                if (author.hasPermission(textChannel, Permission.MESSAGE_READ)) {
                    return "+ " + textChannel.getName();
                } else {
                    return "- " + textChannel.getName();
                }
            }).sorted(Comparator.comparingInt(o -> o.codePointAt(0))).collect(Collectors.joining("\n", "```diff\n", "\n```"));
            return FixedMessage.build(collect);
        }
        List<TextChannel> hiddenChannels = guild.getTextChannelsByName(context.getContent(), true);
        TextChannel thatChannel;
        if (hiddenChannels.isEmpty()) {
            if (context.getMentionedChannels().size() == 1) {
                thatChannel = context.getMentionedChannels().get(0);
            } else return FixedMessage.build("Channel Not Found");
        } else thatChannel = hiddenChannels.get(0);
        System.out.println("thatChannel = " + thatChannel);
        System.out.println("selfMember = " + selfMember);
        if (!selfMember.hasPermission(thatChannel, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_WEBHOOKS)) {
            return FixedMessage.build("Channel is unavailable");
        }
        final PermissionOverride permissionOverride = thatChannel.getPermissionOverride(author);
        if (permissionOverride == null) {
            thatChannel.createPermissionOverride(author).setAllow(Permission.MESSAGE_READ).queue();
            return FixedMessage.build("Access granted");
        } else {
            if (permissionOverride.getAllowed().contains(Permission.MESSAGE_READ))
                return FixedMessage.build("Access has already been granted.");
            else return FixedMessage.build("Permission Override Denied");
        }
    }
}