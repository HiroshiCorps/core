package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

public abstract class BrigadierAPI<C> {

    private final String name;

    public BrigadierAPI(String name) {
        this.name = name;
    }


    public abstract int execute(CommandContext<C> context);

    public abstract void registerArgs(LiteralCommandNode<C> commandNode);

    public LiteralCommandNode<C> buildCommands() {

        LiteralCommandNode<C> commands = LiteralArgumentBuilder.<C>literal(this.name)
                .executes(context -> {
                    execute(context);
                    return 1;
                }).build();

        registerArgs(commands);

        return commands;

    }

    public <T> CommandNode<C> addArgumentCommand(CommandNode<C> literalCommandNode, String name, ArgumentType<T> type) {
        ArgumentCommandNode<C, T> node = RequiredArgumentBuilder.<C, T>argument(name, type)
                .suggests(((context, builder) -> builder.buildFuture())).executes(literalCommandNode.getCommand()).build();

        literalCommandNode.addChild(node);
        return node;
    }

    public <T> CommandNode<C> addArgumentCommand(CommandNode<C> literalCommandNode, String name, ArgumentType<T> type, String... argsTab) {
        ArgumentCommandNode<C, T> node = RequiredArgumentBuilder.<C, T>argument(name, type)
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

}
