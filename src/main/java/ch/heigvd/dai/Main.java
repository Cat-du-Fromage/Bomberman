package ch.heigvd.dai;

import ch.heigvd.dai.networkManager.NetworkRoot;
import picocli.CommandLine;

import java.io.File;

public class Main
{
    public static void main(String[] args)
    {
        // Define command name - source: https://stackoverflow.com/a/11159435
        String jarFilename = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
                        .getName();

        // Create root command
        NetworkRoot networkRoot = new NetworkRoot();

        // Execute command and get exit code
        int exitCode = new CommandLine(networkRoot)
                        .setCommandName(jarFilename)
                        .setCaseInsensitiveEnumValuesAllowed(true)
                        .execute(args);

        System.exit(exitCode);
    }
}