# Synth
## How to start
If you want just to make sounds, then run file Database.java in package database (this action need to be performed once). It will create demo synths in database. Then run MainGUI in package ui.gui.
On the left you can define synth structure, but now lets just load demosynth, which we recreated. Under this big field you can see small text field and buttons l "load" and "save". Enter "prophet 6" (without quotes) in this field and press load. Synth's structure now loaded. Press button "build" to build synth with this structure. You already can play it with your midi device. On the left you can edit its parameters.
## StructScript
Maybe you want to define synth's structure by your own. Then you can use StructScript. You write StructScript's code on the left side and then press "build" and this synth became built.
- to create some signal source you write "new ClassName(constructor args...)"
- to save signal source into variable you write "variableName = ..."
- to bind signal source to some "socket" (realtime changable parameter) you write "socket <= ..." or "... => socket"
- to add signal source to some socket you write "socket <- ..." or "... -> socket".
- to process signal which goes to socket you write "socket -<- ..." or "... ->- socket"
- you can multiply, add or sub signals just by writing *, + and -
- to do some lines on per voice basis wrap it into "-v-" and "---"
- to bind signal to output write "... => output"
- all voices outputs become mixed in signal "voiceMix"

# Changelog
## First presentation
- basic sequencer
- parsing midi input
- ability to play midi files
- ability to describe synth structure
- saving synths and their patches to the database 
- ugly but functional gui
## Second presentation
- good beautiful gui
- sequencer builtin keyboard
- chord machine
- drum sequencer
## Final presentation
- setups saving 
- bugfixes

## TODO
### sound things
- [ ] Exponential envelope stages
- [ ] Linear Slope limiter
- [ ] TZFM Oscillators
- [ ] Variable Poles Filters
- [ ] Highpass/Notch/Bandpass\[/Morphable] Filters
### features things
- [ ] (!!!) To WAV
- [ ] (!) lastNoteIsLegato Gate
- [ ] MIDI note map (for drums)
### concept things
- [ ] (!!) Delayed SignalProcessors (???)
- [ ] Stereo
