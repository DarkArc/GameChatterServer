/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nearce.gamechatter.sponge.command;

import com.nearce.gamechatter.sponge.GameChatterPlugin;
import com.nearce.gamechatter.sponge.NameVerificationHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class GameChatterVerifyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof User) {
            NameVerificationHandler handler = GameChatterPlugin.inst().getGameServer().getNameVerificationHandler();
            if (handler.tryAttempt(args.<String>getOne("auth code").get(), (User) src)) {
                src.sendMessage(Text.of(TextColors.YELLOW, "Successfully identified!"));
            } else {
                src.sendMessage(Text.of(TextColors.RED, "Invalid verification code provided."));
            }
        }

        return CommandResult.success();
    }

    public static CommandSpec aquireSpec() {
        return CommandSpec.builder()
                .description(Text.of("Verify your account"))
                .arguments(GenericArguments.string(Text.of("auth code")))
                .executor(new GameChatterVerifyCommand()).build();
    }
}
