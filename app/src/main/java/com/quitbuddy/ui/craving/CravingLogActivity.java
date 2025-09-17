package com.quitbuddy.ui.craving;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.quitbuddy.R;
import com.quitbuddy.data.model.CravingEventEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.util.Date;

public class CravingLogActivity extends AppCompatActivity {

    private SeekBar seekIntensity;
    private Spinner spinnerTrigger;
    private SwitchMaterial switchDidSmoke;
    private TextInputEditText inputNote;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_craving_log);
        bindViews();
    }

    private void bindViews() {
        seekIntensity = findViewById(R.id.seekIntensity);
        spinnerTrigger = findViewById(R.id.spinnerTrigger);
        switchDidSmoke = findViewById(R.id.switchDidSmoke);
        inputNote = findViewById(R.id.inputNote);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.craving_triggers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrigger.setAdapter(adapter);

        buttonSave.setOnClickListener(this::saveEvent);
    }

    private void saveEvent(View view) {
        int intensity = seekIntensity.getProgress() + 1;
        String trigger = (String) spinnerTrigger.getSelectedItem();
        boolean didSmoke = switchDidSmoke.isChecked();
        String note = inputNote.getText() != null ? inputNote.getText().toString() : "";

        CravingEventEntity event = new CravingEventEntity(new Date(), intensity, trigger, didSmoke, note);
        QuitBuddyRepository.getInstance(this).logCraving(event, () -> {
            Toast.makeText(this, R.string.msg_craving_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
