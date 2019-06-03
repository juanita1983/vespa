// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.security.tool.securityenv;

import com.yahoo.security.tls.MixedMode;
import com.yahoo.security.tls.TransportSecurityOptions;
import com.yahoo.security.tls.TransportSecurityUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.yahoo.vespa.security.tool.securityenv.CliOptions.HELP_OPTION;
import static com.yahoo.vespa.security.tool.securityenv.CliOptions.SHELL_OPTION;

/**
 * Implementation of the 'vespa-security-env' command line utility.
 *
 * @author bjorncs
 */
public class Main {

    private final PrintStream stdOut;
    private final PrintStream stdError;

    Main(PrintStream stdOut, PrintStream stdError) {
        this.stdOut = stdOut;
        this.stdError = stdError;
    }

    public static void main(String[] args) {
        Main program = new Main(System.out, System.err);
        int statusCode = program.execute(args, System.getenv());
        System.exit(statusCode);
    }

    int execute(String[] cliArgs, Map<String, String> envVars) {
        boolean debugMode = envVars.containsKey("VESPA_DEBUG");
        try {
            CommandLine arguments = CliOptions.parseCliArguments(cliArgs);
            if (arguments.hasOption(HELP_OPTION)) {
                CliOptions.printHelp(stdOut);
                return 0;
            }
            UnixShell shell = arguments.hasOption(SHELL_OPTION)
                    ? UnixShell.fromConfigName(arguments.getOptionValue(SHELL_OPTION))
                    : UnixShell.detect(envVars.get("SHELL"));

            Optional<TransportSecurityOptions> options = TransportSecurityUtils.getOptions(envVars);
            if (options.isEmpty()) {
                return 0;
            }
            Map<String, String> outputVariables = new TreeMap<>();
            options.get().getCaCertificatesFile()
                    .ifPresent(caCertFile -> addOutputVariable(outputVariables, OutputVariable.CA_CERTIFICATE, caCertFile.toString()));
            MixedMode mixedMode = TransportSecurityUtils.getInsecureMixedMode(envVars);
            if (mixedMode != MixedMode.PLAINTEXT_CLIENT_MIXED_SERVER) {
                options.get().getCertificatesFile()
                        .ifPresent(certificateFile -> addOutputVariable(outputVariables, OutputVariable.CERTIFICATE, certificateFile.toString()));
                options.get().getPrivateKeyFile()
                        .ifPresent(privateKeyFile -> addOutputVariable(outputVariables, OutputVariable.PRIVATE_KEY, privateKeyFile.toString()));
            }
            shell.writeOutputVariables(stdOut, outputVariables);
            return 0;
        } catch (ParseException e) {
            return handleException("Failed to parse command line arguments: " + e.getMessage(), e, debugMode);
        } catch (IllegalArgumentException e) {
            return handleException("Invalid command line arguments: " + e.getMessage(), e, debugMode);
        } catch (Exception e) {
            return handleException("Failed to generate security environment variables: " + e.getMessage(), e, debugMode);
        }
    }

    private static void addOutputVariable(Map<String, String> outputVariables, OutputVariable variable, String value) {
        outputVariables.put(variable.variableName(), value);
    }

    private int handleException(String message, Exception exception, boolean debugMode) {
        stdError.println(message);
        if (debugMode) {
            exception.printStackTrace(stdError);
        }
        return 1;
    }
}