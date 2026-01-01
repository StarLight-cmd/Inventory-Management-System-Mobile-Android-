package com.example.ims;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    // Declaring dashboard variables
    private MyDatabaseHelper db;
    private BottomNavigationView fox_bottom_Nav;
    private EditText etCurr;
    private Spinner spReportType;
    private Button btnSaveUserPref;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intializing database and GUI components
        db = new MyDatabaseHelper(this);

        etCurr = findViewById(R.id.etCurrency);
        spReportType = findViewById(R.id.spReportType);
        btnSaveUserPref = findViewById(R.id.btnSaveUserPref);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Daily", "Weekly"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReportType.setAdapter(adapter);

        // Getting saved user preferences
        SharedPreferences prefs = getSharedPreferences("FOX_IMS_PREFS", MODE_PRIVATE);
        String savedCurrency = prefs.getString("currency", "R");
        String reportType = prefs.getString("report_type", "Daily");

        etCurr.setText(savedCurrency);

        if (reportType.equals("Weekly")) {
            spReportType.setSelection(1);
        } else {
            spReportType.setSelection(0);
        }

        // Setting user preferences
        btnSaveUserPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String setCurr = etCurr.getText().toString().trim();
                String setReportType = spReportType.getSelectedItem().toString();

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("currency", setCurr);
                editor.putString("report_type", setReportType);
                editor.apply();

                db.showCustomToast("fox management system: User preferences saved.");
            }
        });

        // Configuring bottom navigation
        fox_bottom_Nav = findViewById(R.id.fox_bottom_nav);
        fox_bottom_Nav.setSelectedItemId(R.id.nav_dashboard);
        fox_bottom_Nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_inventory) {
                startActivity(new Intent(getApplicationContext(), InventoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_sales) {
                startActivity(new Intent(getApplicationContext(), SalesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(getApplicationContext(), ReportsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_dashboard) {
                return true;
            }
            return false;
        });
    }
}