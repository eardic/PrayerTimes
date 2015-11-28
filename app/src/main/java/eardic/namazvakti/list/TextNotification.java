package eardic.namazvakti.list;

import android.widget.TextView;

public class TextNotification extends ItemNotification {
    private String notif;

    public TextNotification(String notif) {
        this(null, notif);
    }

    public TextNotification(TextView textView, String notif) {
        super(textView);
        this.setNotif(notif);
    }

    @Override
    public void hide() {
        ((TextView) getView()).setText("");
    }

    @Override
    public void show() {
        ((TextView) getView()).setText(getNotif());
    }

    public String getNotif() {
        return notif;
    }

    public void setNotif(String notif) {
        this.notif = notif;
    }
}
