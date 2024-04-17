package com.akree.expensetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.akree.expensetracker.databinding.ActivityNavBinding;
import com.akree.expensetracker.serialization.Expense;
import com.akree.expensetracker.serialization.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NavActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityNavBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String date = "";
    private String type = "Income";
    private Double bud = 0.0;
    private List<String> cat = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference("user/" + user.getUid() + "/expenses");

        FirebaseDatabase.getInstance().getReference("user/" + user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bud = (double) snapshot.getValue(User.class).getBudget();
                cat = snapshot.getValue(User.class).getCategories();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        binding = ActivityNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //add expenses
        setSupportActionBar(binding.appBarNav.toolbar);
        binding.appBarNav.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddExpenseDialog();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_profile, R.id.nav_statistics, R.id.nav_expenses)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showAddExpenseDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.add_expense_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText amount = dialogView.findViewById(R.id.amount);
        final Spinner type_spinner = dialogView.findViewById(R.id.type_spinner);
        final ChipGroup chip_group_categories = dialogView.findViewById(R.id.chip_group_categories);
        final TextView choose_date = dialogView.findViewById(R.id.choose_date);
        final Button add = dialogView.findViewById(R.id.button_add);

        final AlertDialog b = dialogBuilder.create();
        b.show();

        for (int i = 0; i < cat.size(); i++) {

            LayoutInflater inflater = LayoutInflater.from(this);

            Chip newChip = (Chip) inflater.inflate(R.layout.layout_chip_entry, chip_group_categories, false);

            newChip.setText(cat.get(i));
            newChip.setCloseIconVisible(false);
            chip_group_categories.addView(newChip);

        }

        String[] types = {"Income", "Outcome"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, types);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type_spinner.setAdapter(arrayAdapter);


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String a = amount.getText().toString();
                if (TextUtils.isEmpty(a)) {

                    amount.setError("Enter amount of money!");

                } else {
                    Expense expense = new Expense();
                    expense.setAmount(Double.parseDouble(a));

                    for (int i = 0; i < chip_group_categories.getChildCount(); i++) {
                        Chip chip = (Chip) chip_group_categories.getChildAt(i);
                        if (chip.isChecked() || chip.isSelected()) {
                            expense.setCategory(chip.getText().toString());
                        }
                    }

                    expense.setDate(choose_date.getText().toString());
                    expense.setType(type);

                    String id = UUID.randomUUID() + expense.getCategory();

                    databaseReference.child(id.toUpperCase(Locale.ROOT)).setValue(expense);

                    if (type.equals("Income"))
                        FirebaseDatabase.getInstance().getReference("user/" + user.getUid()).child("budget").setValue(bud + Double.parseDouble(a));
                    else
                        FirebaseDatabase.getInstance().getReference("user/" + user.getUid()).child("budget").setValue(bud - Double.parseDouble(a));

                    b.dismiss();
                }

            }

        });

        choose_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dataPickerDialog = new DatePickerDialog(NavActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        i1++;
                        String x = "";

                        if (i1 < 10) x = "0";

                        date = i2 + "." + i1 + "." + i;

                        choose_date.setText(i2 + "." + x + i1 + "." + i);
                    }
                }, mYear, mMonth, mDay);

                dataPickerDialog.show();

            }
        });

        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                type = types[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }
}