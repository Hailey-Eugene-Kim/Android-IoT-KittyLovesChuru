package com.scsa.andr.selfmanagementapp;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NoteManager {
    private static final String TAG = "NoteManager_SCSA";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NOTE = "note";

    private static File file;
    /**singleton 생성**/
    private static NoteManager manager;
    private NoteManager(){ }
    public static NoteManager getInstance(Context context){
        if(manager == null){
            manager = new NoteManager();
            file = new File(context.getFilesDir(), "data.ser");
            manager.readFile();
        }

        return manager;
    }
    /**singleton 생성**/

    List<com.scsa.andr.selfmanagementapp.Note> noteList;
    private void readFile(){
        //내부 저장소에 읽기
        if(file.exists()){
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))){
//                Log.d(TAG, "read file : "+file.getCanonicalPath());
                noteList = (List<com.scsa.andr.selfmanagementapp.Note>)input.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "File input Error");
            }
        }else{
            noteList = new ArrayList<>();
        }
    }
    private boolean writeFile(){
        if(file.exists()){
            file.delete();
        }

        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))){
//            Log.d(TAG, "output file : "+file.getCanonicalPath());
            output.writeObject(noteList);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "File Output Error");
            return false;
        }
    }

    public boolean createNote(com.scsa.andr.selfmanagementapp.Note note){
        noteList.add(note);

        return writeFile();
    }

    public boolean deleteNote(int rowId){
        noteList.remove(rowId);
        return writeFile();
    }

    public List<com.scsa.andr.selfmanagementapp.Note> getAllNotes(){
        return noteList;
    }

    public com.scsa.andr.selfmanagementapp.Note getNote(int rowId){
        return noteList.get(rowId);
    }

    public boolean updateNote(int rowId, com.scsa.andr.selfmanagementapp.Note note){
//        Log.d(TAG, "updateNote: "+rowId);
        noteList.set(rowId, note);

        return writeFile();
    }
}
