package com.xpdustry.claj.server;

import arc.util.CommandHandler;
import arc.util.CommandHandler.CommandResponse;
import arc.util.CommandHandler.ResponseType;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Threads;

import java.util.Scanner;

public class Control {

    public final CommandHandler handler = new CommandHandler("");
    public final Distributor distributor;

    public Control(Distributor distributor) {
        this.distributor = distributor;
        this.registerCommands();

        Threads.daemon("Application Control", () -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNext()) handleCommand(scanner.nextLine());
            }
        });
    }

    private void handleCommand(String command) {
        CommandResponse response = handler.handleMessage(command);

        if (response.type == ResponseType.unknownCommand) {
            String closest = handler.getCommandList().map(cmd -> cmd.text).min(cmd -> Strings.levenshtein(cmd, command));
            Log.err("Command not found. Did you mean @?", closest);
        } else if (response.type != ResponseType.noCommand && response.type != ResponseType.valid)
            Log.err("Too @ command arguments.", response.type == ResponseType.fewArguments ? "few" : "many");
    }

    private void registerCommands() {
        handler.register("help", "Display the command list.", args -> {
            Log.info("Commands:");
            handler.getCommandList().each(command -> Log.info("  &b&lb@@&fr - @",
                    command.text, command.paramText.isEmpty() ? "" : " &lc&fi" + command.paramText, command.description));
        });

        handler.register("list", "Displays all current rooms.", args -> {
            Log.info("Rooms:");
            distributor.rooms.forEach(entry -> {
                Log.info("  &b&lbRoom @&fr", entry.value.link);
                entry.value.redirectors.each(r -> {
                    Log.info("    [H] &b&lbConnection @&fr - @", r.host.getID(), Main.getIP(r.host));
                    if (r.client == null) return;
                    Log.info("    [C] &b&lbConnection @&fr - @", r.client.getID(), Main.getIP(r.client));
                });
            });
        });

        handler.register("limit", "[amount]", "Sets spam packet limit.", args -> {
            if (args.length == 0)
                Log.info("Current limit - @ packets per 3 seconds.", distributor.spamLimit);
            else {
                distributor.spamLimit = Strings.parseInt(args[0], 300);
                Log.info("Packet spam limit set to @ packets per 3 seconds.", distributor.spamLimit);
            }
        });

        handler.register("ban", "<IP>", "Adds the IP to blacklist.", args -> {
            Blacklist.add(args[0]);
            Log.info("IP @ has been blacklisted.", args[0]);
        });

        handler.register("unban", "<IP>", "Removes the IP from blacklist.", args -> {
            Blacklist.remove(args[0]);
            Log.info("IP @ has been removed from blacklist.", args[0]);
        });

        handler.register("refresh", "Unbans all IPs and refresh GitHub Actions IPs.", args -> {
            Blacklist.clear();
            Blacklist.refresh();
        });

        handler.register("exit", "Stop hosting distributor and exit the application.", args -> {
            distributor.rooms.forEach(entry -> entry.value.sendMessage("[scarlet]\u26A0[] The server is shutting down.\nTry to reconnect in a minute."));

            Log.info("Shutting down the application.");
            distributor.stop();
        });
    }
}
