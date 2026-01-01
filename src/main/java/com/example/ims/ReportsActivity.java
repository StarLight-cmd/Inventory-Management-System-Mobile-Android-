package com.example.ims;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.github.mikephil.charting.charts.BarChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    private BottomNavigationView fox_bottom_Nav;
    private MyDatabaseHelper dbHelper;
    private TextView tvSales, tvProfit, tvInventoryVal, tvStockVal;
    private PieChart pcSalesChart;
    private BarChart bcSales;
    private Button btnExportCSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("FOX_IMS_PREFS", MODE_PRIVATE);
        String currency = prefs.getString("currency", "R");
        String reportType = prefs.getString("report_type", "Daily");

        dbHelper = new MyDatabaseHelper(this);

        tvSales = findViewById(R.id.tvSalesRep);
        tvProfit = findViewById(R.id.tvProfit);
        tvInventoryVal = findViewById(R.id.tvInventoryVal);
        tvStockVal = findViewById(R.id.tvStockVal);

        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
        double totalSales;
        double totalProfit;

        if (reportType.equals("Weekly")) {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

            String weekStart = sdf.format(calendar.getTime());
            String weekEnd = sdf.format(new Date());

            totalSales = dbHelper.getTotalSales(weekStart, weekEnd);
            totalProfit = dbHelper.getProfit(weekStart, weekEnd);
        } else {
            totalSales = dbHelper.getTotalSales(today, today);
            totalProfit = dbHelper.getProfit(today, today);
        }

        double invVal = dbHelper.getInventoryValuation();
        double stockVal = dbHelper.getStockValueAtSellingPrice();

        tvSales.setText("Total Sales (" + reportType + "): " + currency + " " + totalSales);
        tvProfit.setText("Profit (" + reportType + "): " + currency + " " + totalProfit);
        tvInventoryVal.setText("Inventory Valuation (Cost): " + currency + " " + invVal);
        tvStockVal.setText("Stock Value (Selling Price): " + currency + " " + stockVal);

        pcSalesChart = findViewById(R.id.pcSalesChart);
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) totalSales, reportType));

        PieDataSet dataSet = new PieDataSet(entries, "Fox Sales Report (" + reportType + ")");
        dataSet.setColors(new int[]{Color.parseColor("#FF9800")});
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pcSalesChart.setData(data);
        pcSalesChart.invalidate();

        bcSales = findViewById(R.id.bcSalesChart);
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, (float) totalSales));
        barEntries.add(new BarEntry(1, (float) totalProfit));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Fox Sales vs Profit");
        barDataSet.setColors(new int[]{R.color.orange, R.color.purple_1}, this);

        BarData barData = new BarData(barDataSet);
        bcSales.setData(barData);

        final String[] labels = new String[]{"Sales", "Profit"};
        bcSales.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                } else {
                    return "";
                }
            }
        });

        bcSales.getDescription().setEnabled(false);
        bcSales.animateY(1000);
        bcSales.invalidate();

        btnExportCSV = findViewById(R.id.btnExportCsv);
        btnExportCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = "report_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                        ".csv";

                dbHelper.exportCSV(ReportsActivity.this, filename,totalSales,totalProfit,invVal,stockVal,reportType,currency);
            }
        });

        fox_bottom_Nav = findViewById(R.id.fox_bottom_nav);
        fox_bottom_Nav.setSelectedItemId(R.id.nav_reports);

        fox_bottom_Nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(getApplicationContext(), InventoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_sales) {
                startActivity(new Intent(getApplicationContext(), SalesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_reports) {
                return true;
            }
            return false;
        });
    }
}
