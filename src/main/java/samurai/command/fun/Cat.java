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

package samurai.command.fun;

import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONObject;
import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.messages.base.SamuraiMessage;
import samurai.messages.impl.FixedMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Key("cat")
public class Cat extends Command {
    @Override
    protected SamuraiMessage execute(CommandContext context) {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new URL("http://random.cat/meow").openStream(), StandardCharsets.UTF_8))) {
            final String file = new JSONObject(rd.readLine()).getString("file");
            return FixedMessage.build(new EmbedBuilder().setImage(file).build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
