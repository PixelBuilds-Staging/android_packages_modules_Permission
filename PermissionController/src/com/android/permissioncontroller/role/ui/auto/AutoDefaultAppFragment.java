/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.permissioncontroller.role.ui.auto;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.android.permissioncontroller.R;
import com.android.permissioncontroller.auto.AutoSettingsFrameFragment;
import com.android.permissioncontroller.role.ui.DefaultAppChildFragment;
import com.android.permissioncontroller.role.ui.RoleApplicationPreference;
import com.android.role.controller.model.Role;

/** Screen to pick a default app for a particular {@link Role}. */
public class AutoDefaultAppFragment extends AutoSettingsFrameFragment implements
        DefaultAppChildFragment.Parent {

    private String mRoleName;

    private UserHandle mUser;

    /**
     * Create a new instance of this fragment.
     *
     * @param roleName the name of the role for the default app
     * @param user     the user for the default app
     * @return a new instance of this fragment
     */
    @NonNull
    public static AutoDefaultAppFragment newInstance(@NonNull String roleName,
            @NonNull UserHandle user) {
        AutoDefaultAppFragment fragment = new AutoDefaultAppFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Intent.EXTRA_ROLE_NAME, roleName);
        arguments.putParcelable(Intent.EXTRA_USER, user);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mRoleName = arguments.getString(Intent.EXTRA_ROLE_NAME);
        mUser = arguments.getParcelable(Intent.EXTRA_USER);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Preferences will be added via shared logic in {@link DefaultAppChildFragment}.
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            DefaultAppChildFragment fragment = DefaultAppChildFragment.newInstance(mRoleName,
                    mUser);
            getChildFragmentManager().beginTransaction()
                    .add(fragment, null)
                    .commit();
        }
    }

    @Override
    public void setTitle(@NonNull CharSequence title) {
        setHeaderLabel(title);
    }

    @NonNull
    @Override
    public RoleApplicationPreference createApplicationPreference() {
        return new AutoRadioPreference(requireContext());
    }

    @NonNull
    @Override
    public Preference createFooterPreference() {
        Preference preference = new Preference(requireContext());
        preference.setIcon(R.drawable.ic_info_outline);
        preference.setSelectable(false);
        return preference;
    }

    @Override
    public void onPreferenceScreenChanged() {
    }
}
