package com.example.ims;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class SalesActivity extends AppCompatActivity {
    private BottomNavigationView fox_bottom_Nav;
    private MyDatabaseHelper dbHelper;
    private ProgressBar pg;
    private EditText etProductId, etQuantity, etDate, etSaleID;
    private Button btnRecordSale, btnDeleteSale;
    private RecyclerView salesRecyclerView;
    private SalesAdapter salesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        dbHelper = new MyDatabaseHelper(this);

        etProductId = findViewById(R.id.etProductId);
        etQuantity = findViewById(R.id.etQuantity);
        etDate = findViewById(R.id.etDate);
        etSaleID = findViewById(R.id.etsaleId);
        btnRecordSale = findViewById(R.id.btnRecordSale);
        btnDeleteSale = findViewById(R.id.btnDeleteSale);
        pg = findViewById(R.id.pgSalesBar);
        salesRecyclerView = findViewById(R.id.rvSales);
        salesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        load_Fox_INV_Sales();

        btnRecordSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etProductId.getText().toString().isEmpty() || etQuantity.getText().toString().isEmpty()) {
                    dbHelper.showCustomToast("Please enter Product ID and Quantity");
                    return;
                }

                final int productId = Integer.parseInt(etProductId.getText().toString());
                final int quantity = Integer.parseInt(etQuantity.getText().toString());

                final Product product = dbHelper.getProductById(productId);

                if (product == null) {
                    dbHelper.showCustomToast("Product ID not found");
                    return;
                }

                final double unitPrice = product.getPrice();
                final String date = etDate.getText().toString();

                if(date.isEmpty()){
                    dbHelper.showCustomToast("Please enter valid date");
                    return;
                }

                new AlertDialog.Builder(SalesActivity.this)
                        .setTitle("Confirm Product Sale")
                        .setMessage("Do you want to record this sale?\n\nProduct: " + product.getName() +
                                "\nQuantity: " + quantity +
                                "\nUnit Price: " + unitPrice +
                                "\nDate: " + date)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    dbHelper.recordSale(productId, quantity, unitPrice, date);
                                    load_Fox_INV_Sales();
                                    dbHelper.showCustomToast("Sale has been recorded");
                                } catch (IllegalArgumentException e) {
                                    dbHelper.showCustomToast(e.getMessage());
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        SalesActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                etDate.setText(date);
                            }
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        btnDeleteSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int saleID = Integer.parseInt(etSaleID.getText().toString().trim());
                dbHelper.deleteSale(saleID);
                load_Fox_INV_Sales();
            }
        });

        fox_bottom_Nav = findViewById(R.id.fox_bottom_nav);
        fox_bottom_Nav.setSelectedItemId(R.id.nav_sales);

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
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(getApplicationContext(), ReportsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_sales) {
                return true;
            }
            return false;
        });
    }

    private void load_Fox_INV_Sales() {
        pg.setVisibility(View.VISIBLE);

        salesRecyclerView.postDelayed(() -> {
            List<Sale> sales = dbHelper.getAllSales();
            salesAdapter = new SalesAdapter(sales);
            salesRecyclerView.setAdapter(salesAdapter);

            pg.setVisibility(View.GONE);
        }, 500);
    }


}
