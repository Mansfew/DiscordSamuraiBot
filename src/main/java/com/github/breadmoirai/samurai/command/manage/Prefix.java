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
import com.github.breadmoirai.samurai.command.annotations.Admin;
import com.github.breadmoirai.samurai.command.annotations.Key;
import com.github.breadmoirai.samurai.messages.base.SamuraiMessage;
import com.github.breadmoirai.samurai.messages.impl.FixedMessage;

import java.util.regex.Pattern;

@Admin
@Key("prefix")
public class Prefix extends Command {

    @Override
    public SamuraiMessage execute(CommandContext context) {
        if (!context.hasContent() || Pattern.compile("\\s").matcher(context.getContent()).find())
            return FixedMessage.build("Guild prefix: `" + context.getPrefix() + "`");
        final String newPrefix = context.getContent();
        context.getSamuraiGuildUpdater().updatePrefix(newPrefix);
        return FixedMessage.build(String.format("Prefix successfully set to `%s`", newPrefix));
    }
}