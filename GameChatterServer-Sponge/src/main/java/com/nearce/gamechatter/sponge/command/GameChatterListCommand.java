/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge.command;

import com.nearce.gamechatter.ChatParticipant;
import com.nearce.gamechatter.RemoteChatUser;
import com.nearce.gamechatter.sponge.GameChatterPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.stream.Collectors;

public class GameChatterListCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        src.sendMessage(Text.of("There are ", players.size(), "/", Sponge.getServer().getMaxPlayers(), " online:"));
        src.sendMessage(Text.of(String.join(", ", players.stream().map(User::getName).collect(Collectors.toList()))));

        Collection<ChatParticipant> participants = GameChatterPlugin.inst().getConnectedParticipants();
        src.sendMessage(Text.of("There are ", participants.size(), " remote chat users:"));
        src.sendMessage(Text.of(String.join(", ", participants.stream().map(ChatParticipant::getName).collect(Collectors.toList()))));

        return CommandResult.success();
    }

    public static CommandSpec aquireSpec() {
        return CommandSpec.builder()
                .description(Text.of("List online players"))
                .executor(new GameChatterListCommand()).build();
    }
}
