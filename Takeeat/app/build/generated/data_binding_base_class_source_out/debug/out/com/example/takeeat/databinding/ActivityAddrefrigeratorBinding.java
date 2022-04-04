// Generated by view binder compiler. Do not edit!
package com.example.takeeat.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.takeeat.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAddrefrigeratorBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView addrefAddButton;

  @NonNull
  public final Button addrefApply;

  @NonNull
  public final RecyclerView addrefRecyclerView;

  @NonNull
  public final LinearLayout linearLayout;

  private ActivityAddrefrigeratorBinding(@NonNull ConstraintLayout rootView,
      @NonNull TextView addrefAddButton, @NonNull Button addrefApply,
      @NonNull RecyclerView addrefRecyclerView, @NonNull LinearLayout linearLayout) {
    this.rootView = rootView;
    this.addrefAddButton = addrefAddButton;
    this.addrefApply = addrefApply;
    this.addrefRecyclerView = addrefRecyclerView;
    this.linearLayout = linearLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAddrefrigeratorBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAddrefrigeratorBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_addrefrigerator, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAddrefrigeratorBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.addref_AddButton;
      TextView addrefAddButton = ViewBindings.findChildViewById(rootView, id);
      if (addrefAddButton == null) {
        break missingId;
      }

      id = R.id.addref_Apply;
      Button addrefApply = ViewBindings.findChildViewById(rootView, id);
      if (addrefApply == null) {
        break missingId;
      }

      id = R.id.addref_RecyclerView;
      RecyclerView addrefRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (addrefRecyclerView == null) {
        break missingId;
      }

      id = R.id.linearLayout;
      LinearLayout linearLayout = ViewBindings.findChildViewById(rootView, id);
      if (linearLayout == null) {
        break missingId;
      }

      return new ActivityAddrefrigeratorBinding((ConstraintLayout) rootView, addrefAddButton,
          addrefApply, addrefRecyclerView, linearLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
