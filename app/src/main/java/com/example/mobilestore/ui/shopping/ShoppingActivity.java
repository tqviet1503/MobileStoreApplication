package com.example.mobilestore.ui.shopping;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<Product> productList;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_shopping);

            // Initialize UI components
            initializeViews();

            // Setup RecyclerView for products
            setupRecyclerView();

            // Setup navigation buttons
            setupNavigationButtons();

            // Setup category filter chips
            setupCategoryChips();

            // Setup search functionality
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
            // Create sample product data
            productList = new ArrayList<>();

            // Check if drawable exists
            int phoneImageResource;
            try {
                phoneImageResource = R.drawable.phone_sample;
                getResources().getDrawable(phoneImageResource);
            } catch (Resources.NotFoundException e) {
                // Fallback to a system drawable if custom one is missing
                phoneImageResource = android.R.drawable.ic_menu_gallery;
            }

            // Create sample product list with formatted currency
            productList.add(new Product("iPhone 14 Pro Max", "$1,199", 4.9f, phoneImageResource));
            productList.add(new Product("Samsung Galaxy S23 Ultra", "$1,099", 4.7f, phoneImageResource));
            productList.add(new Product("Xiaomi 13 Pro", "$799", 4.5f, phoneImageResource));
            productList.add(new Product("Oppo Find X5 Pro", "$899", 4.6f, phoneImageResource));
            productList.add(new Product("iPhone 14", "$899", 4.8f, phoneImageResource));
            productList.add(new Product("Samsung Galaxy S23", "$849", 4.6f, phoneImageResource));

            // Set up the RecyclerView with a grid layout (2 columns)
            if (productRecyclerView != null) {
                GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                productRecyclerView.setLayoutManager(gridLayoutManager);

                // Add spacing between items
                int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
                productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

                // Create and set the adapter with the new item_phone layout
                productAdapter = new ProductAdapter(productList, this);
                productRecyclerView.setAdapter(productAdapter);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigationButtons() {
        try {
            // Set click listeners for navigation buttons
            if (btnProducts != null) {
                btnProducts.setOnClickListener(view -> {
                    // Already on Products page, do nothing or refresh
                });
            }

            if (btnShop != null) {
                btnShop.setOnClickListener(view -> {
                    // Navigate to Product Detail Activity
                    Intent intent = new Intent(ShoppingActivity.this, ProductActivity.class);
                    startActivity(intent);
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
                    filterProducts("All");
                });
            }

            // We would need to get all other chips dynamically or by ID and set their listeners
            if (categoryChipGroup != null) {
                // This is a simplified example
                for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                    View chipView = categoryChipGroup.getChildAt(i);
                    if (chipView instanceof Chip) {
                        Chip chip = (Chip) chipView;
                        chip.setOnClickListener(view -> {
                            // Filter products by the selected category
                            filterProducts(chip.getText().toString());
                        });
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up category filters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterProducts(String category) {
        // This would filter the product list based on the selected category
        // For simplicity, we're just logging the action
        System.out.println("Filtering by category: " + category);

        // In a real app, we would filter the list and update the adapter
        // List<Product> filteredList = ...
        // productAdapter.updateList(filteredList);
    }

    private void setupSearch() {
        try {
            // Set up search functionality
            if (searchEditText != null) {
                searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                    // Search for products based on the input text
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        searchProducts(query);
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
        // This would search for products matching the query
        // For simplicity, we're just logging the action
        System.out.println("Searching for: " + query);

        // In a real app, we would search the list and update the adapter
        // List<Product> searchResults = ...
        // productAdapter.updateList(searchResults);
    }

    // Model class for Product
    public static class Product {
        private String name;
        private String price;
        private float rating;
        private int imageResId;

        public Product(String name, String price, float rating, int imageResId) {
            this.name = name;
            this.price = price;
            this.rating = rating;
            this.imageResId = imageResId;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getPrice() {
            return price;
        }

        public float getRating() {
            return rating;
        }

        public int getImageResId() {
            return imageResId;
        }
    }

    // Adapter class for RecyclerView using the new item_phone.xml layout
    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private ShoppingActivity context;

        public ProductAdapter(List<Product> products, ShoppingActivity context) {
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
                // Fallback when layout is not found
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
                Product product = products.get(position);

                // Display product information with the new layout
                if (holder.tvPhoneName != null) {
                    holder.tvPhoneName.setText(product.getName());
                }

                if (holder.tvPrice != null) {
                    holder.tvPrice.setText(product.getPrice());
                }

                if (holder.ratingBar != null) {
                    holder.ratingBar.setRating(product.getRating());
                }

                if (holder.tvRatingValue != null) {
                    holder.tvRatingValue.setText(String.valueOf(product.getRating()));
                }

                // Determine brand from product name
                if (holder.tvBrand != null) {
                    if (product.getName().contains("iPhone")) {
                        holder.tvBrand.setText("Apple");
                    } else if (product.getName().contains("Samsung")) {
                        holder.tvBrand.setText("Samsung");
                    } else if (product.getName().contains("Xiaomi")) {
                        holder.tvBrand.setText("Xiaomi");
                    } else if (product.getName().contains("Oppo")) {
                        holder.tvBrand.setText("Oppo");
                    } else {
                        holder.tvBrand.setText("Brand");
                    }
                }

                // Display product image
                if (holder.imgPhone != null) {
                    holder.imgPhone.setImageResource(product.getImageResId());
                }

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
                        // Change icon when clicked on favorite
                        boolean isFavorite = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();
                        if (isFavorite) {
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                            holder.btnFavorite.setTag(false);
                            Toast.makeText(context, "Removed " + product.getName() + " from favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            holder.btnFavorite.setImageResource(R.drawable.ic_favorite);
                            holder.btnFavorite.setTag(true);
                            Toast.makeText(context, "Added " + product.getName() + " to favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Initially not a favorite product
                    holder.btnFavorite.setTag(false);
                }

                // Set up event for add to cart button
                if (holder.btnAddToCart != null) {
                    holder.btnAddToCart.setOnClickListener(v -> {
                        Toast.makeText(context, "Added " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
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
                    // Views not found, will be handled in onBindViewHolder
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

    private void goToProductDetail(Product product) {
        try {
            // Pass product information to the detail screen
            Intent intent = new Intent(ShoppingActivity.this, ProductActivity.class);
            intent.putExtra("PRODUCT_NAME", product.getName());
            intent.putExtra("PRODUCT_PRICE", product.getPrice());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error navigating to product details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
