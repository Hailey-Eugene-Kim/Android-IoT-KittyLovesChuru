package com.scsa.andr.selfmanagementapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.scsa.andr.selfmanagementapp.databinding.NoteEditBinding;

public class NoteEdit extends AppCompatActivity {

    private Integer mRowId;

    private NoteEditBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NoteEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mRowId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mRowId = extras.getInt(com.scsa.andr.selfmanagementapp.NoteManager.KEY_ROWID);
            com.scsa.andr.selfmanagementapp.Note note = (com.scsa.andr.selfmanagementapp.Note)extras.getSerializable(com.scsa.andr.selfmanagementapp.NoteManager.KEY_NOTE);

            binding.title.setText(note.title);
            binding.body.setText(note.body);
        }
       
        binding.confirm.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                com.scsa.andr.selfmanagementapp.Note note = new com.scsa.andr.selfmanagementapp.Note(binding.title.getText().toString(), binding.body.getText().toString(), System.currentTimeMillis() );
                bundle.putSerializable(com.scsa.andr.selfmanagementapp.NoteManager.KEY_NOTE, note);

                if (mRowId != null) {
                    bundle.putInt(com.scsa.andr.selfmanagementapp.NoteManager.KEY_ROWID, mRowId);
                }
                
                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
    }
}
