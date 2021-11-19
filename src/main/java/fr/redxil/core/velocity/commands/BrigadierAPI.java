package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public abstract class BrigadierAPI {

    private final String name;
    private final List<ArgumentCommandNode<CommandSource, String>> args = new ArrayList<>();
    private BrigadierCommand brigadierCommand;

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

        for (int i = 0; i < args.size(); i++) {

            if (i == 0) {

                commands.addChild(args.get(i));

            } else {

                args.get(i - 1).addChild(args.get(i));

            }
        }

        return commands;

    }

    public void addArgumentCommand(LiteralCommandNode<CommandSource> literalCommandNode, String name, StringArgumentType type) {
        ArgumentCommandNode<CommandSource, String> node = RequiredArgumentBuilder.<CommandSource, String>argument(name, type)
                .suggests(((context, builder) -> {
                    return builder.buildFuture();
                })).executes(literalCommandNode.getCommand()).build();


        this.args.add(node);
    }

    public void addArgumentCommand(LiteralCommandNode<CommandSource> literalCommandNode, String name, StringArgumentType type, String... argsTab) {
        ArgumentCommandNode<CommandSource, String> node = RequiredArgumentBuilder.<CommandSource, String>argument(name, type)
                .suggests(((context, builder) -> {

                    for (String args : argsTab) {
                        builder.suggest(args);
                    }

                    return builder.buildFuture();
                })).executes(literalCommandNode.getCommand()).build();


        this.args.add(node);
    }


    public String getName() {
        return name;
    }

    public BrigadierCommand getBrigadierCommand() {

        LiteralCommandNode<CommandSource> literalCommandNode = this.buildCommands();

        registerArgs(literalCommandNode);
        this.brigadierCommand = new BrigadierCommand(literalCommandNode);
        return this.brigadierCommand;
    }
}
