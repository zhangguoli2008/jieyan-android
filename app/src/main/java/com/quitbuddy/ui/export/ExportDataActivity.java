package com.quitbuddy.ui.export;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.quitbuddy.BuildConfig;
import com.quitbuddy.R;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.ui.ThemeManager;

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
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        ThemeManager.tintToolbar(toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
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
                shareFile(file);
            } else {
                textStatus.setText(R.string.msg_export_failed);
            }
        });
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
        ShareCompat.IntentBuilder.from(this)
                .setType("text/csv")
                .setStream(uri)
                .setChooserTitle(R.string.export_share_title)
                .setSubject(getString(R.string.export_share_title))
                .setText(getString(R.string.export_share_text))
                .startChooser();
        textStatus.setText(R.string.msg_export_shared);
    }
}
