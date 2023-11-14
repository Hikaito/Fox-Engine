package system.undo;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import java.util.LinkedList;
// data structure for system.undo and redo sets
//the pattern:
// Take Action:
//      store current state on system.undo stack [an system.undo-able task returns current state as a Receipt]
//      alter state to new state
//      if there is a redo stack, destroy it
// Undo: put current state on redo stack, make system.undo the current state
// Redo: put current state on system.undo stack, make redo the current state

public class UndoRedoStack {
    // variables
    private final int maxStackSize; // maximum size for the system.undo and redo stacks
    private final LinkedList<Receipt> undoList = new LinkedList<>();
    private final LinkedList<Receipt> redoList = new LinkedList<>();

    // accessors
    public int getMaxStackSize(){
        return maxStackSize;
    }
    public boolean hasUndo(){
        return !undoList.isEmpty();
    }
    public boolean hasRedo(){
        return !redoList.isEmpty();
    }

    // constructor
    public UndoRedoStack(int maxStackSize){
        //if stack size illegal, make new stack of default size
        if (maxStackSize < 0) this.maxStackSize = 0;
        else this.maxStackSize = maxStackSize;
    }

    // add to system.undo list accessor
    public void add(Receipt task){
        //clear redo list
        redoList.clear();

        // add to end of system.undo list
        undoList.addLast(task);

        // pop beginning if applicable
        while(undoList.size() > maxStackSize){
            undoList.removeFirst();
        }
    }

    // system.undo accessor
    public boolean undo(){
        // empty lists if none
        if(undoList.isEmpty()) return false;

        // retrieve task
        Receipt task = undoList.getLast();
        undoList.removeLast();

        // perform task and store revoked state to redo
        redoList.addLast(task.revoke());

        // pop beginning of stack if applicable
        while(redoList.size() > maxStackSize){
            redoList.removeFirst();
        }

        return true;
    }

    // redo accessor
    public boolean redo(){
        // empty lists if none
        if(redoList.isEmpty()) return false;

        // retrieve task
        Receipt task = redoList.getLast();
        redoList.removeLast();

        // perform task and store revoked state in system.undo list
        undoList.addLast(task.revoke());

        // pop beginning if applicable
        while(undoList.size() > maxStackSize){
            undoList.removeFirst();
        }

        return true;
    }
}
