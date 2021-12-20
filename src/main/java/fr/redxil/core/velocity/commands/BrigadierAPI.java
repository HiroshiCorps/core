package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

public abstract class BrigadierAPI {

    private final String name;

    public BrigadierAPI(String name) {
        this.name = name;
    }


    public abstract int execute(CommandContext<CommandSource> context);

    public abstract void registerArgs(LiteralCommandNode<CommandSource> commandNode);

    public LiteralCommandNode<CommandSource> buildCommands() {

        LiteralCommandNode<CommandSource> commands = LiteralArgumentBuilder.<CommandSource>literal(this.name)
                .executes(context -> {
                    execute(context);
                    return 1;
                }).build();

        registerArgs(commands);

        return commands;

    }

    public <T> CommandNode<CommandSource> addArgumentCommand(CommandNode<CommandSource> literalCommandNode, String name, ArgumentType<T> type) {
        ArgumentCommandNode<CommandSource, T> node = RequiredArgumentBuilder.<CommandSource, T>argument(name, type)
                .suggests(((context, builder) -> builder.buildFuture())).executes(literalCommandNode.getCommand()).build();

        literalCommandNode.addChild(node);
        return node;
    }

    public <T> CommandNode<CommandSource> addArgumentCommand(CommandNode<CommandSource> literalCommandNode, String name, ArgumentType<T> type, String... argsTab) {
        ArgumentCommandNode<CommandSource, T> node = RequiredArgumentBuilder.<CommandSource, T>argument(name, type)
                .suggests(((context, builder) -> {

                    for (String args : argsTab) {
                        builder.suggest(args);
                    }

                    return builder.buildFuture();
                })).executes(literalCommandNode.getCommand()).build();

        literalCommandNode.addChild(node);
        return node;
    }


    public String getName() {
        return name;
    }

    public BrigadierCommand getBrigadierCommand() {
        return new BrigadierCommand(this.buildCommands());
    }
}
