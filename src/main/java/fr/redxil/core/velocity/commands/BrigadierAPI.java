package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BrigadierAPI<C> {

    private final String name;

    public BrigadierAPI(String name) {
        this.name = name;
    }

    public abstract void registerArgs(LiteralCommandNode<C> commandNode);

    public abstract void onCommandWithoutArgs(CommandContext<C> commandExecutor);

    public LiteralCommandNode<C> buildCommands() {

        LiteralCommandNode<C> commands = LiteralArgumentBuilder.<C>literal(this.name)
                .executes(context -> {
                    onCommandWithoutArgs(context);
                    return 1;
                }).build();

        registerArgs(commands);

        return commands;

    }

    public <T> CommandNode<C> addArgumentCommand(CommandNode<C> literalCommandNode, String name, ArgumentType<T> type, Consumer<CommandContext<C>> argumentExecutor) {
        ArgumentCommandNode<C, T> node = RequiredArgumentBuilder.<C, T>argument(name, type)
                .suggests(((context, builder) -> builder.buildFuture())).executes(context -> {
                    argumentExecutor.accept(context);
                    return 1;
                }).build();

        literalCommandNode.addChild(node);
        return node;
    }

    public <T> CommandNode<C> addArgumentCommand(CommandNode<C> literalCommandNode, String name, ArgumentType<T> type, Consumer<CommandContext<C>> argumentExecutor, String... argsTab) {
        ArgumentCommandNode<C, T> node = RequiredArgumentBuilder.<C, T>argument(name, type)
                .suggests(((context, builder) -> {

                    for (String args : argsTab) {
                        builder.suggest(args);
                    }

                    return builder.buildFuture();
                })).executes(context -> {
                    argumentExecutor.accept(context);
                    return 1;
                }).build();

        literalCommandNode.addChild(node);
        return node;
    }

    public <T> CommandNode<C> addArgumentCommand(CommandNode<C> literalCommandNode, String name, ArgumentType<T> type, Consumer<CommandContext<C>> argumentExecutor, BiConsumer<CommandContext<C>, SuggestionsBuilder> biConsumer) {
        ArgumentCommandNode<C, T> node = RequiredArgumentBuilder.<C, T>argument(name, type)
                .suggests(((context, builder) -> {

                    biConsumer.accept(context, builder);

                    return builder.buildFuture();
                })).executes(context -> {
                    argumentExecutor.accept(context);
                    return 1;
                }).build();

        literalCommandNode.addChild(node);
        return node;
    }


    public String getName() {
        return name;
    }

}
