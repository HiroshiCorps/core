package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public class PartyCMD extends LiteralArgumentCreator<CommandSource> {

    public PartyCMD() {
        super("party");
        super.setExecutor(this::emptyCommand);

        super.createLiteralArgument("invite", this::missingPlayer).createLiteralThen("player", this::inviteCommand);
        super.createLiteralArgument("revokeinvite", this::missingPlayer).createLiteralThen("player", this::revokeCommand);
        super.createLiteralArgument("join", this::missingPlayer).createLiteralThen("player", this::joinCommand);
        super.createLiteralArgument("kick", this::missingPlayer).createLiteralThen("player", this::kickCommand);

        super.createLiteralArgument("info", this::infoCommand);
        super.createLiteralArgument("create", this::createCommand);
        super.createLiteralArgument("leave", this::leaveCommand);
        super.createLiteralArgument("help", this::helpCommand);
    }

    public void emptyCommand(CommandContext<CommandSource> command, String args) {
        command.getSource().sendMessage(Component.text("Merci de faire /party help"));
    }

    public Optional<Party> getParty(UUID uuid) {
        return CoreAPI.getInstance().getPartyManager().getPlayerParty(uuid);
    }

    public Optional<Player> getPlayer(CommandSource source) {
        if (source instanceof Player player)
            return Optional.of(player);
        return Optional.empty();
    }

    public Optional<Player> getPlayer(String name) {
        return CoreVelocity.getInstance().getProxyServer().getPlayer(name);
    }

    public void missingPlayer(CommandContext<CommandSource> command, String args) {
        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;
        opPlayer.get().sendMessage(Component.text("Merci de faire /party " + args + " <joueur>"));
    }

    public void inviteCommand(CommandContext<CommandSource> command, String args) {

        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;

        Optional<Player> opTarget = getPlayer(command.getArgument("player", String.class));
        if (opTarget.isEmpty()) {
            opPlayer.get().sendMessage(Component.text("Joueur non reconnue"));
            return;
        }

        Optional<Party> opParty = getParty(opPlayer.get().getUniqueId());
        opParty.ifPresentOrElse(party -> {
            if (party.invitePlayer(opTarget.get().getUniqueId())) {
                opPlayer.get().sendMessage(Component.text("Le joueur à reçu l'invitation"));
            } else opPlayer.get().sendMessage(Component.text("Impossible de l'inviter"));
        }, () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie")));

    }

    public void revokeCommand(CommandContext<CommandSource> command, String args) {

        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;

        Optional<Player> opTarget = getPlayer(command.getArgument("player", String.class));
        if (opTarget.isEmpty()) {
            opPlayer.get().sendMessage(Component.text("Joueur non reconnue"));
            return;
        }

        Optional<Party> opParty = getParty(opPlayer.get().getUniqueId());
        opParty.ifPresentOrElse(party -> {
            if (party.revokeInvite(opTarget.get().getUniqueId())) {
                opPlayer.get().sendMessage(Component.text("Le joueur à vue sont invitation déchirée"));
            } else opPlayer.get().sendMessage(Component.text("Il n'est pas invité"));
        }, () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie")));

    }

    public void joinCommand(CommandContext<CommandSource> command, String args) {

        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;

        Optional<Player> opTarget = getPlayer(command.getArgument("player", String.class));
        if (opTarget.isEmpty()) {
            opPlayer.get().sendMessage(Component.text("Joueur non reconnue"));
            return;
        }

        Optional<Party> opParty = getParty(opTarget.get().getUniqueId());
        opParty.ifPresentOrElse(party -> {
            if (party.joinParty(opPlayer.get().getUniqueId())) {
                opPlayer.get().sendMessage(Component.text("Vous avez rejoins la partie"));
            } else opPlayer.get().sendMessage(Component.text("Action impossible"));
        }, () -> opPlayer.get().sendMessage(Component.text("La cible n'est pas dans une partie")));

    }

    public void kickCommand(CommandContext<CommandSource> command, String args) {

        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;

        Optional<Player> opTarget = getPlayer(command.getArgument("player", String.class));
        if (opTarget.isEmpty()) {
            opPlayer.get().sendMessage(Component.text("Joueur non reconnue"));
            return;
        }

        Optional<Party> opParty = getParty(opPlayer.get().getUniqueId());
        opParty.ifPresentOrElse(party -> {
            if (party.kickParty(opPlayer.get().getUniqueId(), opTarget.get().getUniqueId())) {
                opPlayer.get().sendMessage(Component.text("Le joueur à disparu"));
            } else opPlayer.get().sendMessage(Component.text("Action impossible"));
        }, () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie")));

    }

    public void infoCommand(CommandContext<CommandSource> command, String args) {
        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;
        getParty(opPlayer.get().getUniqueId()).ifPresentOrElse(party -> opPlayer.get().sendMessage(Component.text("Commande indisponible pour le moment")), () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie !!")));
    }

    public void createCommand(CommandContext<CommandSource> command, String args) {
        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;
        CoreAPI.getInstance().getPartyManager().createParty(opPlayer.get().getUniqueId()).ifPresentOrElse(party -> opPlayer.get().sendMessage(Component.text("Tu es maintenant dans une partie")), () -> opPlayer.get().sendMessage(Component.text("Impossible, tu es déjà dans une partie")));
    }

    public void leaveCommand(CommandContext<CommandSource> command, String args) {
        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;
        getParty(opPlayer.get().getUniqueId()).ifPresentOrElse(party -> {
            party.quitParty(opPlayer.get().getUniqueId());
            opPlayer.get().sendMessage(Component.text("Vous avez quitté la partie"));
        }, () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie")));
    }

    public void helpCommand(CommandContext<CommandSource> command, String args) {
        Optional<Player> opPlayer = getPlayer(command.getSource());
        if (opPlayer.isEmpty())
            return;
        getParty(opPlayer.get().getUniqueId()).ifPresentOrElse(party -> {
            party.quitParty(opPlayer.get().getUniqueId());
            opPlayer.get().sendMessage(Component.text("Vous avez quitté la partie"));
        }, () -> opPlayer.get().sendMessage(Component.text("Tu n'est pas dans une partie")));
    }

}
