package ui.cui;

import synthesizer.SoundPlayer;
import synthesizer.sources.SignalSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static synthesizer.sources.SignalSource.sampleRate;

public class Main {

    public static void main(String[] args) {
        ConsoleHandler handler = new ConsoleHandler();
        if(args.length > 0){
            try{
                File basic = new File(args[0]);
                Scanner reader = new Scanner(basic);
                while (reader.hasNextLine())
                    handler.handleCommand(reader.nextLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        SoundPlayer player = new SoundPlayer(handler.getMix());
        player.addTimeDependent(handler);
        player.play();

        Scanner console = new Scanner(System.in);
        while (true){
            String line = console.nextLine();
            line = line.trim();
            if(line.equals("quit"))
                break;
            handler.handleCommand(line);
        }
        player.stop();
    }
}
