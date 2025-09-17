package com.quitbuddy.ui.onboarding;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.quitbuddy.R;
import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;
import com.quitbuddy.ui.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutBaseline;
    private TextInputLayout layoutCigs;
    private TextInputLayout layoutPrice;
    private TextInputLayout layoutReminder1;
    private TextInputLayout layoutReminder2;
    private TextInputLayout layoutReminder3;
    private TextInputEditText inputStartDate;
    private TextInputEditText inputBaseline;
    private TextInputEditText inputCigs;
    private TextInputEditText inputPrice;
    private TextInputEditText inputReminder1;
    private TextInputEditText inputReminder2;
    private TextInputEditText inputReminder3;
    private MaterialButtonToggleGroup toggleGroup;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Date selectedDate = new Date();
    private String selectedMode = QuitPlanEntity.MODE_COLD_TURKEY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        bindViews();
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ThemeManager.tintToolbar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutBaseline = findViewById(R.id.layoutBaseline);
        layoutCigs = findViewById(R.id.layoutCigs);
        layoutPrice = findViewById(R.id.layoutPrice);
        layoutReminder1 = findViewById(R.id.layoutReminder1);
        layoutReminder2 = findViewById(R.id.layoutReminder2);
        layoutReminder3 = findViewById(R.id.layoutReminder3);
        inputStartDate = findViewById(R.id.inputStartDate);
        inputBaseline = findViewById(R.id.inputBaseline);
        inputCigs = findViewById(R.id.inputCigsPerPack);
        inputPrice = findViewById(R.id.inputPrice);
        inputReminder1 = findViewById(R.id.inputReminder1);
        inputReminder2 = findViewById(R.id.inputReminder2);
        inputReminder3 = findViewById(R.id.inputReminder3);
        toggleGroup = findViewById(R.id.toggleMode);

        layoutReminder2.setHint(getString(R.string.onboarding_reminder_secondary, 2));
        layoutReminder3.setHint(getString(R.string.onboarding_reminder_secondary, 3));

        inputStartDate.setText(dateFormat.format(selectedDate));
        View.OnClickListener dateClick = v -> showDatePicker();
        inputStartDate.setOnClickListener(dateClick);

        View.OnClickListener timeClick = this::showTimePicker;
        inputReminder1.setOnClickListener(timeClick);
        inputReminder2.setOnClickListener(timeClick);
        inputReminder3.setOnClickListener(timeClick);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonModeGradual) {
                    selectedMode = QuitPlanEntity.MODE_GRADUAL;
                } else {
                    selectedMode = QuitPlanEntity.MODE_COLD_TURKEY;
                }
            } else if (group.getCheckedButtonId() == View.NO_ID) {
                selectedMode = null;
            }
        });

        findViewById(R.id.buttonSave).setOnClickListener(v -> attemptSave());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDate = cal.getTime();
            inputStartDate.setText(dateFormat.format(selectedDate));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker(View view) {
        TextInputEditText target = (TextInputEditText) view;
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void attemptSave() {
        clearErrors();
        String baselineText = inputBaseline.getText() != null ? inputBaseline.getText().toString().trim() : "";
        String cigsText = inputCigs.getText() != null ? inputCigs.getText().toString().trim() : "";
        String priceText = inputPrice.getText() != null ? inputPrice.getText().toString().trim() : "";
        String reminder1 = valueOf(inputReminder1);

        if (selectedMode == null) {
            Toast.makeText(this, R.string.onboarding_error_mode, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasError = false;
        if (TextUtils.isEmpty(baselineText)) {
            layoutBaseline.setError(getString(R.string.error_missing_fields));
            hasError = true;
        }
        if (TextUtils.isEmpty(cigsText)) {
            layoutCigs.setError(getString(R.string.error_missing_fields));
            hasError = true;
        }
        if (TextUtils.isEmpty(priceText)) {
            layoutPrice.setError(getString(R.string.error_missing_fields));
            hasError = true;
        }
        if (TextUtils.isEmpty(reminder1)) {
            layoutReminder1.setError(getString(R.string.error_missing_fields));
            hasError = true;
        }
        if (hasError) {
            Toast.makeText(this, R.string.error_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int baseline;
        int cigsPerPack;
        double price;
        try {
            baseline = Integer.parseInt(baselineText);
            cigsPerPack = Integer.parseInt(cigsText);
            price = Double.parseDouble(priceText);
            if (baseline <= 0 || cigsPerPack <= 0 || price <= 0) {
                throw new NumberFormatException("non-positive");
            }
        } catch (NumberFormatException exception) {
            String error = getString(R.string.onboarding_error_number);
            layoutBaseline.setError(error);
            layoutCigs.setError(error);
            layoutPrice.setError(error);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> reminders = new ArrayList<>();
        reminders.add(reminder1);
        String reminder2 = valueOf(inputReminder2);
        String reminder3 = valueOf(inputReminder3);
        if (!TextUtils.isEmpty(reminder2)) {
            reminders.add(reminder2);
        }
        if (!TextUtils.isEmpty(reminder3)) {
            reminders.add(reminder3);
        }

        QuitPlanEntity plan = new QuitPlanEntity(selectedDate, selectedMode, baseline, price, cigsPerPack, reminders);
        showSummaryAndSave(plan);
    }

    private void showSummaryAndSave(QuitPlanEntity plan) {
        String modeLabel = getModeDisplay(plan.mode);
        String reminderSummary = TextUtils.join("、", plan.reminderTimes);
        String summary = getString(R.string.onboarding_summary_message,
                dateFormat.format(plan.startDate),
                modeLabel,
                plan.dailyBaseline,
                reminderSummary);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.onboarding_summary_title)
                .setMessage(summary)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> persistPlan(plan))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void persistPlan(QuitPlanEntity plan) {
        QuitBuddyRepository.getInstance(this).savePlan(plan, () -> {
            Toast.makeText(this, R.string.msg_plan_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void clearErrors() {
        layoutBaseline.setError(null);
        layoutCigs.setError(null);
        layoutPrice.setError(null);
        layoutReminder1.setError(null);
        layoutReminder2.setError(null);
        layoutReminder3.setError(null);
        layoutStartDate.setError(null);
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String getModeDisplay(String mode) {
        if (QuitPlanEntity.MODE_GRADUAL.equals(mode)) {
            return getString(R.string.onboarding_mode_gradual);
        }
        return getString(R.string.onboarding_mode_cold_turkey);
    }
}
