/*
 *         Copyright 2017 Ton Ly (BreadMoirai)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.github.breadmoirai.bot.modules.music.command;


import com.github.breadmoirai.bot.modules.music.GuildMusicManager;
import com.github.breadmoirai.bot.modules.music.MusicModule;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;

import java.util.Optional;

public class AutoPlay {

    @MainCommand
    public void autoplay(CommandEvent event, MusicModule module) {
        Optional<GuildMusicManager> guildMusicManager = module.retrieveManager(event.getGuildId());
        guildMusicManager.ifPresent(guildMusicManager1 -> event.replyFormat("AutoPlay is currently `%s`", guildMusicManager1.getScheduler().isAutoPlay() ? "enabled" : "disabled"));
    }


    @Command({"on", "enable"})
    public void enable(CommandEvent event, MusicModule module) {
        Optional<GuildMusicManager> guildMusicManager = module.retrieveManager(event.getGuildId());
        if (guildMusicManager.isPresent()) {
            guildMusicManager.get().getScheduler().setAutoPlay(true);
            event.reply("AutoPlay is now `enabled`");
        }
    }

    @Command({"off", "disable"})
    public void disable(CommandEvent event, MusicModule module) {
        Optional<GuildMusicManager> guildMusicManager = module.retrieveManager(event.getGuildId());
        if (guildMusicManager.isPresent()) {
            guildMusicManager.get().getScheduler().setAutoPlay(false);
            event.reply("AutoPlay is now `disabled`");
        }
    }

}
