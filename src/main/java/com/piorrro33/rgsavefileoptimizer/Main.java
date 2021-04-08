package com.piorrro33.rgsavefileoptimizer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Callable;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

@Command(name = Main.APPLICATION_NAME, version = Main.APPLICATION_VERSION,
        description = "Rogue Galaxy Save File Optimizer", mixinStandardHelpOptions = true)
public class Main implements Callable<Integer> {
    public static final String APPLICATION_NAME = "rg-save-file-optimizer";
    public static final String APPLICATION_VERSION = "v0.1dev";
    public static final int SAVE_FILE_SIZE = 0x1BC00;
    public static final int HASHED_DATA_LENGTH = 1; // Only hash first byte of save data, always 0x00
    public static final byte[] DUMMY_MD5 = hexStringToByteArray("93b885adfe0da089cdf634904fd59f71"); // MD5 of 0x00
    public static final short CHAPTER_NUM = 0;

    @Parameters(arity = "1..*", paramLabel = "FILES", description = "save files to optimize")
    private Path[] filePaths;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).setExecutionExceptionHandler(Main::handleExecutionException).execute(args);
        System.exit(exitCode);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) {
        System.err.println("An exception has occurred when running the command!" + ex.getMessage());
        return 1;
    }

    @Override
    public Integer call() throws Exception {
        System.out.println(APPLICATION_NAME + ' ' + APPLICATION_VERSION);
        for (Path filePath : filePaths) {
            System.out.println("Optimizing \"" + filePath + "\"...");
            if (!Files.isRegularFile(filePath)) {
                System.err.println("Error: " + filePath + " is not a valid path to a save file.");
                return ExitCode.USAGE;
            }
            long fileSize = Files.size(filePath);
            if (fileSize != SAVE_FILE_SIZE) {
                System.err.println("Error: " + filePath + " is of invalid size. Expected " + SAVE_FILE_SIZE + ", got " + fileSize);
                return ExitCode.SOFTWARE;
            }
            Path copyPath = filePath.resolveSibling(filePath.getFileName() + ".orig");
            if (Files.exists(copyPath)) {
                // File already exists, find first .origX that does not exist
                int i = 0;
                Path testedPath;
                do {
                    testedPath = copyPath.resolveSibling(copyPath.getFileName().toString() + i);
                    i++;
                } while (Files.exists(testedPath));
                copyPath = testedPath;
            }
            System.out.println("Making a backup of the save file...");
            Files.copy(filePath, copyPath);
            try (SeekableByteChannel inChannel = Files.newByteChannel(filePath, READ, WRITE)) {
                ByteBuffer bb = ByteBuffer.allocate(SAVE_FILE_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                System.out.println("Reading save file...");
                inChannel.read(bb);
                System.out.println("Editing header...");
                bb.putInt(0x10, HASHED_DATA_LENGTH);
                int index = 0x20;
                for (byte b : DUMMY_MD5) {
                    bb.put(index++, b);
                }
                bb.putInt(0x30, HASHED_DATA_LENGTH);
                index = 0x40;
                for (byte b : DUMMY_MD5) {
                    bb.put(index++, b);
                }
                System.out.println("Editing chapter number...");
                bb.putShort(0x20C, CHAPTER_NUM);
                System.out.println("Writing save file...");
                bb.flip();
                inChannel.position(0);
                inChannel.write(bb);
            } catch (IOException e) {
                System.err.println("Error: something went wrong when optimizing the save file! " + e.getMessage());
                try {
                    Files.move(copyPath, filePath, StandardCopyOption.REPLACE_EXISTING);
                    System.err.println("The original file has been restored.");
                } catch (IOException e2) {
                    System.err.println("Error: could not restore the original file. It should be at " + copyPath);
                    System.err.println("Exception message: " + e2.getMessage());
                }
                return ExitCode.SOFTWARE;
            }
        }
        System.out.println("Done!");
        return ExitCode.OK;
    }
}
