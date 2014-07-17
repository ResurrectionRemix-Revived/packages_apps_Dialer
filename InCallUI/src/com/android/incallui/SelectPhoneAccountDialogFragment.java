/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.incallui;

import com.google.android.collect.Lists;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telecomm.PhoneAccount;
import android.telecomm.PhoneAccountMetadata;
import android.telecomm.TelecommManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.contacts.common.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog that allows the user to switch between default SIM cards
 */
public class SelectPhoneAccountDialogFragment extends DialogFragment {
    private List<PhoneAccount> mAccounts;
    private boolean mIsSelected;
    private TelecommManager mTelecommManager;

    /* Preferred way to show this dialog */
    public static void show(FragmentManager fragmentManager) {
        SelectPhoneAccountDialogFragment fragment = new SelectPhoneAccountDialogFragment();
        fragment.show(fragmentManager, "selectAccount");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mIsSelected = false;
        mTelecommManager = TelecommManager.from(getActivity());
        mAccounts = mTelecommManager.getEnabledPhoneAccounts();

        final DialogInterface.OnClickListener selectionListener =
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsSelected = true;
                PhoneAccount selectedAccount = mAccounts.get(which);
                InCallPresenter.getInstance().handleAccountSelection(selectedAccount);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ListAdapter selectAccountListAdapter = new SelectAccountListAdapter(
                builder.getContext(),
                R.layout.select_account_list_item,
                mAccounts);

        return builder.setTitle(R.string.select_account_dialog_title)
                .setAdapter(selectAccountListAdapter, selectionListener)
                .create();
    }

    private class SelectAccountListAdapter extends ArrayAdapter<PhoneAccount> {
        private Context mContext;
        private int mResId;

        public SelectAccountListAdapter(Context context, int resource, List<PhoneAccount> objects) {
            super(context, resource, objects);
            mContext = context;
            mResId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView;
            final ViewHolder holder;

            if (convertView == null) {
                // Cache views for faster scrolling
                rowView = inflater.inflate(mResId, null);
                holder = new ViewHolder();
                holder.textView = (TextView) rowView.findViewById(R.id.text);
                holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            }
            else {
                rowView = convertView;
                holder = (ViewHolder) rowView.getTag();
            }

            PhoneAccount item = getItem(position);
            PhoneAccountMetadata itemMetadata = mTelecommManager.getPhoneAccountMetadata(item);
            holder.textView.setText(itemMetadata.getLabel());
            holder.imageView.setImageDrawable(itemMetadata.getIcon(mContext));
            return rowView;
        }

        private class ViewHolder {
            TextView textView;
            ImageView imageView;
        }
    }

    @Override
    public void onPause() {
        if (!mIsSelected) {
            InCallPresenter.getInstance().cancelAccountSelection();
        }
        super.onPause();
    }
}