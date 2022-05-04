package de.hs.stralsund.dartstracker.activities.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hs.stralsund.dartstracker.activities.PlayerSettingsActivity;
import de.hs.stralsund.dartstracker.activities.R;
import de.hs.stralsund.dartstracker.dartgame.GameInformation;
import de.hs.stralsund.dartstracker.dartgame.Player;

public class PlayerDeleteDialog extends AppCompatDialogFragment {
    private PlayerSettingsActivity playerSettings;
    private EditText playerDeleteName;
    private ListView playerList;
    private Button deletePlayerActivate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_delete_player_dialog, null);
        this.playerList = view.findViewById(R.id.playerNameList);
        this.playerDeleteName = view.findViewById(R.id.playerNameDeleteText);
        this.deletePlayerActivate = view.findViewById(R.id.playerDeleteButton);
        setPlayerNameListContent();
        this.playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerDeleteName.setText((String) parent.getItemAtPosition(position));
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        this.deletePlayerActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playerDeleteName.getText().toString().equals("")){
                    playerSettings.deletePlayer(playerDeleteName.getText().toString());
                    setPlayerNameListContent();
                }
            }
        });
        builder.setView(view);
        return builder.create();
    }

    @NotNull
    private List<String> setPlayerNameListContent() {

        List<String> playerNameList = new ArrayList<>();
        for(Player player : this.playerSettings.getGameInformation().getPlayerList()){
            playerNameList.add(player.getPlayerName());
        }

        playerList.setAdapter(new ArrayAdapter<>(playerList.getContext(), R.layout.support_simple_spinner_dropdown_item, playerNameList));

        return playerNameList;
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
