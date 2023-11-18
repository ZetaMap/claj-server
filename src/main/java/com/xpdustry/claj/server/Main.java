package com.xpdustry.claj.server;

import arc.net.ArcNet;
import arc.net.Connection;
import arc.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static final String[] tags = { "&lc&fb[D]&fr", "&lb&fb[I]&fr", "&ly&fb[W]&fr", "&lr&fb[E]", "" };
    public static final DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static void main(String[] args) {
        ArcNet.errorHandler = Log::err;
        Log.logger = (level, text) -> { // this is how fashionable i am
            String result = Log.format("&lk&fb[" + dateTime.format(LocalDateTime.now()) + "]&fr " + tags[level.ordinal()] + " " + text + "&fr");
            System.out.println(result);
        };

        var distributor = new Distributor();
        new Control(distributor);

        try {
            if (args.length == 0) throw new RuntimeException("Need a port as an argument!");
            distributor.run(Integer.parseInt(args[0]));
        } catch (Throwable error) {
            Log.err("Could not to load redirect system", error);
        }
    }

    public static String getIP(Connection connection) {
        return connection.getRemoteAddressTCP().getAddress().getHostAddress();
    }
}
