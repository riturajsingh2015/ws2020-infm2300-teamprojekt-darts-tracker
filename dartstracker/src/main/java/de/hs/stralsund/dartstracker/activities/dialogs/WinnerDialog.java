package de.hs.stralsund.dartstracker.activities.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.hs.stralsund.dartstracker.activities.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WinnerDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WinnerDialog extends AppCompatDialogFragment {

    private static final String WINNER_NAME = "winnerName";

    private TextView winnerNameView;
    private String winnerName;

    public WinnerDialog() {
        // Required empty public constructor
    }

    public static WinnerDialog newInstance(String winnerName) {

        WinnerDialog fragment = new WinnerDialog();
        Bundle args = new Bundle();
        args.putString(WINNER_NAME, winnerName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            winnerName = getArguments().getString(WINNER_NAME);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_winner_dialog, null);

        builder.setView(view);

        winnerNameView = view.findViewById(R.id.winnerName);
        winnerNameView.setText(winnerName);

        return builder.create();
    }
}