package com.akree.expensetracker.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.akree.expensetracker.serialization.Expense;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ExpensesViewModel extends ViewModel {
    private final MutableLiveData<Map<String, Expense>> expenses = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Double> budget = new MutableLiveData<>(0.0);

    private static class ExpensesList {
        //public List<Expense> expenses = new LinkedList<>();
        public Map<String, Expense> expenses = new HashMap<>();
        public double budget = 0.0;

        public ExpensesList() {
        }

        public ExpensesList(Map<String, Expense> e, double b) {
            this.expenses = e;
            this.budget = b;
        }
    }

    public ExpensesViewModel() {
        FirebaseDatabase.getInstance()
                .getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                ExpensesList el = dataSnapshot.getValue(ExpensesList.class);
                if (el != null) {
                    expenses.setValue(el.expenses);
                    budget.setValue(el.budget);
                }
            }
        });

        FirebaseDatabase.getInstance()
                .getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ExpensesList el = snapshot.getValue(ExpensesList.class);
                        if (el != null) {
                            expenses.setValue(el.expenses);
                            budget.setValue(el.budget);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public LiveData<Map<String, Expense>> getExpenses() {
        return expenses;
    }

    public LiveData<Double> getBudget() {
        return budget;
    }
}
