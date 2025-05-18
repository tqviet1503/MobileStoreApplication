package com.example.mobilestore.ui.admin_customer_bill;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilestore.R;
import com.example.mobilestore.adapter.BillAdapter;
import com.example.mobilestore.bill.BillDetails;
import com.example.mobilestore.data.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BillListFragment extends Fragment implements BillAdapter.OnBillClickListener {
    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private ProductRepository repository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_bill_list, container, false);
        repository = ProductRepository.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view_bill);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        List<BillDetails> bills = getBillsFromDatabase();
        adapter = new BillAdapter(bills, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBillClick(BillDetails bill) {
        showBillDetailsDialog(bill);
    }

    private void showBillDetailsDialog(BillDetails bill) {
        SQLiteDatabase db = repository.db;

        Log.d("BillDetails", "Showing dialog for bill_id: " + bill.getBillId());

        try {
            Cursor cursor = db.rawQuery(
                    "SELECT b.id, b.total_amount, b.created_at, " +
                            "c.name as customer_name, c.phone as customer_phone, c.address as customer_address, " +
                            "bd.phone_name, bd.quantity, bd.unit_price " +
                            "FROM bills b " +
                            "INNER JOIN bill_details bd ON b.id = bd.bill_id " +
                            "INNER JOIN customers c ON b.customer_id = c.id " +
                            "WHERE b.id = ? AND bd.id = ?",
                    new String[]{
                            String.valueOf(bill.getBillId()),
                            String.valueOf(bill.getId())
                    }
            );

            if (cursor != null && cursor.moveToFirst()) {
                Log.d("BillDetails", "Found bill data");

                View dialogView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.bill_details_dialog, null);

                TextView billIdText = dialogView.findViewById(R.id.text_bill_id);
                TextView customerText = dialogView.findViewById(R.id.text_customer_info);
                TextView phoneText = dialogView.findViewById(R.id.text_phone_info);
                TextView quantityText = dialogView.findViewById(R.id.text_quantity);
                TextView totalText = dialogView.findViewById(R.id.text_total_amount);

                billIdText.setText("Bill #" + cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                customerText.setText(String.format("Customer: %s\nPhone: %s\nAddress: %s",
                        cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customer_phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("customer_address"))
                ));
                phoneText.setText("Product: " + cursor.getString(cursor.getColumnIndexOrThrow("phone_name")));
                quantityText.setText("Quantity: " + cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                totalText.setText(String.format(Locale.getDefault(), "Total Price: %,.0fđ",
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"))));

                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .setPositiveButton("Close", null)
                        .show();

                cursor.close();
            } else {
                Log.e("BillDetails", "No data found for bill_id: " + bill.getBillId());
                Toast.makeText(requireContext(),
                        "Could not find bill details", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("BillDetails", "Error showing dialog: " + e.getMessage());
            Toast.makeText(requireContext(),
                    "Error showing bill details", Toast.LENGTH_SHORT).show();
        }
    }

    private List<BillDetails> getBillsFromDatabase() {
        List<BillDetails> bills = new ArrayList<>();
        SQLiteDatabase db = repository.db;

        Log.d("BillList", "Querying bills from database");

        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT b.id as bill_id, bd.id as detail_id, bd.quantity, " +
                        "bd.unit_price, bd.phone_id, b.total_amount " +
                        "FROM bills b " +
                        "INNER JOIN bill_details bd ON b.id = bd.bill_id " +
                        "ORDER BY b.created_at DESC", null);

        if (cursor.moveToFirst()) {
            do {
                BillDetails bill = new BillDetails();
                int billId = cursor.getInt(cursor.getColumnIndexOrThrow("bill_id"));
                int detailId = cursor.getInt(cursor.getColumnIndexOrThrow("detail_id"));

                Log.d("BillList", "Found bill - Bill ID: " + billId + ", Detail ID: " + detailId);

                bill.setId(cursor.getInt(cursor.getColumnIndexOrThrow("detail_id")));
                bill.setBillId(cursor.getInt(cursor.getColumnIndexOrThrow("bill_id")));
                bill.setPhoneId(cursor.getString(cursor.getColumnIndexOrThrow("phone_id")));
                bill.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow("quantity")));
                bill.setUnitPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price")));
                bills.add(bill);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bills;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView(); // Refresh list when fragment resumes
    }
}