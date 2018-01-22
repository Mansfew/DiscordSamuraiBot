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
package com.github.breadmoirai.samurai.command.general;

import com.github.breadmoirai.samurai.command.Command;
import com.github.breadmoirai.samurai.command.CommandContext;
import com.github.breadmoirai.samurai.command.annotations.Key;
import com.github.breadmoirai.samurai.command.manage.Schedule;
import com.github.breadmoirai.samurai.messages.base.SamuraiMessage;
import com.github.breadmoirai.samurai.messages.impl.FixedMessage;
import net.dv8tion.jda.core.EmbedBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Key("remind")
public class Remind extends Command {

    public SamuraiMessage execute(CommandContext context) {
        final List<String> args = context.getArgs();
        final Duration duration = Schedule.getDuration(args);
        final OffsetDateTime plus = context.getTime().plus(duration);
        context.getCommandScheduler().scheduleCommand(context.getPrefix() + "say " + String.join(" ", args), context.createPrimitive(), context.getChannelId(), plus.toInstant());

        return FixedMessage.build(new EmbedBuilder().setFooter("Reminder scheduled at", null).setTimestamp(plus).build());

    }
}