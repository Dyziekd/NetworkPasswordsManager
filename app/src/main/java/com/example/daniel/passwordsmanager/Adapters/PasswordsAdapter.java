package com.example.daniel.passwordsmanager.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.daniel.passwordsmanager.DataModels.Password;
import com.example.daniel.passwordsmanager.R;

import java.io.Serializable;
import java.util.ArrayList;


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
        if(convertView == null)
            convertView = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.passwords_list_item, parent, false);

        // create list row elements
        Holder holder = new Holder();
        holder.serviceName = (TextView)convertView.findViewById(R.id.passwords_list__service_name);
        holder.password = (TextView)convertView.findViewById(R.id.passwords_list__password);
        convertView.setTag(holder);

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
        return convertView;
    }


    private static class Holder
    {
        private TextView serviceName, password;
    }
}
