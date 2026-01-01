package com.example.ims;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {

    private List<Sale> sales;

    public SalesAdapter(List<Sale> salesList) {
        this.sales = salesList;
    }

    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sale_item, parent, false);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        Sale sale = sales.get(position);


        holder.tvSaleId.setText("Sale ID: " + sale.getId());
        holder.tvProductId.setText("Product ID: " + sale.getProductId());
        holder.tvProductName.setText("Name: " + sale.getProdName());
        holder.tvQuantity.setText("Quantity: " + sale.getQuantity());
        holder.tvUnitPrice.setText("Unit Price: R" + sale.getUnitPrice());
        holder.tvTotal.setText("Total: R" + sale.getTotal());
        holder.tvCost.setText("Cost: R" + sale.getProdCost());
        holder.tvDate.setText("Date: " + sale.getDate());
    }

    @Override
    public int getItemCount() {

        return sales.size();
    }

    public static class SalesViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleId, tvProductId, tvProductName, tvQuantity, tvUnitPrice, tvTotal, tvCost, tvDate;

        public SalesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleId = itemView.findViewById(R.id.tvSaleId);
            tvProductId = itemView.findViewById(R.id.tvProductId);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
