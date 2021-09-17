package se.uu.it.smbugfinder.sut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class SocketSUT implements SUT<String, String> {
	private static String RESET = "reset";
	
	private PrintWriter sockout;
	private BufferedReader sockin;

	public SocketSUT(Socket sock) {
		try {
			// Create socket out (no buffering) and in 
			sockout = new PrintWriter(sock.getOutputStream(), true);
			sockin = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendReset() {
		sockout.println(RESET);
	}
	
	public String sendInput(String input) throws IOException {
		sockout.println(input);
		String output = sockin.readLine();
		return output;
	} 
	
	@Override
	public Word<String> execute(Word<String> inputWord) {
		WordBuilder<String> outputBuilder = new WordBuilder<String>();
		for (String input : inputWord) {
			try {
				String output = sendInput(input);
				outputBuilder.append(output);
			} catch (IOException e) {
				throw new RuntimeException("Failed to send input " + input);
			}
			
		}
		return outputBuilder.toWord();
	}

}
