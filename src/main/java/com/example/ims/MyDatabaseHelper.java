package com.example.ims;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "inventoryMS.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_PRICE = "price";
    public static final String COL_STOCK = "stock";
    public static final String COL_COST = "cost";
    public static final String COL_CATEGORY = "category";


    public static final String TABLE_SALES = "sales";
    public static final String COL_SID = "id";
    public static final String COL_PID = "product_id";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT_PRICE = "unit_price";
    public static final String COL_TOTAL = "total_price";
    public static final String COL_DATE = "sold_date";


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String queryProduct = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_PRICE + " REAL NOT NULL, "
                + COL_COST + " REAL DEFAULT 0, "
                + COL_STOCK + " INTEGER NOT NULL, "
                + COL_CATEGORY + " TEXT);";

        String querySale = "CREATE TABLE " + TABLE_SALES + "("
                + COL_SID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PID + " INTEGER NOT NULL, "
                + COL_QUANTITY + " INTEGER NOT NULL, "
                + COL_UNIT_PRICE + " REAL NOT NULL, "
                + COL_TOTAL + " REAL NOT NULL, "
                + COL_DATE + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_PID + ") REFERENCES " + TABLE_PRODUCTS + "(" + COL_ID + "));";

        db.execSQL(queryProduct);
        db.execSQL(querySale);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public void addProduct(String name, double price, double cost, int stock, String category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_PRICE, price);
        cv.put(COL_COST, cost);
        cv.put(COL_STOCK, stock);
        cv.put(COL_CATEGORY, category);

        long result = db.insert(TABLE_PRODUCTS, null,cv);

        if(result == -1){
            showCustomToast("Could not add product to fox inventory_db");
        }else{
            showCustomToast("Product successfully added to fox inventory_db");
        }
    }

    public void updateProduct(int id, String name, double price, double cost, int stock, String category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_NAME, name);
        cv.put(COL_PRICE, price);
        cv.put(COL_COST, cost);
        cv.put(COL_STOCK, stock);
        cv.put(COL_CATEGORY, category);

        int result = db.update(TABLE_PRODUCTS,cv, COL_ID + "=?", new String[]{String.valueOf(id)});

        if(result == 0){
            showCustomToast("Could not update product item");
        }else{
            showCustomToast("Product successfully updated");
        }
    }

    public void deleteProduct(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_PRODUCTS,COL_ID + "=?", new String[]{String.valueOf(id)});
        if(result == 0){
            showCustomToast("Could not delete product item");
        }else{
            showCustomToast("Product item successfully deleted");
        }
    }

    public List<Product> getAllProducts(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Product> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COL_NAME, null);
        if(c.moveToFirst()){
            do{
                Product p = new Product(
                        c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_PRICE)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_COST)),
                        c.getInt(c.getColumnIndexOrThrow(COL_STOCK)),
                        c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
                list.add(p);

            }while (c.moveToNext());
        }

        c.close();
        return list;
    }

    public Product getProductById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_PRODUCTS, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Product p = null;

        if(c.moveToFirst()){
            p = new Product(
                    c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_PRICE)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_COST)),
                    c.getInt(c.getColumnIndexOrThrow(COL_STOCK)),
                    c.getString(c.getColumnIndexOrThrow(COL_CATEGORY))
            );
        }

        c.close();
        return p;
    }

    public long recordSale(int productId, int quantity, double unitPrice, String date) throws IllegalArgumentException {
        SQLiteDatabase db = this.getWritableDatabase();
        long saleID = -1;
        db.beginTransaction();
        try {
            Cursor c = db.query(TABLE_PRODUCTS,
                    new String[]{COL_STOCK, COL_NAME},
                    COL_ID + "=?",
                    new String[]{String.valueOf(productId)},
                    null, null, null);

            if (!c.moveToFirst()) {
                c.close();
                throw new IllegalArgumentException("Product not found");
            }

            int stock = c.getInt(c.getColumnIndexOrThrow(COL_STOCK));
            String prodName = c.getString(c.getColumnIndexOrThrow(COL_NAME));
            c.close();

            if (stock <= 0) {
                showCustomToast("Sale blocked! " + prodName + " is out of stock.");
                return -1; // no sale recorded
            }

            if (stock < quantity) {
                showCustomToast("Insufficient stock! Only " + stock + " left for " + prodName);
                return -1; // block sale
            }

            ContentValues sale = new ContentValues();
            sale.put(COL_PID, productId);
            sale.put(COL_QUANTITY, quantity);
            sale.put(COL_UNIT_PRICE, unitPrice);
            sale.put(COL_TOTAL, unitPrice * quantity);
            sale.put(COL_DATE, date);
            saleID = db.insertOrThrow(TABLE_SALES, null, sale);

            ContentValues updateStock = new ContentValues();
            updateStock.put(COL_STOCK, stock - quantity);
            db.update(TABLE_PRODUCTS, updateStock, COL_ID + "=?", new String[]{String.valueOf(productId)});

            db.setTransactionSuccessful();

            if (stock - quantity <= 5) {
                showCustomToast("Warning: Low stock for " + prodName + " (" + (stock - quantity) + " left)");
            }

        } finally {
            db.endTransaction();
        }

        return saleID;
    }


    public void deleteSale(int saleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SALES, COL_SID + "=?", new String[]{String.valueOf(saleId)});

        if (result == 0) {
            showCustomToast("Could not delete sale");
        } else {
            showCustomToast("Sale successfully deleted");
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT s.*, p." + COL_NAME + " as product_name, p." + COL_COST + " as product_cost " +
                "FROM " + TABLE_SALES + " s " +
                "LEFT JOIN " + TABLE_PRODUCTS + " p ON s." + COL_PID + " = p." + COL_ID +
                " ORDER BY s." + COL_SID + " DESC";

        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                Sale s = new Sale(
                        c.getInt(c.getColumnIndexOrThrow(COL_SID)),
                        c.getInt(c.getColumnIndexOrThrow(COL_PID)),
                        c.getString(c.getColumnIndexOrThrow("product_name")),
                        c.getInt(c.getColumnIndexOrThrow(COL_QUANTITY)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_UNIT_PRICE)),
                        c.getDouble(c.getColumnIndexOrThrow(COL_TOTAL)),
                        c.getString(c.getColumnIndexOrThrow(COL_DATE)),
                        c.getDouble(c.getColumnIndexOrThrow("product_cost"))
                );
                list.add(s);
            } while (c.moveToNext());
        }

        c.close();
        return list;
    }

    public double getTotalSales(String startDate, String endDate) {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_TOTAL + ") FROM " + TABLE_SALES +
                        " WHERE " + COL_DATE + " BETWEEN ? AND ?",
                new String[]{startDate, endDate});
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    public double getProfit(String startDate, String endDate) {
        double profit = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM((s." + COL_UNIT_PRICE + " - p." + COL_COST + ") * s." + COL_QUANTITY + ") " +
                        "FROM " + TABLE_SALES + " s " +
                        "JOIN " + TABLE_PRODUCTS + " p ON s." + COL_PID + " = p." + COL_ID +
                        " WHERE s." + COL_DATE + " BETWEEN ? AND ?",
                new String[]{startDate, endDate});
        if (c.moveToFirst()) {
            profit = c.getDouble(0);
        }
        c.close();
        return profit;
    }

    public double getInventoryValuation() {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_STOCK + " * " + COL_COST + ") FROM " + TABLE_PRODUCTS, null);
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    public double getStockValueAtSellingPrice() {
        double total = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT SUM(" + COL_STOCK + " * " + COL_PRICE + ") FROM " + TABLE_PRODUCTS, null);
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    public void showCustomToast(String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.fox_inventory_toast, null);

        ImageView icon = layout.findViewById(R.id.fox_toast_icon);
        icon.setImageResource(R.drawable.fox_logo_inset);

        TextView text = layout.findViewById(R.id.fox_toast_text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void exportCSV(Context context, String filename, double totalSales, double totalProfit, double invVal, double stockVal, String reportType, String currency) {
        SQLiteDatabase db = getReadableDatabase();

        if (!filename.endsWith(".csv")) {
            filename += ".csv";
        }

        Uri fileUri = null;
        OutputStream outputStream = null;

        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (fileUri != null) {
                    outputStream = resolver.openOutputStream(fileUri);
                }
            } else {
                File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!exportDir.exists()) exportDir.mkdirs();
                File file = new File(exportDir, filename);
                fileUri = Uri.fromFile(file);
                outputStream = new FileOutputStream(file);
            }

            if (outputStream == null) {
                Toast.makeText(context, "Failed to create file", Toast.LENGTH_LONG).show();
                return;
            }

            Writer writer = new OutputStreamWriter(outputStream);

            writer.write("$$$ REPORT SUMMARY (" + reportType + ") $$$\n");
            writer.write("Metric,Value\n");
            writer.write("Total Sales (" + reportType + ")," + currency + " " + totalSales + "\n");
            writer.write("Profit (" + reportType + ")," + currency + " " + totalProfit + "\n");
            writer.write("Inventory Valuation (Cost)," + currency + " " + invVal + "\n");
            writer.write("Stock Value (Selling Price)," + currency + " " + stockVal + "\n");
            writer.write("\n\n");

            Cursor cursorProducts = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
            writer.write("=== PRODUCTS TABLE ===\n");

            for (int i = 0; i < cursorProducts.getColumnCount(); i++) {
                writer.write(cursorProducts.getColumnName(i));
                if (i < cursorProducts.getColumnCount() - 1) writer.write(",");
            }
            writer.write("\n");

            while (cursorProducts.moveToNext()) {
                for (int i = 0; i < cursorProducts.getColumnCount(); i++) {
                    String value = cursorProducts.getString(i);
                    writer.write(value != null ? value : "");
                    if (i < cursorProducts.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");
            }
            cursorProducts.close();

            writer.write("\n\n");

            Cursor cursorSales = db.rawQuery("SELECT * FROM " + TABLE_SALES, null);
            writer.write("=== SALES TABLE ===\n");

            for (int i = 0; i < cursorSales.getColumnCount(); i++) {
                writer.write(cursorSales.getColumnName(i));
                if (i < cursorSales.getColumnCount() - 1) writer.write(",");
            }
            writer.write("\n");

            while (cursorSales.moveToNext()) {
                for (int i = 0; i < cursorSales.getColumnCount(); i++) {
                    String value = cursorSales.getString(i);
                    writer.write(value != null ? value : "");
                    if (i < cursorSales.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");
            }
            cursorSales.close();

            writer.flush();
            writer.close();

            showCustomToast("SUCCESSFUL:\n Fox_inventory_db and reports exported to CSV");

        } catch (Exception e) {
            e.printStackTrace();
            showCustomToast("FAILED:\n Error could not export Fox_inventory_db and reports to CSV");
        }
    }




}

class Product {
    private int id;
    private String name;
    private double price;
    private double cost;
    private int stock;
    private String category;

    public Product(int id, String name, double price, double cost, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.category = category;
    }

    public Product(String name, double price, int stock, String category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

class Sale{
    private int id;
    private int productId;
    private String prodName;
    private int quantity;
    private double unitPrice;
    private double total;
    private String date;
    private double prodCost;


    public Sale(int id, int productId, String productName, int quantity, double unitPrice, double total, String date, double productCost) {
        this.id = id;
        this.productId = productId;
        this.prodName = productName;
        this.quantity = quantity; this.unitPrice = unitPrice;
        this.total = total;
        this.date = date;
        this.prodCost = productCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getProdCost() {
        return prodCost;
    }

    public void setProdCost(double prodCost) {
        this.prodCost = prodCost;
    }
}

