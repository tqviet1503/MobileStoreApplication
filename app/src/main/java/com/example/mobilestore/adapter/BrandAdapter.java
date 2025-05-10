package com.example.mobilestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.R;
import com.example.mobilestore.model.Brand;

import java.util.List;

//this class will apply for all recycler view in admin management

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {
    private List<Brand> brands;
    private OnBrandClickListener listener;

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
        void onAddPhoneClick(Brand brand);
    }

    public BrandAdapter(List<Brand> brands, OnBrandClickListener listener) {
        this.brands = brands;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brands.get(position);
        holder.bind(brand, listener);
    }

    @Override
    public int getItemCount() {
        return brands != null ? brands.size() : 0;
    }

    public void updateBrands(List<Brand> newBrands) {
        this.brands = newBrands;
        notifyDataSetChanged();
    }

    static class BrandViewHolder extends RecyclerView.ViewHolder {
        private ImageView logoImage;
        private TextView nameText;
        private TextView countText;
        private ImageButton detailButton;
        private LinearLayout brandPhone;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            logoImage = itemView.findViewById(R.id.brand_logo);
            nameText = itemView.findViewById(R.id.brand_name);
            countText = itemView.findViewById(R.id.phone_count);
            detailButton = itemView.findViewById(R.id.btn_add_detail);
            brandPhone = itemView.findViewById(R.id.brand_phone);
        }

        public void bind(Brand brand, OnBrandClickListener listener) {
            logoImage.setImageResource(brand.getLogoResource());
            nameText.setText(brand.getName());
            countText.setText(String.format("%d phones", brand.getPhoneCount()));

            detailButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddPhoneClick(brand);
                }
            });

            brandPhone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBrandClick(brand);
                }
            });
        }
    }
}