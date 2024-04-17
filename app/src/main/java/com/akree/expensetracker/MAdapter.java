package com.akree.expensetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akree.expensetracker.serialization.Expense;

import java.util.ArrayList;
import java.util.List;

public class MAdapter extends RecyclerView.Adapter<MAdapter.ExpenseViewHolder> {

    private ArrayList<Expense> expenses = new ArrayList<Expense>();
    private Context context;

    public MAdapter(Context context, ArrayList<Expense> expenses) {
        this.expenses = expenses;
        this.context = context;
    }


    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false);

        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {

        holder.category.setText(expenses.get(position).getCategory());
        holder.type.setText(expenses.get(position).getType());
        holder.date.setText(expenses.get(position).getDate());
        holder.amount.setText(Double.valueOf(expenses.get(position).getAmount()).toString());

    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {

        private TextView category, date, amount, type;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);

            category = itemView.findViewById(R.id.category);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);
            type = itemView.findViewById(R.id.type);


        }

    }

    public void updateList(List<Expense> list) {
        expenses = (ArrayList<Expense>) list;
        notifyDataSetChanged();
    }

}

