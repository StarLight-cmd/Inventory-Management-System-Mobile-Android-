package com.example.ims;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {
    // Gui components
    private BottomNavigationView fox_bottom_Nav;
    private RecyclerView rcInventory;
    private ProductAdapter productAdapter;
    private MyDatabaseHelper dbHelper;
    private EditText etProductName, etSellingPrice, etProductStock,etProductId, etCostPrice;
    private Spinner spCategory;
    private Button btnAddProduct,btnDeleteProduct, btnUpdateProduct ;
    private ProgressBar pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // referencing db and components
        dbHelper = new MyDatabaseHelper(this);
        rcInventory = findViewById(R.id.rcInventory);
        rcInventory.setLayoutManager(new LinearLayoutManager(this));

        etProductName = findViewById(R.id.etProductName);
        etSellingPrice = findViewById(R.id.etSellingPrice);
        etProductStock = findViewById(R.id.etProductStock);
        spCategory = findViewById(R.id.spCategory);
        etProductId = findViewById(R.id.etProductId);
        etCostPrice = findViewById(R.id.etCostPrice);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnDeleteProduct = findViewById(R.id.btnDelProduct);
        btnUpdateProduct = findViewById(R.id.btnUpProduct);

        // creating array and array adpater for spinner
        String[] categories = {"Electronics", "Clothing", "Groceries", "Furniture", "Miscellaneous", "Sports"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        pg = findViewById(R.id.pgInventory);

        // loading inventory list
        loadInventory();

        // add product to fox db
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                builder.setTitle("Confirm Product Addition");
                builder.setMessage("Are you sure you want to add this product?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String Pname = etProductName.getText().toString();
                        double Pprice = Double.parseDouble(etSellingPrice.getText().toString());
                        double Pcost = Double.parseDouble(etCostPrice.getText().toString());
                        int Pstock = Integer.parseInt(etProductStock.getText().toString());
                        String Pcategory = spCategory.getSelectedItem().toString();

                        dbHelper.addProduct(Pname, Pprice, Pcost, Pstock, Pcategory);
                        loadInventory();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });


        // update product in db
        btnUpdateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                builder.setTitle("Confirm Product Update");
                builder.setMessage("Are you sure you want to update this product?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int Pid = Integer.parseInt(etProductId.getText().toString());
                        String Pname = etProductName.getText().toString();
                        double Pprice = Double.parseDouble(etSellingPrice.getText().toString());
                        int Pstock = Integer.parseInt(etProductStock.getText().toString());
                        double Pcost = Double.parseDouble(etCostPrice.getText().toString());
                        String Pcategory = spCategory.getSelectedItem().toString();
                        dbHelper.updateProduct(Pid, Pname, Pprice, Pcost, Pstock, Pcategory);
                        loadInventory();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });


        // button to delete product from inventory
        btnDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                builder.setTitle("Confirm Product Deletion");
                builder.setMessage("Are you sure you want to delete this product?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int Pid = Integer.parseInt(etProductId.getText().toString());
                        dbHelper.deleteProduct(Pid);
                        loadInventory();
                    }
                });
                builder.setNegativeButton("No", null);
                builder.show();
            }
        });


        // bottom navigation
        fox_bottom_Nav = findViewById(R.id.fox_bottom_nav);
        fox_bottom_Nav.setSelectedItemId(R.id.nav_inventory);

        fox_bottom_Nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
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
            } else if (id == R.id.nav_inventory) {
                return true;
            }
            return false;
        });
    }

    private void loadInventory() {
        pg.setVisibility(View.VISIBLE);

        rcInventory.postDelayed(() -> {
            List<Product> products = dbHelper.getAllProducts();
            productAdapter = new ProductAdapter(products);
            rcInventory.setAdapter(productAdapter);

            pg.setVisibility(View.GONE);
        }, 500);
    }
}
