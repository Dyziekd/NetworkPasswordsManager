package com.example.daniel.passwordsmanager.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import com.example.daniel.passwordsmanager.DataModels.Password;
import com.example.daniel.passwordsmanager.R;


public class PasswordsAdapter extends ArrayAdapter<Password> implements Serializable
{
    public PasswordsAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<Password> data)
    {
        super(context, resource, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        // create new row view
        View newRow = convertView;
        if(newRow == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newRow = layoutInflater.inflate(R.layout.passwords_list_item, null);
        }

        // create list row elements
        Holder holder = new Holder();
        holder.serviceName = (TextView)newRow.findViewById(R.id.passwords_list__service_name);
        holder.password = (TextView)newRow.findViewById(R.id.passwords_list__password);
        newRow.setTag(holder);

        // get row data
        Password rowData = getItem(position);

        // set row data
        holder.serviceName.setText(rowData.getServiceName() + " password:");
        holder.serviceName.setTextColor(Color.WHITE);
        holder.password.setText("********");

        // tag password as expired if it is expired
        if(rowData.isPassswordExpired())
        {
            holder.serviceName.setText(holder.serviceName.getText().toString() + " (expired)");
            holder.serviceName.setTextColor(Color.RED);
        }

        // return new row
        return newRow;
    }


    private static class Holder
    {
        private TextView serviceName, password;
    }
}
