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
package com.github.breadmoirai.samurai.plugins.music.commands;

import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.samurai.plugins.music.AbstractMusicCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.stream.Collectors;

public class CanPlay extends AbstractMusicCommand {

    @Override
    public void onCommand(CommandEvent event) {
        final Member selfMember = event.getGuild().getSelfMember();
        final String permss = event.getGuild().getVoiceChannels().stream().map(voiceChannel -> (PermissionUtil.checkPermission(voiceChannel, selfMember, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK) ? "+" : "-") + voiceChannel.getName()).collect(Collectors.joining("\n", "```diff\n", "\n```"));
        event.reply(permss);
    }
}