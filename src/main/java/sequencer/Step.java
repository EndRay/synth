package sequencer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Step implements Iterable<Note> {
    private final CopyOnWriteArrayList<Note> notes;
    public Step(Note... notes){
        this.notes = new CopyOnWriteArrayList<>();
        Collections.addAll(this.notes, notes);
    }
    void addNote(Note note){
        this.notes.add(note);
    }
    void clear(){
        this.notes.clear();
    }

    public List<Note> getNotes(){
        return Collections.unmodifiableList(notes);
    }

    @Override
    public ListIterator<Note> iterator() {
        return getNotes().listIterator();
    }
}
