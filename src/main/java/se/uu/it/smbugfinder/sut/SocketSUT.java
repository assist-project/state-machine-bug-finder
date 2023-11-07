package se.uu.it.smbugfinder.sut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public final class SocketSUT implements SUT<String, String> {
    private static String RESET = "reset";

    private PrintWriter sockout;
    private BufferedReader sockin;
    private String reset;
    private String resetConfirmation;

    public SocketSUT(Socket sock) {
        this(sock, RESET, null);
    }

    public SocketSUT(Socket sock, String reset, @Nullable String resetConfirmation) {
        try {
            // Create socket out (no buffering) and in
            sockout = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8), true);
            sockin = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            this.reset = reset;
            this.resetConfirmation = resetConfirmation;
            sendReset();
        }
        catch (IOException e) {
            throw new SocketSutException("Failed to connect to the SUT", e);
        }
    }

    private void sendReset() {
        sockout.println(reset);
        if (resetConfirmation != null) {
            try {
                String readConfirmation = sockin.readLine();
                if (!readConfirmation.equals(resetConfirmation)) {
                    throw new SocketSutException("On reset, received \"" + readConfirmation + "\" when expected the confirmation message \"" + resetConfirmation +"\".");
                }
            } catch (IOException e) {
                throw new SocketSutException("Could not read reset confirmation", e);
            }
        }
    }

    private String sendInput(String input)  {
        String output = null;
        sockout.println(input);
        try {
            output = sockin.readLine();
        } catch (IOException e) {
            throw new SocketSutException("Could not read input", e);
        }
        return output;
    }

    @Override
    public Word<String> execute(Word<String> inputWord) {
        WordBuilder<String> outputBuilder = new WordBuilder<String>();
        for (String input : inputWord) {
            String output = sendInput(input);
            outputBuilder.append(output);
        }
        sendReset();
        return outputBuilder.toWord();
    }

}
