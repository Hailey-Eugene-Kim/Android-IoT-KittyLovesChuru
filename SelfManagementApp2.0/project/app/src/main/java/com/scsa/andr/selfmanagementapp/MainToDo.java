package com.scsa.andr.selfmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.scsa.andr.selfmanagementapp.databinding.NotepadListBinding;
import com.scsa.andr.selfmanagementapp.databinding.NotesRowBinding;

import java.util.List;

public class MainToDo extends AppCompatActivity {

    private static final String TAG = "MainToDo_SCSA";

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;


    public static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private int mNoteNumber = 1;
    private NoteManager noteManager;

    public static int churucnt;

    private NotepadListBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NotepadListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //setContentView(R.layout.todo_main);

        //추가
        Intent in = getIntent();
        //Toast.makeText(this, "onCreate."+in.getAction(), Toast.LENGTH_SHORT).show();

        noteManager = NoteManager.getInstance(this);
        fillData();

        Button button3 = (Button) findViewById(R.id.done);
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                churucnt++;
                Toast.makeText(MainToDo.this, getString(R.string.churucnt) + churucnt, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                item.getOrder();
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Log.d(TAG, "onContextItemSelected: "+info.id);
                noteManager.deleteNote((int) info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent intent = new Intent(this, NoteEdit.class);
        startActivityForResult(intent, ACTIVITY_CREATE);

//        deprecated 된 startActivityForResult 대신하여 Launcher를 사용한다.
//        launcher.launch(intent);
    }


//    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
//        new ActivityResultContracts.StartActivityForResult(),
//        new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Bundle extras = result.getData().getExtras();
//                    Note note = (Note)extras.getSerializable(NoteManager.KEY_NOTE);
//                    noteManager.createNote(note);
//                }
//                fillData();
//            }
//        });


    private void fillData() {
        ListView listView = binding.listView;

        NoteAdapter adapter = new NoteAdapter();
        adapter.setList(noteManager.getAllNotes());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                Log.d(TAG, "onItemClick: position:"+position);
//                Log.d(TAG, "onItemClick: id:"+id);
                Intent intent = new Intent(MainToDo.this, NoteEdit.class);
                Bundle bundle = new Bundle();
                bundle.putInt(NoteManager.KEY_ROWID, position);
                bundle.putSerializable(NoteManager.KEY_NOTE, noteManager.getNote(position));

                intent.putExtras(bundle); //bundle을 intent에 집어넣음
                startActivityForResult(intent, ACTIVITY_EDIT);
            }
        });

        registerForContextMenu(listView);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(intent != null){
            Bundle extras = intent.getExtras();
            Note note;
            switch(requestCode) {
                case ACTIVITY_CREATE: //입력으로 했을 때
                    note = (Note)extras.getSerializable(NoteManager.KEY_NOTE);
                    noteManager.createNote(note);
                    break;

                case ACTIVITY_EDIT: //수정으로 했을 때
                    Integer rowId = extras.getInt(NoteManager.KEY_ROWID);
                    note = (Note)extras.getSerializable(NoteManager.KEY_NOTE);
                    noteManager.updateNote(rowId, note);
                    break;
            }
        }
        fillData();
    }


    private class NoteAdapter extends BaseAdapter {
        List<Note> list;

        public void setList(List<Note> list){
            this.list = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder;

            if (convertView == null) {
//                convertView = LayoutInflater.from(Notepadv2.this).inflate(R.layout.notes_row,null);
//
//                holder = new ViewHolder();
//                holder.text = (TextView) convertView.findViewById(R.id.text1);

                NotesRowBinding binding = NotesRowBinding.inflate(LayoutInflater.from(MainToDo.this));
                convertView = binding.getRoot();

                holder = new ViewHolder();
                holder.textView = binding.text1;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.textView.setText(list.get(position).title);

            return convertView;
        }

        class ViewHolder {
            TextView textView;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }

}