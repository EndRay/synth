package ui.cui;

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

    static void play(ConsoleHandler handler) throws LineUnavailableException, IOException {


        byte[] buf = new byte[2];
        AudioFormat af = new AudioFormat((float) sampleRate, 16, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af, (int) (0.1 * sampleRate));
        sdl.start();
        //int samples = (int) (200 * (float) sampleRate);

        Scanner console = new Scanner(System.in);

        SignalSource output = handler.getMix();//new Socket(synth);

        for (int i = 0; true; i++) {
            if (i % 300 == 0 && System.in.available() > 0) {
                String line = console.nextLine();
                line = line.trim();
                if(line.equals("quit"))
                    break;
                handler.handleCommand(line);
            }
            double sample = output.getSample(i);
            handler.samplePassed();
            int sampleInt = (int) (sample * 0x7fffffff);
            buf[1] = (byte) ((sampleInt >> 24) & 0xff);
            buf[0] = (byte) ((sampleInt >> 16) & 0xff);
            sdl.write(buf, 0, 2);
        }
        sdl.drain();
        sdl.stop();
    }

    public static void main(String[] args) throws LineUnavailableException, IOException {
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
        play(handler);
    }
}
