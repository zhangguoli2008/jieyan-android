package com.quitbuddy.ui.export;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.quitbuddy.R;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportDataActivity extends AppCompatActivity {

    private TextView textStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        textStatus = findViewById(R.id.textExportStatus);
        MaterialButton buttonExport = findViewById(R.id.buttonExport);
        buttonExport.setOnClickListener(v -> exportData());
    }

    private void exportData() {
        File dir = getExternalFilesDir(null);
        if (dir == null) {
            textStatus.setText(R.string.msg_export_failed);
            return;
        }
        String fileName = "cravings_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
        File file = new File(dir, fileName);
        QuitBuddyRepository.getInstance(this).exportCravings(file, success -> {
            if (success) {
                textStatus.setText(getString(R.string.msg_export_success, file.getAbsolutePath()));
            } else {
                textStatus.setText(R.string.msg_export_failed);
            }
        });
    }
}
