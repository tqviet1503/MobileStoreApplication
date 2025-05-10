package com.example.mobilestore.ui.shopping;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.example.mobilestore.R;
import com.example.mobilestore.ui.product_detail.ProductActivity;
import com.example.mobilestore.ui.payment.PaymentActivity;
import com.example.mobilestore.ui.customer_profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class ShoppingActivity extends AppCompatActivity {

    // UI components
    private TextView titleTextView;
    private ImageView avatarImageView, searchIcon;
    private EditText searchEditText;
    private CardView searchCardView, navBarCardView;
    private Button btnProducts, btnShop, btnPayment, btnProfile;
    private ChipGroup categoryChipGroup;
    private Chip chipAll;
    private RecyclerView productRecyclerView;

    // Data
    private List<Phone> allPhones;
    private List<Phone> filteredPhones;
    private ProductRepository repository;
    private ProductAdapter productAdapter;
    private String currentCategory = "All";  // Danh mục hiện tại
    private String currentSearchQuery = "";  // Truy vấn tìm kiếm hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_shopping);
            initializeViews();
            setupNavigationButtons();
            repository = ProductRepository.getInstance(this);
            setupRecyclerView();
            setupCategoryChips();
            setupSearch();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing shopping screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            chipAll = findViewById(R.id.chipAll);
            productRecyclerView = findViewById(R.id.productRecyclerView);
        } catch (Exception e) {
            Toast.makeText(this, "Error finding views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        try {
            // xài phone chỗ này thay vì product như cũ
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

            //tạm thời không xài static nữa
            // Create sample product list with formatted currency and proper category
//            allProductList.add(new Product("iPhone 14 Pro Max", "$1,199", 4.9f, phoneImageResource, "iPhone"));
//            allProductList.add(new Product("Samsung Galaxy S23 Ultra", "$1,099", 4.7f, phoneImageResource, "Samsung"));
//            allProductList.add(new Product("Xiaomi 13 Pro", "$799", 4.5f, phoneImageResource, "Xiaomi"));
//            allProductList.add(new Product("Oppo Find X5 Pro", "$899", 4.6f, phoneImageResource, "Oppo"));
//            allProductList.add(new Product("iPhone 14", "$899", 4.8f, phoneImageResource, "iPhone"));
//            allProductList.add(new Product("Samsung Galaxy S23", "$849", 4.6f, phoneImageResource, "Samsung"));
            // get các brand từ database
            List<String> brands = getAllBrandNames();
            for (String brand : brands) {
                allPhones.addAll(repository.getPhonesForBrand(brand));
            }

            // chắc là filter
            filteredPhones.addAll(allPhones);

            // Set up the RecyclerView with a grid layout (2 columns)
            if (productRecyclerView != null) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                productRecyclerView.setLayoutManager(gridLayoutManager);

                // Add spacing between items
                int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
                productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

                // Create and set the adapter with the new item_phone layout
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
                    // Đã ở màn hình Products, không cần làm gì
                });
            }

            if (btnShop != null) {
                btnShop.setOnClickListener(view -> {
                    // Chỉ sử dụng simulateCartNavigation thay vì mở ProductActivity
                    simulateCartNavigation();
                });
            }

            if (btnPayment != null) {
                btnPayment.setOnClickListener(view -> {
                    // Navigate to Payment Activity
                    Intent intent = new Intent(ShoppingActivity.this, PaymentActivity.class);
                    startActivity(intent);
                });
            }

            if (btnProfile != null) {
                btnProfile.setOnClickListener(view -> {
                    // Navigate to Profile Activity
                    Intent intent = new Intent(ShoppingActivity.this, ProfileActivity.class);
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up navigation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCategoryChips() {
        try {
            // Set click listeners for chips
            if (chipAll != null) {
                chipAll.setOnClickListener(view -> {
                    // Show all products (no filtering)
                    currentCategory = "All";
                    applyFilters();
                    updateChipSelection(chipAll);
                });
            }

            // Get all other chips dynamically and set their listeners
            if (categoryChipGroup != null) {
                for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                    View chipView = categoryChipGroup.getChildAt(i);
                    if (chipView instanceof Chip) {
                        Chip chip = (Chip) chipView;
                        final String categoryName = chip.getText().toString();

                        chip.setOnClickListener(view -> {
                            // Filter products by the selected category
                            currentCategory = categoryName;
                            applyFilters();
                            updateChipSelection(chip);
                        });
                    }
                }
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
                    chip.setChipBackgroundColorResource(R.color.colorPrimary);
                    chip.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    chip.setChipBackgroundColorResource(R.color.chipBackground);
                    chip.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }
    }

    private void filterProducts() {
        applyFilters();
    }

    //chỉnh lại chỗ filter này theo phone
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

        if (filteredPhones.isEmpty()) {
            Toast.makeText(this, "No phones found", Toast.LENGTH_SHORT).show();
        }

        productAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        try {
            // Set up search functionality
            if (searchEditText != null) {
                // TextWatcher theo dõi các thay đổi văn bản trong thời gian thực
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // Cập nhật truy vấn tìm kiếm
                        currentSearchQuery = s.toString().trim();
                        if (currentSearchQuery.length() >= 1 || currentSearchQuery.isEmpty()) {
                            applyFilters();
                        }
                    }
                });

                // Xử lý khi người dùng nhấn Enter trên bàn phím
                searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                            actionId == EditorInfo.IME_ACTION_DONE ||
                            (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        // Áp dụng tìm kiếm khi người dùng nhấn Enter
                        applyFilters();
                        return true;
                    }
                    return false;
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up search: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void searchProducts(String query) {
        currentSearchQuery = query;
        applyFilters();
    }

    // Model class for Product with additional category field
//    public static class Product {
//        private String name;
//        private String price;
//        private float rating;
//        private int imageResId;
//        private String category;
//
//        public Product(String name, String price, float rating, int imageResId, String category) {
//            this.name = name;
//            this.price = price;
//            this.rating = rating;
//            this.imageResId = imageResId;
//            this.category = category;
//        }
//
//        // Getters
//        public String getName() {
//            return name;
//        }
//
//        public String getPrice() {
//            return price;
//        }
//
//        public float getRating() {
//            return rating;
//        }
//
//        public int getImageResId() {
//            return imageResId;
//        }
//
//        public String getCategory() {
//            return category;
//        }
//    }

    // Adapter class for RecyclerView using the new item_phone.xml layout
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
                // Inflate the new item_phone.xml layout
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

                // Display product information with the new layout
                if (holder.tvPhoneName != null) {
                    holder.tvPhoneName.setText(product.getPhoneName());
                }

                if (holder.tvPrice != null) {
                    holder.tvPrice.setText(String.format("%,.0fđ", product.getPrice()));
                }
//
//                if (holder.ratingBar != null) {
//                    holder.ratingBar.setRating(product.getRating());
//                }

//                if (holder.tvRatingValue != null) {
//                    holder.tvRatingValue.setText(String.valueOf(product.getRating()));
//                }

                // Display brand from product category
                if (holder.tvBrand != null) {
                    holder.tvBrand.setText(product.getBrand());
                }

                // Display product image
//                if (holder.imgPhone != null) {
//                    holder.imgPhone.setImageResource(product.getImageResId());
//                }

                // Show "New" badge for some products
                if (holder.tvBadge != null) {
                    if (position % 3 == 0) { // Randomly show for some products
                        holder.tvBadge.setVisibility(View.VISIBLE);
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
                    holder.btnAddToCart.setOnClickListener(v -> {
                        // Cập nhật: Chuyển thông tin sản phẩm đến màn hình chi tiết
                        // thay vì gọi btnShop.performClick()
                        try {
                            Toast.makeText(context, "Added " + product.getPhoneName() + " to cart", Toast.LENGTH_SHORT).show();

                            // Tạo Intent với thông tin sản phẩm đầy đủ
                            Intent intent = new Intent(context, ProductActivity.class);
                            intent.putExtra("PRODUCT_NAME", product.getPhoneName());
                            intent.putExtra("PRODUCT_PRICE", product.getPrice());
                            intent.putExtra("FROM_CART", true); // Thêm flag để biết đây là từ nút Add to Cart

                            // Bắt đầu Activity
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(context, "Error navigating to product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Xử lý sự kiện khi nhấp vào nút View Details (nếu có trong layout mới)
                if (holder.btnViewDetails != null) {
                    holder.btnViewDetails.setOnClickListener(v -> {
                        goToProductDetail(product);
                    });
                }

                // Handle event when clicking on the entire item
                holder.itemView.setOnClickListener(view -> {
                    goToProductDetail(product);
                });
            } catch (Exception e) {
                Toast.makeText(context, "Error binding product view: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            return products != null ? products.size() : 0;
        }

        // ViewHolder for the new item_phone.xml layout
        class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPhone;
            TextView tvBrand, tvPhoneName, tvPrice, tvRatingValue, tvBadge;
            RatingBar ratingBar;
            ImageButton btnFavorite, btnAddToCart;
            Button btnViewDetails; // Có thể có trong layout mới

            public ProductViewHolder(View itemView) {
                super(itemView);
                try {
                    // Find views in the item_phone.xml layout
                    imgPhone = itemView.findViewById(R.id.imgPhone);
                    tvBrand = itemView.findViewById(R.id.tvBrand);
                    tvPhoneName = itemView.findViewById(R.id.tvPhoneName);
                    tvPrice = itemView.findViewById(R.id.tvPrice);
                    tvRatingValue = itemView.findViewById(R.id.tvRatingValue);
                    tvBadge = itemView.findViewById(R.id.tvBadge);
                    ratingBar = itemView.findViewById(R.id.ratingBar);
                    btnFavorite = itemView.findViewById(R.id.btnFavorite);
                    btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
                } catch (Exception e) {
                    // Views không tìm thấy, xử lý trong onBindViewHolder
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
        // Cập nhật giao diện để hiển thị tab Cart được chọn
        // Đặt nút Phones về trạng thái không được chọn
        if (btnProducts != null) {
            btnProducts.setBackgroundResource(R.drawable.tab_background);
            btnProducts.setTextColor(getResources().getColor(android.R.color.white, null));
            btnProducts.setAlpha(0.9f);
        }

        // Đặt nút Cart về trạng thái được chọn
        if (btnShop != null) {
            btnShop.setBackgroundResource(R.drawable.selected_tab_background);
            btnShop.setTextColor(getResources().getColor(android.R.color.white, null));
            btnShop.setAlpha(1.0f);
        }

        // Hiển thị một Dialog giả lập màn hình giỏ hàng
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Shopping Cart");

        // Tạo danh sách các mặt hàng trong giỏ hàng
        StringBuilder cartItems = new StringBuilder();
        int numItemsInCart = 0;

        // Kiểm tra số sản phẩm mà người dùng đã thêm vào giỏ hàng
        for (Phone product : allPhones) {
            if (Math.random() > 0.7) {
                cartItems.append("• ").append(product.getPhoneName())
                        .append(" - ").append(product.getPrice())
                        .append("\n");
                numItemsInCart++;
            }
        }

        // Nếu không có sản phẩm nào trong giỏ hàng
        if (numItemsInCart == 0) {
            builder.setMessage("Your cart is empty.");
            builder.setPositiveButton("Shop Now", (dialog, id) -> {
                // Quay lại tab Phones
                btnProducts.performClick();
            });
        } else {
            // Hiển thị các sản phẩm và tổng giá
            cartItems.append("\nTotal Items: ").append(numItemsInCart);
            builder.setMessage(cartItems.toString());

            builder.setPositiveButton("Checkout", (dialog, id) -> {
                // Chuyển đến màn hình thanh toán
                if (btnPayment != null) {
                    btnPayment.performClick();
                }
            });

            builder.setNegativeButton("Continue Shopping", (dialog, id) -> {
                // Quay lại tab Phones
                btnProducts.performClick();
            });
        }

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}