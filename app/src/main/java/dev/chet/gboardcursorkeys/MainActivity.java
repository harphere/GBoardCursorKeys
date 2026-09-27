package dev.chet.gboardcursorkeys;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        TextView view = new TextView(this);
        view.setText("Gboard Cursor Keys\n\nEnable the module in LSPosed, scope it to Gboard (com.google.android.inputmethod.latin), then force-stop and reopen Gboard.\n\nTap the arrows to move one character. Hold to repeat.");
        int p = (int)(24 * getResources().getDisplayMetrics().density);
        view.setPadding(p,p,p,p);
        setContentView(view);
    }
}
