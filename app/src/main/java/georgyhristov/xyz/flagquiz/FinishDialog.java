package georgyhristov.xyz.flagquiz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



/**
 * Created by gohv on 13.10.16.
 */

public class FinishDialog extends DialogFragment{


MainActivityFragment main = new MainActivityFragment();



    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Game Finished")
                .setMessage((
                        getString(R.string.results,
                                main.totalGuesses,
                                (1000 / (double) main.totalGuesses))));

       /*         .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exit();
            }
        }*/




        android.app.AlertDialog dialog = builder.create();
        return dialog;
    }

    private void exit() {
        System.exit(1);
    }
}
