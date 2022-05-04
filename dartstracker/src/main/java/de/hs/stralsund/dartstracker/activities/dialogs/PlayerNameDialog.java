package de.hs.stralsund.dartstracker.activities.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import de.hs.stralsund.dartstracker.activities.PlayerSettingsActivity;
import de.hs.stralsund.dartstracker.activities.R;

public class PlayerNameDialog extends AppCompatDialogFragment {
    private EditText editTextPlayerName;
    private PlayerSettingsActivity playerSettings;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        editTextPlayerName = view.findViewById(R.id.dialog_PlayerName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.dialog_title)
                .setNegativeButton(R.string.dialog_cancelbutton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton(R.string.dialog_confirmbutton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerName = editTextPlayerName.getText().toString();
                if(playerName.isEmpty()){
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(playerSettings, R.string.emptyPlayerName,duration);
                    toast.show();
                }else{
                    playerSettings.addPlayer(playerName);
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            playerSettings = (PlayerSettingsActivity) context;
        }
        catch (ClassCastException classE) {
            throw new ClassCastException(context.toString() + "must implement PlayerNameDialogInterface");
        }
    }
}
