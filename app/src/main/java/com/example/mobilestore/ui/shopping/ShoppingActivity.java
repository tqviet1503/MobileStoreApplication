package com.example.mobilestore.ui.shopping;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilestore.data.repository.ProductRepository;
import com.example.mobilestore.model.Brand;
import com.example.mobilestore.model.Phone;
import com.example.mobilestore.ui.admin_storage.StorageActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.product_detail.ProductActivity;
import com.example.mobilestore.ui.payment.PaymentActivity;
import com.example.mobilestore.ui.customer_profile.ProfileActivity;
import com.example.mobilestore.data.cart.Cart;
import com.example.mobilestore.ui.cart.CartActivity;

import java.util.ArrayList;
import java.util.List;

public class ShoppingActivity extends AppCompatActivity implements ProductRepository.OnDataChangeListener {

    // UI components
    private TextView titleTextView;
    private ImageView avatarImageView, searchIcon;
    private EditText searchEditText;
    private CardView searchCardView, navBarCardView;
    private Button btnProducts, btnShop, btnPayment, btnProfile;
    private ChipGroup categoryChipGroup;
    private RecyclerView productRecyclerView;

    // Data
    private List<Phone> allPhones;
    private List<Phone> filteredPhones;
    private ProductRepository repository;
    private ProductAdapter productAdapter;
    private String currentCategory = "All";  // Current category
    private String currentSearchQuery = "";  // Current search query

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_shopping);
            initializeViews();
            setupNavigationButtons();
            repository = ProductRepository.getInstance(this);
            repository.addListener(this);
            setupRecyclerView();
            generateDynamicCategoryChips();
            setupSearch();
            setupAvatarButton();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing shopping screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repository != null) {
            repository.removeListener(this);
        }
    }

    private void initializeViews() {
        try {
            titleTextView = findViewById(R.id.titleTextView);
            avatarImageView = findViewById(R.id.avatarImageView);
            searchIcon = findViewById(R.id.searchIcon);
            searchEditText = findViewById(R.id.searchEditText);
            searchCardView = findViewById(R.id.searchCardView);
            navBarCardView = findViewById(R.id.navBarCardView);
            btnProducts = findViewById(R.id.btnProducts);
            btnShop = findViewById(R.id.btnShop);
            btnPayment = findViewById(R.id.btnPayment);
            btnProfile = findViewById(R.id.btnProfile);
            categoryChipGroup = findViewById(R.id.categoryChipGroup);
            productRecyclerView = findViewById(R.id.productRecyclerView);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        try {
            // Use phone
            allPhones = new ArrayList<>();
            filteredPhones = new ArrayList<>();

            // Check if drawable exists
            int phoneImageResource;
            try {
                phoneImageResource = R.drawable.phone_sample;
                getResources().getDrawable(phoneImageResource);
            } catch (Resources.NotFoundException e) {
                // Fallback to a system drawable if custom one is missing
                phoneImageResource = android.R.drawable.ic_menu_gallery;
            }

            // Get brands from database
            List<String> brands = getAllBrandNames();
            for (String brand : brands) {
                allPhones.addAll(repository.getPhonesForBrand(brand));
            }

            // Filter
            filteredPhones.addAll(allPhones);

            // Set up the RecyclerView
            if (productRecyclerView != null) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                productRecyclerView.setLayoutManager(gridLayoutManager);
                try {
                    int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
                    productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
                } catch (Resources.NotFoundException e) {
                    int spacingInPixels = (int)(8 * getResources().getDisplayMetrics().density);
                    productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));
                }

                // Create and set the adapter with the item_phone layout
                productAdapter = new ProductAdapter(filteredPhones, this);
                productRecyclerView.setAdapter(productAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> getAllBrandNames() {
        List<String> brandNames = new ArrayList<>();
        for (Brand brand : repository.getAllBrands()) {
            brandNames.add(brand.getName());
        }
        return brandNames;
    }

    private void setupNavigationButtons() {
        try {
            if (btnProducts != null) {
                btnProducts.setOnClickListener(view -> {
                });
            }

            if (btnShop != null) {
                btnShop.setOnClickListener(view -> {
                    simulateCartNavigation();
                });
            }

            if (btnPayment != null) {
                btnPayment.setOnClickListener(view -> {
                    Intent intent = new Intent(ShoppingActivity.this, PaymentActivity.class);
                    startActivity(intent);
                });
            }

            if (btnProfile != null) {
                btnProfile.setOnClickListener(view -> {
                    Intent intent = new Intent(ShoppingActivity.this, ProfileActivity.class);
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Generate category chips dynamically based on available brands
     * Safe version that handles missing resources
     */
    private void generateDynamicCategoryChips() {
        try {
            if (categoryChipGroup == null) {
                Toast.makeText(this, "Category chip group not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Clear existing chips
            categoryChipGroup.removeAllViews();

            // Create the "All" chip first with the styling from the layout
            Chip chipAll = new Chip(this);
            chipAll.setText("All");
            chipAll.setCheckable(true);
            chipAll.setChecked(true);

            // Set chip colors safely
            try {
                chipAll.setChipBackgroundColorResource(R.color.colorPrimary);
            } catch (Resources.NotFoundException e) {
                chipAll.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            }
            chipAll.setTextColor(getResources().getColor(android.R.color.white));
            chipAll.setTextSize(14);
            chipAll.setTypeface(Typeface.DEFAULT_BOLD);
            try {
                chipAll.setMinHeight(getResources().getDimensionPixelSize(R.dimen.chip_min_height));
            } catch (Resources.NotFoundException e) {
                chipAll.setMinHeight((int)(40 * getResources().getDisplayMetrics().density));
            }

            chipAll.setOnClickListener(view -> {
                currentCategory = "All";
                applyFilters();
                updateChipSelection(chipAll);
            });

            categoryChipGroup.addView(chipAll);

            // Get all brands and create chips for each
            List<Brand> brands = repository.getAllBrands();
            for (Brand brand : brands) {
                Chip chip = new Chip(this);
                chip.setText(brand.getName());
                chip.setCheckable(true);

                // Set chip colors and dimensions safely
                try {
                    chip.setChipBackgroundColorResource(R.color.chipBackground);
                } catch (Resources.NotFoundException e) {
                    chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFF0F8FF));
                }

                try {
                    chip.setTextColor(getResources().getColor(R.color.colorPrimary));
                } catch (Resources.NotFoundException e) {
                    chip.setTextColor(0xFF2196F3);
                }

                // Set chip stroke width and color safely
                try {
                    chip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
                } catch (Resources.NotFoundException e) {
                    chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
                }

                try {
                    chip.setChipStrokeColorResource(R.color.colorPrimary);
                } catch (Resources.NotFoundException e) {
                    chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(0xFF2196F3));
                }

                // Set min height safely
                try {
                    chip.setMinHeight(getResources().getDimensionPixelSize(R.dimen.chip_min_height));
                } catch (Resources.NotFoundException e) {
                    chip.setMinHeight((int)(40 * getResources().getDisplayMetrics().density));
                }

                // Set chip icon if logo resource exists
                if (brand.getLogoResource() != 0) {
                    try {
                        chip.setChipIconResource(brand.getLogoResource());
                        chip.setChipIconVisible(true);

                        // Use direct padding or check for method availability
                        try {
                            chip.setIconEndPadding(8f);
                        } catch (NoSuchMethodError methodError) {
                            chip.setPadding(8, 0, 8, 0);
                        }
                    } catch (Resources.NotFoundException e) {
                        chip.setChipIconVisible(false);
                    }
                }

                final String brandName = brand.getName();
                chip.setOnClickListener(view -> {
                    currentCategory = brandName;
                    applyFilters();
                    updateChipSelection(chip);
                });

                categoryChipGroup.addView(chip);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up category filters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateChipSelection(Chip selectedChip) {
        // Reset all chips to unselected state
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            View chipView = categoryChipGroup.getChildAt(i);
            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                if (chip == selectedChip) {
                    try {
                        chip.setChipBackgroundColorResource(R.color.colorPrimary);
                    } catch (Resources.NotFoundException e) {
                        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFF2196F3));
                    }
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    try {
                        chip.setChipBackgroundColorResource(R.color.chipBackground);
                    } catch (Resources.NotFoundException e) {
                        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFF0F8FF));
                    }

                    try {
                        chip.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } catch (Resources.NotFoundException e) {
                        chip.setTextColor(0xFF2196F3);
                    }
                }
            }
        }
    }

    private void applyFilters() {
        filteredPhones.clear();
        for (Phone phone : allPhones) {
            boolean matchesCategory = currentCategory.equals("All") || phone.getBrand().equals(currentCategory);
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    phone.getPhoneName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    phone.getBrand().toLowerCase().contains(currentSearchQuery.toLowerCase());

            if (matchesCategory && matchesSearch) {
                filteredPhones.add(phone);
            }
        }

        if (filteredPhones.isEmpty() && !allPhones.isEmpty()) {
            Toast.makeText(this, "No phones found", Toast.LENGTH_SHORT).show();
        }

        productAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        try {
            // Set up search functionality
            if (searchEditText != null) {
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Update search query and apply filters
                        currentSearchQuery = s.toString().trim();
                        if (currentSearchQuery.length() >= 1 || currentSearchQuery.isEmpty()) {
                            applyFilters();
                        }
                    }
                });

                // Handle when user presses Enter on keyboard
                searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        // Apply search when user presses Enter
                        applyFilters();
                        return true;
                    }
                    return false;
                });

                // Set up search icon click listener for explicit search
                if (searchIcon != null) {
                    searchIcon.setOnClickListener(v -> {
                        currentSearchQuery = searchEditText.getText().toString().trim();
                        applyFilters();
                    });
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupAvatarButton() {
        if (avatarImageView != null) {
            avatarImageView.setOnClickListener(v -> {
                Intent intent = new Intent(ShoppingActivity.this, StorageActivity.class);
                startActivity(intent);
            });
        }
    }

    //Random star
    private float getDeterministicRating(String productId) {
        int hashCode = productId.hashCode();
        int baseNumber = Math.abs(hashCode % 11);
        float rating = 4.0f + (baseNumber / 10.0f); //4.0 to 5.0
        rating = Math.max(4.0f, Math.min(5.0f, rating));
        return Math.round(rating * 10) / 10.0f;
    }

    public void refreshData() {
        try {
            // Refresh phone data
            allPhones.clear();
            List<Brand> brands = repository.getAllBrands();
            for (Brand brand : brands) {
                allPhones.addAll(repository.getPhonesForBrand(brand.getName()));
            }

            // Regenerate category chips
            generateDynamicCategoryChips();

            // Apply current filters
            applyFilters();

            // Update adapter
            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error refreshing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onBrandAdded(Brand brand) {
        runOnUiThread(this::refreshData);
    }

    @Override
    public void onPhoneAdded(Phone phone) {
        runOnUiThread(this::refreshData);
    }

    @Override
    public void onDataChanged() {};

    // Adapter class for RecyclerView
    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Phone> products;
        private ShoppingActivity context;

        public ProductAdapter(List<Phone> products, ShoppingActivity context) {
            this.products = products;
            this.context = context;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            try {
                // Inflate the item_phone.xml layout
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phone, parent, false);
                return new ProductViewHolder(view);
            } catch (Resources.NotFoundException e) {
                Toast.makeText(context, "Product layout not found. Using default layout.", Toast.LENGTH_SHORT).show();

                CardView cardView = new CardView(parent.getContext());
                cardView.setLayoutParams(new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.WRAP_CONTENT));
                cardView.setCardElevation(8);
                cardView.setRadius(12);
                cardView.setContentPadding(16, 16, 16, 16);

                TextView textView = new TextView(parent.getContext());
                textView.setLayoutParams(new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.WRAP_CONTENT));
                textView.setId(View.generateViewId());
                cardView.addView(textView);

                return new ProductViewHolder(cardView);
            }
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            try {
                Phone product = products.get(position);

                // Kiểm tra tình trạng tồn kho
                boolean isOutOfStock = (product.getStockQuantity() <= 0);

                // Display product information with the layout
                if (holder.tvPhoneName != null) {
                    holder.tvPhoneName.setText(product.getPhoneName());
                }

                if (holder.tvPrice != null) {
                    holder.tvPrice.setText(String.format("%,.0fđ", product.getPrice()));
                }

                // Display brand from product category
                if (holder.tvBrand != null) {
                    holder.tvBrand.setText(product.getBrand());
                }

                // Set product image based on brand
                if (holder.imgPhone != null) {
                    String brand = product.getBrand().toLowerCase();
                    try {
                        if (brand.contains("apple") || brand.contains("iphone")) {
                            holder.imgPhone.setImageResource(R.drawable.phone_sample);
                        } else if (brand.contains("samsung") || brand.contains("galaxy")) {
                            holder.imgPhone.setImageResource(R.drawable.samsung);
                        } else if (brand.contains("redmi") || brand.contains("xiaomi")) {
                            holder.imgPhone.setImageResource(R.drawable.redmi);
                        } else if (brand.contains("oppo")) {
                            holder.imgPhone.setImageResource(R.drawable.oppo);
                        } else {
                            holder.imgPhone.setImageResource(R.drawable.default_phone);
                        }
                    } catch (Resources.NotFoundException e) {
                        holder.imgPhone.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }

                // Hiển thị overlay và text SOLD OUT khi hết hàng
                if (holder.overlayView != null) {
                    holder.overlayView.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
                }

                if (holder.tvSoldOutOverlay != null) {
                    holder.tvSoldOutOverlay.setVisibility(isOutOfStock ? View.VISIBLE : View.GONE);
                }

                // Hiển thị trạng thái tồn kho
                if (holder.tvStockStatus != null) {
                    if (isOutOfStock) {
                        holder.tvStockStatus.setText("Out of Stock");
                        holder.tvStockStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        holder.tvStockStatus.setText("In Stock");
                        holder.tvStockStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    }
                }

                // Random star
                if (holder.ratingBar != null) {
                    float rating = getDeterministicRating(product.getId());
                    holder.ratingBar.setRating(rating);
                    if (holder.tvRatingValue != null) {
                        holder.tvRatingValue.setText(String.format("%.1f", rating));
                    }
                }

                // Hiển thị badge "NEW" chỉ khi sản phẩm còn hàng
                if (holder.tvBadge != null) {
                    if (position % 3 == 0 && !isOutOfStock) {
                        holder.tvBadge.setVisibility(View.VISIBLE);
                        holder.tvBadge.setText("NEW");
                        try {
                            holder.tvBadge.setBackgroundResource(R.drawable.badge_background);
                        } catch (Resources.NotFoundException e) {
                            holder.tvBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                        }
                    } else {
                        holder.tvBadge.setVisibility(View.GONE);
                    }
                }

                // Set up event for favorite button
                if (holder.btnFavorite != null) {
                    holder.btnFavorite.setOnClickListener(v -> {
                        boolean isFavorite = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();
                        if (isFavorite) {
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                            holder.btnFavorite.setTag(false);
                            Toast.makeText(context, "Removed " + product.getPhoneName() + " from favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
                            holder.btnFavorite.setTag(true);
                            Toast.makeText(context, "Added " + product.getPhoneName() + " to favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                    holder.btnFavorite.setTag(false);
                }

                // Set up event for add to cart button
                if (holder.btnAddToCart != null) {
                    // Vô hiệu hóa nút "Add to Cart" nếu sản phẩm hết hàng
                    holder.btnAddToCart.setEnabled(!isOutOfStock);
                    holder.btnAddToCart.setAlpha(isOutOfStock ? 0.5f : 1.0f);

                    holder.btnAddToCart.setOnClickListener(v -> {
                        if (isOutOfStock) {
                            Toast.makeText(context, product.getPhoneName() + " is currently out of stock", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                Cart.getInstance(context).addItem(product, 1);
                                Toast.makeText(context, "Added " + product.getPhoneName() + " to cart", Toast.LENGTH_SHORT).show();

                                // Create Intent with full product information
                                Intent intent = new Intent(context, ProductActivity.class);
                                intent.putExtra("PRODUCT_NAME", product.getPhoneName());
                                intent.putExtra("PRODUCT_PRICE", String.format("%,.0fđ", product.getPrice()));
                                intent.putExtra("FROM_CART", true);

                                // Start Activity
                                context.startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(context, "Error navigating to product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // Handle View Details button event (if exists in new layout)
                if (holder.btnViewDetails != null) {
                    holder.btnViewDetails.setOnClickListener(v -> {
                        if (isOutOfStock) {
                            Toast.makeText(context, product.getPhoneName() + " is currently out of stock", Toast.LENGTH_SHORT).show();
                        } else {
                            goToProductDetail(product);
                        }
                    });
                }

                // Handle event when clicking on the entire item
                holder.itemView.setOnClickListener(view -> {
                    if (isOutOfStock) {
                        Toast.makeText(context, product.getPhoneName() + " is currently out of stock", Toast.LENGTH_SHORT).show();
                    } else {
                        goToProductDetail(product);
                    }
                });
            } catch (Exception e) {
                Toast.makeText(context, "Error binding product view: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            return products != null ? products.size() : 0;
        }

        // ViewHolder for the item_phone.xml layout
        class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPhone;
            View overlayView;
            TextView tvBrand, tvPhoneName, tvPrice, tvRatingValue, tvBadge, tvSoldOutOverlay, tvStockStatus;
            RatingBar ratingBar;
            ImageButton btnFavorite, btnAddToCart;
            Button btnViewDetails;

            public ProductViewHolder(View itemView) {
                super(itemView);
                try {
                    // Find views in the item_phone.xml layout
                    imgPhone = itemView.findViewById(R.id.imgPhone);
                    overlayView = itemView.findViewById(R.id.overlayView);
                    tvSoldOutOverlay = itemView.findViewById(R.id.tvSoldOutOverlay);
                    tvBrand = itemView.findViewById(R.id.tvBrand);
                    tvPhoneName = itemView.findViewById(R.id.tvPhoneName);
                    tvPrice = itemView.findViewById(R.id.tvPrice);
                    tvRatingValue = itemView.findViewById(R.id.tvRatingValue);
                    tvBadge = itemView.findViewById(R.id.tvBadge);
                    tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
                    ratingBar = itemView.findViewById(R.id.ratingBar);
                    btnFavorite = itemView.findViewById(R.id.btnFavorite);
                    btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
                } catch (Exception e) {
                    // Handle null references gracefully
                }
            }
        }
    }

    // Class to create spacing between items in the grid
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private void goToProductDetail(Phone phone) {
        try {
            Intent intent = new Intent(ShoppingActivity.this, ProductActivity.class);
            intent.putExtra("PRODUCT_NAME", phone.getPhoneName());
            intent.putExtra("PRODUCT_PRICE", String.format("%,.0fđ", phone.getPrice()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateCartNavigation() {
        try {
            // Cập nhật giao diện các button
            if (btnProducts != null) {
                btnProducts.setBackgroundResource(R.drawable.tab_background);
                btnProducts.setTextColor(getResources().getColor(android.R.color.white, null));
                btnProducts.setAlpha(0.9f);
            }

            // Set Cart button to selected state
            if (btnShop != null) {
                btnShop.setBackgroundResource(R.drawable.selected_tab_background);
                btnShop.setTextColor(getResources().getColor(android.R.color.white, null));
                btnShop.setAlpha(1.0f);
            }

            // Chuyển đến CartActivity
            Intent intent = new Intent(ShoppingActivity.this, CartActivity.class);
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(double price) {
        return String.format("%,.0fđ", price);
    }

    // Tạo badge background cho Sold Out nếu chưa tồn tại
    private void createSoldOutBadgeResource() {
        try {
            getResources().getDrawable(R.drawable.badge_sold_out_background);
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, "Creating sold out badge resource", Toast.LENGTH_SHORT).show();
        }
    }
}