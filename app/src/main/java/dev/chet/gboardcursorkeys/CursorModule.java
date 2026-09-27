package dev.chet.gboardcursorkeys;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.SurroundingText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CursorModule implements IXposedHookLoadPackage {
    private static final WeakHashMap<InputMethodService, WeakReference<FrameLayout>> overlays = new WeakHashMap<>();
    private static final int TAG = 0x4732434b;
    private static void trace(String message) { XposedBridge.log("GboardCursorKeys: " + message); }

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) {
        if (!"com.google.android.inputmethod.latin".equals(p.packageName)) return;
        trace("loaded in Gboard");
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onWindowShown", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                InputMethodService ime = (InputMethodService)param.thisObject;
                ime.getWindow().getWindow().getDecorView().post(() -> attach(ime));
            }
        });
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onWindowHidden", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) { detach((InputMethodService)param.thisObject); }
        });
        XposedHelpers.findAndHookMethod(InputMethodService.class, "onDestroy", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) { detach((InputMethodService)param.thisObject); }
        });
    }

    private static void attach(InputMethodService ime) {
        try {
            Window window = ime.getWindow().getWindow();
            ViewGroup decor = (ViewGroup) window.getDecorView();
            View existing = decor.findViewWithTag(TAG);
            if (existing != null) return;
            FrameLayout layer = new FrameLayout(ime);
            layer.setTag(TAG);
            layer.setClickable(false);
            layer.setFocusable(false);
            int size = dp(ime, 44), edge = dp(ime, 3), bottom = dp(ime, 2);
            TextView left = button(ime, "‹", KeyEvent.KEYCODE_DPAD_LEFT);
            TextView right = button(ime, "›", KeyEvent.KEYCODE_DPAD_RIGHT);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size, Gravity.BOTTOM | Gravity.LEFT);
            lp.leftMargin = edge; lp.bottomMargin = bottom;
            layer.addView(left, lp);
            FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(size, size, Gravity.BOTTOM | Gravity.RIGHT);
            rp.rightMargin = edge; rp.bottomMargin = bottom;
            layer.addView(right, rp);
            decor.addView(layer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlays.put(ime, new WeakReference<>(layer));
            trace("arrows attached; window=" + decor.getClass().getName());
        } catch (Throwable error) { trace("attach failed: " + error); }
    }

    private static TextView button(InputMethodService ime, String glyph, int keycode) {
        TextView view = new TextView(ime);
        view.setText(glyph);
        view.setTextSize(29);
        view.setGravity(Gravity.CENTER);
        view.setIncludeFontPadding(false);
        boolean night = (ime.getResources().getConfiguration().uiMode & 0x30) == 0x20;
        view.setTextColor(night ? Color.WHITE : Color.rgb(35, 38, 45));
        GradientDrawable background = new GradientDrawable();
        background.setColor(night ? 0xCC383A40 : 0xDDE7E9EE);
        background.setCornerRadius(dp(ime, 13));
        view.setBackground(background);
        view.setOnTouchListener(new RepeatListener(ime, keycode));
        return view;
    }

    private static void detach(InputMethodService ime) {
        WeakReference<FrameLayout> ref = overlays.remove(ime);
        FrameLayout layer = ref == null ? null : ref.get();
        if (layer != null && layer.getParent() instanceof ViewGroup) ((ViewGroup)layer.getParent()).removeView(layer);
    }

    private static int dp(InputMethodService ime, int n) {
        return (int)(n * ime.getResources().getDisplayMetrics().density + .5f);
    }

    private static void move(InputMethodService ime, int code) {
        InputConnection connection = ime.getCurrentInputConnection();
        if (connection == null) { trace("move: current InputConnection is null"); return; }
        trace("move: direction=" + (code == KeyEvent.KEYCODE_DPAD_LEFT ? "left" : "right")
                + " connection=" + connection.getClass().getName());
        try {
            if (Build.VERSION.SDK_INT >= 31) {
                SurroundingText surrounding = connection.getSurroundingText(64, 64, 0);
                trace("surrounding: " + (surrounding == null ? "null" :
                        "offset=" + surrounding.getOffset() + " selection="
                                + surrounding.getSelectionStart() + "," + surrounding.getSelectionEnd()
                                + " length=" + (surrounding.getText() == null ? -1 : surrounding.getText().length())));
                if (surrounding != null && surrounding.getOffset() >= 0) {
                    int start = surrounding.getSelectionStart();
                    int end = surrounding.getSelectionEnd();
                    CharSequence content = surrounding.getText();
                    if (start >= 0 && end >= 0 && content != null
                            && start <= content.length() && end <= content.length()) {
                        int position;
                        if (code == KeyEvent.KEYCODE_DPAD_LEFT) {
                            position = Math.min(start, end);
                            if (start == end && position > 0) {
                                position--;
                                if (position > 0 && Character.isLowSurrogate(content.charAt(position))
                                        && Character.isHighSurrogate(content.charAt(position - 1))) position--;
                            }
                        } else {
                            position = Math.max(start, end);
                            if (start == end && position < content.length()) {
                                if (Character.isHighSurrogate(content.charAt(position))
                                        && position + 1 < content.length()
                                        && Character.isLowSurrogate(content.charAt(position + 1))) position++;
                                position++;
                            }
                        }
                        int absolute = surrounding.getOffset() + position;
                        boolean applied = connection.setSelection(absolute, absolute);
                        trace("surrounding setSelection(" + absolute + ")=" + applied);
                        if (applied) return;
                    }
                }
            }
            ExtractedText extracted = connection.getExtractedText(new ExtractedTextRequest(), 0);
            trace("extracted: " + (extracted == null ? "null" :
                    "offset=" + extracted.startOffset + " selection=" + extracted.selectionStart
                            + "," + extracted.selectionEnd + " length="
                            + (extracted.text == null ? -1 : extracted.text.length())));
            if (extracted != null && extracted.text != null && extracted.selectionStart >= 0
                    && extracted.selectionEnd >= 0 && extracted.startOffset >= 0) {
                int position = code == KeyEvent.KEYCODE_DPAD_LEFT
                        ? Math.min(extracted.selectionStart, extracted.selectionEnd)
                        : Math.max(extracted.selectionStart, extracted.selectionEnd);
                if (extracted.selectionStart == extracted.selectionEnd) {
                    position = code == KeyEvent.KEYCODE_DPAD_LEFT
                            ? Math.max(0, position - 1) : Math.min(extracted.text.length(), position + 1);
                }
                int absolute = extracted.startOffset + position;
                boolean applied = connection.setSelection(absolute, absolute);
                trace("extracted setSelection(" + absolute + ")=" + applied);
                if (applied) return;
            }
        } catch (Throwable error) { trace("selection failed: " + error); }
        // Fallback for editors that implement key events but not selection APIs.
        long time = SystemClock.uptimeMillis();
        boolean down = connection.sendKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD));
        boolean up = connection.sendKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_UP, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_SOFT_KEYBOARD));
        trace("key fallback: down=" + down + " up=" + up);
    }

    private static class RepeatListener implements View.OnTouchListener {
        private final InputMethodService ime;
        private final int code;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean pressed;
        private final Runnable repeat = new Runnable() {
            @Override public void run() {
                if (pressed) { move(ime, code); handler.postDelayed(this, 75); }
            }
        };
        RepeatListener(InputMethodService ime, int code) { this.ime = ime; this.code = code; }
        @Override public boolean onTouch(View view, MotionEvent event) {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    trace("touch DOWN: " + (code == KeyEvent.KEYCODE_DPAD_LEFT ? "left" : "right"));
                    pressed = true; view.setPressed(true); move(ime, code);
                    handler.postDelayed(repeat, 350); return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    trace("touch " + (event.getActionMasked() == MotionEvent.ACTION_UP ? "UP" : "CANCEL"));
                    pressed = false; view.setPressed(false); handler.removeCallbacks(repeat); return true;
                default: return true;
            }
        }
    }
}
