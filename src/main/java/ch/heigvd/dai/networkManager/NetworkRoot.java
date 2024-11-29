package ch.heigvd.dai.networkManager;

import picocli.CommandLine;

@CommandLine.Command(
        description = "A small application to experiment with UDP.",
        version = "1.0.0",
        subcommands = {Server.class, Client.class},
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true)
public class NetworkRoot {
}
