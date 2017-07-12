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

package samurai.command.items;

import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.items.ItemRarity;
import samurai.messages.base.SamuraiMessage;
import samurai.messages.impl.FixedMessage;

import java.util.Arrays;
import java.util.stream.Collectors;

@Key("rarity")
public class ItemRarityInfo extends Command{
    @Override
    protected SamuraiMessage execute(CommandContext context) {
        return FixedMessage.build(Arrays.stream(ItemRarity.values()).map(itemRarity -> itemRarity.getEmote() + itemRarity.toString()).collect(Collectors.joining("\n")));
    }
}
