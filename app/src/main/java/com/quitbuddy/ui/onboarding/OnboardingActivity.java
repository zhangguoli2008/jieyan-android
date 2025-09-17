package com.quitbuddy.ui.onboarding;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.quitbuddy.R;
import com.quitbuddy.data.model.QuitPlanEntity;
import com.quitbuddy.data.repo.QuitBuddyRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private TextInputEditText inputStartDate;
    private TextInputEditText inputBaseline;
    private TextInputEditText inputCigs;
    private TextInputEditText inputPrice;
    private TextInputEditText inputReminder1;
    private TextInputEditText inputReminder2;
    private TextInputEditText inputReminder3;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private Date selectedDate = new Date();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        bindViews();
    }

    private void bindViews() {
        inputStartDate = findViewById(R.id.inputStartDate);
        inputBaseline = findViewById(R.id.inputBaseline);
        inputCigs = findViewById(R.id.inputCigsPerPack);
        inputPrice = findViewById(R.id.inputPrice);
        inputReminder1 = findViewById(R.id.inputReminder1);
        inputReminder2 = findViewById(R.id.inputReminder2);
        inputReminder3 = findViewById(R.id.inputReminder3);

        inputStartDate.setText(dateFormat.format(selectedDate));
        View.OnClickListener dateClick = v -> showDatePicker();
        inputStartDate.setOnClickListener(dateClick);

        View.OnClickListener timeClick = this::showTimePicker;
        inputReminder1.setOnClickListener(timeClick);
        inputReminder2.setOnClickListener(timeClick);
        inputReminder3.setOnClickListener(timeClick);

        findViewById(R.id.buttonSave).setOnClickListener(v -> savePlan());
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

    private void savePlan() {
        String baselineText = inputBaseline.getText() != null ? inputBaseline.getText().toString() : "";
        String cigsText = inputCigs.getText() != null ? inputCigs.getText().toString() : "";
        String priceText = inputPrice.getText() != null ? inputPrice.getText().toString() : "";
        String reminder1 = valueOf(inputReminder1);

        if (TextUtils.isEmpty(baselineText) || TextUtils.isEmpty(cigsText) || TextUtils.isEmpty(priceText) || TextUtils.isEmpty(reminder1)) {
            Toast.makeText(this, R.string.error_missing_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        int baseline = Integer.parseInt(baselineText);
        int cigsPerPack = Integer.parseInt(cigsText);
        double price = Double.parseDouble(priceText);

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

        QuitPlanEntity plan = new QuitPlanEntity(selectedDate, "coldTurkey", baseline, price, cigsPerPack, reminders);
        QuitBuddyRepository.getInstance(this).savePlan(plan, () -> {
            Toast.makeText(this, R.string.msg_plan_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String valueOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }
}
